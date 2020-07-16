package org.graalvm.vm.trcview.arch.pdp11.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.pdp11.device.PDP11Devices;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class PDP11TraceReader extends ArchTraceReader {
    public static final int MAGIC_CPU0 = 0x43505530;
    public static final int MAGIC_CPUZ = 0x4350555a;
    public static final int MAGIC_CPU1 = 0x43505531;
    public static final int MAGIC_BUS0 = 0x42555330;
    public static final int MAGIC_BUS1 = 0x42555331;
    public static final int MAGIC_TRAP = 0x54524150;
    public static final int MAGIC_IRQ0 = 0x49525130;
    public static final int MAGIC_RX2A = 0x52583241;
    public static final int MAGIC_RX2C = 0x52583243;
    public static final int MAGIC_RX2D = 0x52583244;
    public static final int MAGIC_RX2E = 0x52583245;
    public static final int MAGIC_RX2S = 0x52583253;
    public static final int MAGIC_DLV1 = 0x444C5631;

    private static final int STEP_LIMIT = 5_000;

    private final WordInputStream in;
    private int init = 0;
    private PDP11StepEvent lastStep;
    private PDP11CpuState lastState = null;
    private long steps = 0;

    private MemoryEvent mem;
    private PDP11DLV11Event dlv11j;

    public PDP11TraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public PDP11TraceReader(WordInputStream in) {
        this.in = in;
        lastStep = null;
    }

    private static MmapEvent map() {
        return new MmapEvent(0, 0, 0x10000, Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC, Mman.MAP_PRIVATE | Mman.MAP_FIXED, -1, 0, null, 0, null);
    }

    @Override
    public Event read() throws IOException {
        switch (init) {
            case 0:
                init++;
                return map();
            case 1:
                init++;
                return PDP11Devices.createDevices();
        }

        if (mem != null) {
            int addr = (int) mem.getAddress();
            short value = (short) mem.getValue();
            boolean write = mem.isWrite();
            mem = null;
            switch (addr) {
                case 0177170:
                    return new PDP11DeviceRegisterEvent(0, PDP11Devices.RXV21, PDP11Devices.RXV21_RX2CS, value, write);
                case 0177172:
                    return new PDP11DeviceRegisterEvent(0, PDP11Devices.RXV21, PDP11Devices.RXV21_RX2DB, value, write);
                case 0177560:
                    return new PDP11DeviceRegisterEvent(0, PDP11Devices.DLV11J, PDP11Devices.DLV11J_RCSR, value, write);
                case 0177562:
                    return new PDP11DeviceRegisterEvent(0, PDP11Devices.DLV11J, PDP11Devices.DLV11J_RBUF, value, write);
                case 0177564:
                    return new PDP11DeviceRegisterEvent(0, PDP11Devices.DLV11J, PDP11Devices.DLV11J_XCSR, value, write);
                case 0177566:
                    return new PDP11DeviceRegisterEvent(0, PDP11Devices.DLV11J, PDP11Devices.DLV11J_XBUF, value, write);
            }
        }

        if (dlv11j != null) {
            IoEvent ioe = dlv11j.getIoEvent();
            dlv11j = null;
            if (ioe != null) {
                return ioe;
            }
        }

        int magic;
        try {
            magic = in.read32bit();
        } catch (EOFException e) {
            return null;
        }
        switch (magic) {
            case MAGIC_CPU0:
                steps = 0;
                lastState = new PDP11CpuFullState(in, 0);
                lastStep = lastState;
                return lastStep;
            case MAGIC_CPUZ:
                steps++;
                lastState = new PDP11CpuDeltaState(in, lastState, 0);
                if (steps >= STEP_LIMIT) {
                    lastState = new PDP11CpuFullState(lastState);
                    steps = 0;
                }
                lastStep = lastState;
                return lastStep;
            case MAGIC_CPU1: {
                PDP11CpuEvent evt = new PDP11CpuEvent(in, 0);
                if (evt.getType() == PDP11CpuEvent.CPU_TRAP) {
                    return evt.getTrapEvent(lastStep);
                } else {
                    return evt;
                }
            }
            case MAGIC_BUS0: {
                PDP11BusEvent bus = new PDP11BusEvent(in, 0);
                mem = bus.getMemoryEvent();
                if (mem != null) {
                    return mem;
                } else {
                    return bus;
                }
            }
            case MAGIC_BUS1:
                PDP11MemoryDumpEvent dump = new PDP11MemoryDumpEvent(in, 0);
                return dump.getMemoryDumpEvent();
            case MAGIC_TRAP:
                return new PDP11TrapEvent(in, 0);
            case MAGIC_IRQ0:
                return new PDP11IrqEvent(in, 0);
            case MAGIC_DLV1: {
                long step = lastStep != null ? lastStep.getStep() : 0;
                dlv11j = new PDP11DLV11Event(in, 0, step);
                return dlv11j;
            }
            case MAGIC_RX2C:
                return new PDP11RXV21Command(in, 0);
            case MAGIC_RX2S:
                return new PDP11RXV21Step(in, 0);
            case MAGIC_RX2D:
                return new PDP11RXV21Dma(in, 0);
            case MAGIC_RX2E:
                return new PDP11RXV21Error(in, 0);
            case MAGIC_RX2A:
                return new PDP11RXV21Disk(in, 0);
        }
        throw new IOException("unknown record: " + HexFormatter.tohex(magic, 8) + " [position " + tell() + "]");
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
