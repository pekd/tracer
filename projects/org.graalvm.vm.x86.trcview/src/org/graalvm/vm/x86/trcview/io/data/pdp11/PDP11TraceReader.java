package org.graalvm.vm.x86.trcview.io.data.pdp11;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.x86.trcview.io.data.ArchTraceReader;
import org.graalvm.vm.x86.trcview.io.data.Event;
import org.graalvm.vm.x86.trcview.io.data.MemoryEvent;
import org.graalvm.vm.x86.trcview.io.data.MmapEvent;

public class PDP11TraceReader extends ArchTraceReader {
    public static final int MAGIC_CPU0 = 0x43505530;
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

    private final WordInputStream in;
    private boolean map;
    private PDP11StepEvent lastStep;

    public PDP11TraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public PDP11TraceReader(WordInputStream in) {
        this.in = in;
        map = false;
        lastStep = null;
    }

    private static MmapEvent map() {
        return new MmapEvent(0, 0, 0x10000, Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC, Mman.MAP_PRIVATE | Mman.MAP_FIXED, -1, 0, null, 0, null);
    }

    @Override
    public Event read() throws IOException {
        if (!map) {
            map = true;
            return map();
        }
        int magic;
        try {
            magic = in.read32bit();
        } catch (EOFException e) {
            return null;
        }
        switch (magic) {
            case MAGIC_CPU0:
                lastStep = new PDP11StepEvent(in, 0);
                return lastStep;
            case MAGIC_CPU1:
                PDP11CpuEvent evt = new PDP11CpuEvent(in, 0);
                if (evt.getType() == PDP11CpuEvent.CPU_TRAP) {
                    return evt.getTrapEvent(lastStep);
                } else {
                    return evt;
                }
            case MAGIC_BUS0: {
                PDP11BusEvent bus = new PDP11BusEvent(in, 0);
                MemoryEvent mem = bus.getMemoryEvent();
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
            case MAGIC_DLV1:
                return new PDP11DLV11Event(in, 0);
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
