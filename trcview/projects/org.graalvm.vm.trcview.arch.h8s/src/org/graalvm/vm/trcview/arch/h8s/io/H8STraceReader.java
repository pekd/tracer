package org.graalvm.vm.trcview.arch.h8s.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.h8s.device.H8SDevices;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.LEInputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.log.Trace;

public class H8STraceReader extends ArchTraceReader {
    private static final Logger log = Trace.create(H8STraceReader.class);

    private static final int TYPE_READ_I8 = 0;
    private static final int TYPE_READ_I16 = 1;
    private static final int TYPE_READ_I32 = 2;
    private static final int TYPE_WRITE_I8 = 3;
    private static final int TYPE_WRITE_I16 = 4;
    private static final int TYPE_WRITE_I32 = 5;
    private static final int TYPE_STEP = 6;
    private static final int TYPE_DUMP = 7;
    private static final int TYPE_MAP = 8;
    private static final int TYPE_IRQ = 9;

    private static final int STEP_LIMIT = 2_000;

    private final WordInputStream in;

    private final int mcuType;
    private long step = 0;

    private final H8SDevices devices;

    private H8SCpuState last = new H8SZeroCpuState(0);

    private Event latch;

    public H8STraceReader(InputStream in) {
        this(new LEInputStream(in));
    }

    private H8STraceReader(WordInputStream in) {
        this.in = in;

        short type = 0;

        try {
            type = in.read16bit();
        } catch (IOException e) {
            // swallow?
        }

        mcuType = type;
        log.info("H8S type: " + type);

        devices = new H8SDevices(mcuType);
        latch = devices.getEvent();
    }

    public int getMCUType() {
        return mcuType;
    }

    private static boolean isIO(long address) {
        int addr = (int) (address & 0x00FFFFFF);
        if (addr < 0xFFFE40) {
            return false;
        } else if (addr < 0xFFFF08) {
            return true;
        } else if (addr < 0xFFFF28) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Event read() throws IOException {
        if (latch != null) {
            Event evt = latch;
            latch = null;
            return evt;
        }

        int type;
        try {
            type = in.read8bit();
        } catch (EOFException e) {
            return null;
        }

        switch (type) {
            case TYPE_STEP: {
                int bitfield = Short.toUnsignedInt(in.read16bit());
                int changesReg = Integer.bitCount(bitfield & 0xFF);
                int changesSPR = Integer.bitCount(bitfield & 0xF00);
                H8SCpuState state;
                if (changesReg == 0 && changesSPR == 0) {
                    state = new H8STinyDeltaCpuState(step++, in, bitfield, last);
                } else if (changesReg == 0) {
                    state = new H8SSmallSPRDeltaCpuState(step++, in, bitfield, last);
                } else if (changesReg == 1) {
                    state = new H8SSmallDeltaCpuState(step++, in, bitfield, last);
                } else {
                    state = new H8SDeltaCpuState(step++, in, bitfield, last);
                }
                if ((step % STEP_LIMIT) == 0) {
                    state = new H8SFullCpuState(state);
                }
                last = state;
                return state;
            }
            case TYPE_READ_I8: {
                byte value = (byte) in.read8bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                if (isIO(address)) {
                    latch = devices.getReadEvent((int) address, value);
                }
                return new MemoryEventI8(true, 0, address, false, value);
            }
            case TYPE_READ_I16: {
                short value = in.read16bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                if (isIO(address)) {
                    latch = devices.getReadEvent((int) address, value);
                }
                return new MemoryEventI16(true, 0, address, false, value);
            }
            case TYPE_READ_I32: {
                int value = in.read32bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                if (isIO(address)) {
                    latch = devices.getReadEvent((int) address, value);
                }
                return new MemoryEventI32(true, 0, address, false, value);
            }
            case TYPE_WRITE_I8: {
                byte value = (byte) in.read8bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                if (isIO(address)) {
                    latch = devices.getWriteEvent((int) address, value);
                }
                return new MemoryEventI8(true, 0, address, true, value);
            }
            case TYPE_WRITE_I16: {
                short value = in.read16bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                if (isIO(address)) {
                    latch = devices.getWriteEvent((int) address, value);
                }
                return new MemoryEventI16(true, 0, address, true, value);
            }
            case TYPE_WRITE_I32: {
                int value = in.read32bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                if (isIO(address)) {
                    latch = devices.getWriteEvent((int) address, value);
                }
                return new MemoryEventI32(true, 0, address, true, value);
            }
            case TYPE_MAP: {
                int namelen = in.read8bit();
                long address = Integer.toUnsignedLong(in.read32bit());
                long length = Integer.toUnsignedLong(in.read32bit());
                byte[] rawName = new byte[namelen];
                in.read(rawName);
                String name = new String(rawName, StandardCharsets.UTF_8);
                return new MmapEvent(0, address, length, Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC, Mman.MAP_FIXED | Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS, -1, 0, name, address, null);
            }
            case TYPE_DUMP: {
                long address = Integer.toUnsignedLong(in.read32bit());
                long length = Integer.toUnsignedLong(in.read32bit());
                if (length == 0) {
                    // skip this dump event, return the next one
                    log.warning("Suspicious DUMP event of length 0 encountered for address 0x" + HexFormatter.tohex(address, 4));
                    return read();
                }
                byte[] data = new byte[(int) length];
                in.read(data);
                return new MemoryDumpEvent(0, address, data);
            }
            case TYPE_IRQ: {
                short irq = in.read16bit();
                latch = new H8SInterruptDeviceEvent(irq);
                return new H8SInterruptEvent(irq, last);
            }
        }

        throw new IOException("unknown record: " + type + " [position " + tell() + "]");
    }

    @Override
    public void finish(TraceAnalyzer trc) {
        devices.setRegisterNames(trc);

        TypedMemory mem = trc.getTypedMemory();
        ArchitectureTypeInfo info = trc.getArchitecture().getTypeInfo();
        Type ptr = new Type(new Type(DataType.VOID), info);
        if (mcuType == 2350) {
            mem.set(0x0000, ptr, "VEC_RESET");
            mem.set(0x0004, ptr, "VEC_MRST");
            mem.set(0x0014, ptr, "VEC_TRACE");
            mem.set(0x001C, ptr, "VEC_NMI");
            mem.set(0x0020, ptr, "VEC_TRAP0");
            mem.set(0x0024, ptr, "VEC_TRAP1");
            mem.set(0x0028, ptr, "VEC_TRAP2");
            mem.set(0x002C, ptr, "VEC_TRAP3");
            mem.set(0x0040, ptr, "VEC_IRQ0");
            mem.set(0x0044, ptr, "VEC_IRQ1");
            mem.set(0x0048, ptr, "VEC_IRQ2");
            mem.set(0x004C, ptr, "VEC_IRQ3");
            mem.set(0x0050, ptr, "VEC_IRQ4");
            mem.set(0x0054, ptr, "VEC_IRQ5");
            mem.set(0x0058, ptr, "VEC_IRQ6");
            mem.set(0x005C, ptr, "VEC_IRQ7");
            mem.set(0x0060, ptr, "VEC_SWDTEND");
            mem.set(0x0064, ptr, "VEC_WOVI");
            mem.set(0x0068, ptr, "VEC_CMI");
            mem.set(0x0070, ptr, "VEC_ADI");
            mem.set(0x0080, ptr, "VEC_TGI0A");
            mem.set(0x0084, ptr, "VEC_TGI0B");
            mem.set(0x0088, ptr, "VEC_TGI0C");
            mem.set(0x008C, ptr, "VEC_TGI0D");
            mem.set(0x0090, ptr, "VEC_TGI0V");
            mem.set(0x0090, ptr, "VEC_TGI0V");
            mem.set(0x00A0, ptr, "VEC_TGI1A");
            mem.set(0x00A4, ptr, "VEC_TGI1B");
            mem.set(0x00A8, ptr, "VEC_TGI1V");
            mem.set(0x00AC, ptr, "VEC_TGI1U");
            mem.set(0x00B0, ptr, "VEC_TGI2A");
            mem.set(0x00B4, ptr, "VEC_TGI2B");
            mem.set(0x00B8, ptr, "VEC_TGI2V");
            mem.set(0x00BC, ptr, "VEC_TGI2U");
            mem.set(0x00C0, ptr, "VEC_TGI3A");
            mem.set(0x00C4, ptr, "VEC_TGI3B");
            mem.set(0x00C8, ptr, "VEC_TGI3C");
            mem.set(0x00CC, ptr, "VEC_TGI3D");
            mem.set(0x00D0, ptr, "VEC_TGI3V");
            mem.set(0x00E0, ptr, "VEC_TGI4A");
            mem.set(0x00E4, ptr, "VEC_TGI4B");
            mem.set(0x00E8, ptr, "VEC_TGI4V");
            mem.set(0x00EC, ptr, "VEC_TGI4U");
            mem.set(0x00F0, ptr, "VEC_TGI5A");
            mem.set(0x00F4, ptr, "VEC_TGI5B");
            mem.set(0x00F8, ptr, "VEC_TGI5V");
            mem.set(0x00FC, ptr, "VEC_TGI5U");
            mem.set(0x0120, ptr, "VEC_DEND0A");
            mem.set(0x0124, ptr, "VEC_DEND0B");
            mem.set(0x0128, ptr, "VEC_DEND1A");
            mem.set(0x012C, ptr, "VEC_DEND1B");
            mem.set(0x0140, ptr, "VEC_ERI0");
            mem.set(0x0144, ptr, "VEC_RXI0");
            mem.set(0x0148, ptr, "VEC_TXI0");
            mem.set(0x014C, ptr, "VEC_TEI0");
            mem.set(0x0150, ptr, "VEC_ERI1");
            mem.set(0x0154, ptr, "VEC_RXI1");
            mem.set(0x0158, ptr, "VEC_TXI1");
            mem.set(0x015C, ptr, "VEC_TEI1");
        }
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
