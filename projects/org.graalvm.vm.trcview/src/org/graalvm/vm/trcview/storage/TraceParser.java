package org.graalvm.vm.trcview.storage;

import java.io.EOFException;
import java.io.IOException;
import java.util.logging.Logger;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.io.ProgressListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class TraceParser {
    private static final Logger log = Trace.create(TraceParser.class);
    private static final int PROGRESS_UPDATE = 10_000;

    private final StorageBackend backend;
    private final TraceReader in;
    private final ProgressListener progress;

    private int tid;
    private long steps;

    public TraceParser(TraceReader in, ProgressListener progress, StorageBackend backend) {
        this.in = in;
        this.backend = backend;
        this.progress = progress;
        steps = 0;
    }

    private static class Step {
        public final Step parent;
        public final StepEvent step;
        public final boolean trap;

        Step(Step parent, StepEvent step, boolean trap) {
            this.parent = parent;
            this.step = step;
            this.trap = trap;
        }
    }

    private void createStep(Step step) {
        byte[] cpustate = StorageBackend.serialize(step.step.getState());
        int type = StorageBackend.getInstructionType(step.step.getType());
        if (step.trap) {
            type = StorageBackend.TYPE_TRAP;
        }
        long parent = -1;
        if (step.parent != null) {
            parent = step.parent.step.getStep();
        }
        backend.createStep(step.step.getTid(), step.step.getStep(), parent, step.step.getPC(), type, step.step.getMachinecode(), cpustate);
    }

    private void createTrap(Step step, InterruptEvent evt) {
        byte[] cpustate = StorageBackend.serialize(evt);
        long parent = -1;
        if (step != null && step.parent != null) {
            parent = step.parent.step.getStep();
        }
        int steptid = tid;
        long id = -1;
        long pc = -1;
        byte[] machinecode = {};
        if (step != null) {
            steptid = step.step.getTid();
            id = step.step.getStep();
            pc = step.step.getPC();
            machinecode = step.step.getMachinecode();
        }
        backend.createStep(steptid, id, parent, pc, StorageBackend.TYPE_TRAP, machinecode, cpustate);
    }

    private void createRead(Step last, int rdtid, long address, int size, long value) {
        long id = -1;
        if (last != null) {
            id = last.step.getStep();
        }
        backend.createRead(rdtid, id, address, size, value);
    }

    private void createWrite(Step last, int wrtid, long address, int size, long value) {
        long id = -1;
        if (last != null) {
            id = last.step.getStep();
        }
        backend.createWrite(wrtid, id, address, size, value);
    }

    private Event readEvent() throws IOException {
        try {
            return in.read();
        } catch (EOFException e) {
            log.log(Levels.WARNING, "Unexpected EOF", e);
            return null;
        }
    }

    public long read() throws IOException {
        boolean system = in.getArchitecture().isSystemLevel();
        Event event;
        Step parent = null;
        Step lastStep = null;
        long stepcnt = 0;

        while ((event = readEvent()) != null) {
            if (stepcnt % PROGRESS_UPDATE == 0 && progress != null) {
                progress.progressUpdate(in.tell());
            }
            stepcnt++;

            while (tid != 0 && event.getTid() != tid) {
                event = in.read();
                if (event == null) {
                    break;
                }
            }
            if (tid == 0) {
                tid = event.getTid();
            }

            // handle event
            if (event instanceof StepEvent) {
                steps++;

                StepEvent step = (StepEvent) event;
                Step current = new Step(parent, step, false);
                createStep(current);
                if (step.getMachinecode() != null && (step.isCall() || (system && step.isSyscall()))) {
                    parent = current;
                } else if (step.isReturn()) {
                    if (parent != null) {
                        parent = parent.parent;
                    }
                } else if (system && step.isReturnFromSyscall()) {
                    Step irq = parent;
                    while (irq != null && !irq.trap) {
                        irq = irq.parent;
                    }
                    if (irq == null) {
                        // act as if it was a ret
                        if (parent != null) {
                            parent = parent.parent;
                        }
                    } else {
                        // return to trap
                        parent = irq.parent;
                    }
                }
                lastStep = current;
            } else if (event instanceof InterruptEvent) {
                InterruptEvent trap = (InterruptEvent) event;
                if (lastStep != null && lastStep.step.getType() != InstructionType.SYSCALL) {
                    createTrap(lastStep, trap);
                    parent = new Step(parent, lastStep.step, true);
                } else if (lastStep == null) {
                    createTrap(lastStep, trap);
                    parent = new Step(parent, null, true);
                }
            } else if (event instanceof MemoryEvent) {
                MemoryEvent evt = (MemoryEvent) event;
                long addr = evt.getAddress();
                if (evt.getSize() <= 8) {
                    long val = evt.getValue();
                    if (evt.isBigEndian()) {
                        val = Long.reverseBytes(val);
                    }
                    if (evt.isWrite()) {
                        createWrite(lastStep, evt.getTid(), addr, evt.getSize(), val);
                    } else {
                        createRead(lastStep, evt.getTid(), addr, evt.getSize(), val);
                    }
                } else {
                    Vector128 val = evt.getVector();
                    long hi = val.getI64(1);
                    long lo = val.getI64(0);
                    if (evt.isBigEndian()) {
                        hi = Long.reverseBytes(val.getI64(0));
                        lo = Long.reverseBytes(val.getI64(1));
                    }
                    if (evt.isWrite()) {
                        createWrite(lastStep, evt.getTid(), addr, evt.getSize(), lo);
                        createWrite(lastStep, evt.getTid(), addr + 8, evt.getSize(), hi);
                    } else {
                        createRead(lastStep, evt.getTid(), addr, evt.getSize(), lo);
                        createRead(lastStep, evt.getTid(), addr + 8, evt.getSize(), hi);
                    }
                }
            } else if (event instanceof MemoryDumpEvent) {
                MemoryDumpEvent evt = (MemoryDumpEvent) event;
                long addr = evt.getAddress();
                byte[] data = evt.getData();
                for (int i = 0; i < data.length; i++) {
                    createWrite(lastStep, evt.getTid(), addr + i, 1, data[i]);
                }
            }
        }

        // final progress update
        if (progress != null) {
            progress.progressUpdate(in.tell());
        }

        return steps;
    }
}
