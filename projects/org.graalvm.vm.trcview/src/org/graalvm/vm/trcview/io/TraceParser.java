package org.graalvm.vm.trcview.io;

import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.arch.io.DeviceDefinitionEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.IncompleteTraceStep;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class TraceParser {
    private static final Logger log = Trace.create(TraceParser.class);

    private static final long THRESHOLD = 10_000;

    private final TraceReader in;
    private final Analysis analysis;
    private final ProgressListener progress;

    private final boolean system;
    private final boolean stackedTraps;

    private final ThreadContext[] threads;
    private long threadBits;

    private final Map<Integer, ThreadContext> threadsHigh;

    private long cnt = 0;

    @SuppressWarnings("unchecked")
    private TraceParser(TraceReader in, Analysis analysis, ProgressListener progress) {
        this.in = in;
        this.analysis = analysis;
        this.progress = progress;

        system = in.getArchitecture().isSystemLevel();
        stackedTraps = in.getArchitecture().isStackedTraps();

        threads = new ThreadContext[64];
        for (int i = 0; i < 64; i++) {
            threads[i] = new ThreadContext(i);
        }
        threadBits = 0;

        threadsHigh = new HashMap<>();
    }

    private Event readEvent() throws IOException {
        try {
            return in.read();
        } catch (EOFException e) {
            log.log(Levels.WARNING, "Unexpected EOF", e);
            return null;
        }
    }

    private class ThreadContext {
        private final int tid;

        private StepEvent lastStep = null;
        private BlockNode parent = new BlockNode((StepEvent) null);

        ThreadContext(int tid) {
            this.tid = tid;
        }

        private void ret() {
            parent.trim();
            if (parent.getNodes().isEmpty()) {
                parent.add(new IncompleteTraceStep(tid));
            }
            if (parent.parent != null) {
                parent = parent.parent;
            }
        }

        private void ret(BlockNode root) {
            while (parent != root) {
                parent.trim();
                if (parent.getNodes().isEmpty()) {
                    parent.add(new IncompleteTraceStep(tid));
                }
                if (parent.parent != null) {
                    parent = parent.parent;
                }
            }
            ret();
        }

        private void process(Event evt) {
            cnt++;
            if (cnt >= THRESHOLD && progress != null) {
                cnt = 0;
                progress.progressUpdate(in.tell());
            }

            if (evt instanceof StepEvent) {
                StepEvent step = (StepEvent) evt;
                if (step.getMachinecode() != null && (step.isCall() || (system && step.isSyscall()))) {
                    // call or syscall
                    BlockNode block = new BlockNode(step);
                    parent.add(block);
                    parent = block;
                    analysis.process(step, block);
                } else if (step.isReturn()) {
                    // return
                    analysis.process(step, step);
                    parent.add(step);

                    if (!system || stackedTraps || !parent.isInterrupt()) {
                        // RET can only return from traps if stacked traps are enabled
                        ret();
                    }
                } else if (system && step.isReturnFromSyscall()) {
                    analysis.process(step, step);
                    parent.add(step);

                    if (stackedTraps) {
                        ret();
                    } else {
                        BlockNode irq = parent;
                        while (irq != null && !irq.isInterrupt()) {
                            irq = irq.parent;
                        }
                        if (irq == null) {
                            // act as if it was a ret
                            ret();
                        } else {
                            // return to trap
                            ret(irq);
                        }
                    }
                } else {
                    // normal step event
                    analysis.process(step, step);
                    parent.add(step);
                }
                lastStep = step;
            } else if (evt instanceof InterruptEvent) {
                InterruptEvent trap = (InterruptEvent) evt;
                if (lastStep != null && lastStep.getType() != InstructionType.SYSCALL) {
                    BlockNode block = new BlockNode(trap);
                    analysis.process(trap, block);
                    parent.add(block);
                    parent = block;
                } else if (lastStep == null) {
                    // like a call
                    BlockNode block = new BlockNode(lastStep);
                    parent.add(block);
                    parent = block;
                    analysis.process(lastStep, block);
                }
            } else if (evt instanceof DeviceDefinitionEvent) {
                analysis.process(evt, evt);
            } else { // memory events, device register events, ...
                analysis.process(evt, evt);
            }
        }

        private BlockNode get() {
            while (parent.parent != null) {
                parent = parent.parent;
            }
            return parent;
        }
    }

    private void read() throws IOException {
        Event evt;
        while ((evt = readEvent()) != null) {
            process(evt);
        }
    }

    private void process(Event evt) {
        int tid = evt.getTid();
        if (tid >= 0 && tid < 64) {
            threadBits |= 1L << tid;
            threads[tid].process(evt);
        } else {
            ThreadContext ctx = threadsHigh.get(tid);
            if (ctx == null) {
                ctx = new ThreadContext(tid);
                threadsHigh.put(tid, ctx);
            }
            ctx.process(evt);
        }
    }

    private Map<Integer, BlockNode> getThreads() {
        Map<Integer, BlockNode> result = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            if ((threadBits & (1L << i)) != 0) {
                ThreadContext thread = threads[i];
                BlockNode block = thread.get();
                if (block.getHead() != null || block.getFirstNode() != null) {
                    result.put(i, thread.get());
                }
            }
        }

        for (Entry<Integer, ThreadContext> entry : threadsHigh.entrySet()) {
            ThreadContext thread = entry.getValue();
            BlockNode block = thread.get();
            if (block.getHead() != null || block.getFirstNode() != null) {
                result.put(entry.getKey(), thread.get());
            }
        }

        return result;
    }

    public static Map<Integer, BlockNode> parse(TraceReader in, Analysis analysis, ProgressListener progress) throws IOException {
        TraceParser parser = new TraceParser(in, analysis, progress);
        parser.read();
        return parser.getThreads();
    }
}
