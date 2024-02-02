package org.graalvm.vm.trcview.arch.z80.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.z80.device.Z80Devices;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.LEInputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.log.Trace;

public class Z80TraceReader extends ArchTraceReader {
    public static final int TYPE_STEP = 0;
    public static final int TYPE_READ = 1;
    public static final int TYPE_WRITE = 2;
    public static final int TYPE_IN = 3;
    public static final int TYPE_OUT = 4;
    public static final int TYPE_DUMP = 5;
    public static final int TYPE_MAP = 6;
    public static final int TYPE_DEVICES = 7;
    public static final int TYPE_SET_I = 8;
    public static final int TYPE_SET_IM = 9;
    public static final int TYPE_SET_EI = 10;
    public static final int TYPE_IRQ = 11;
    public static final int TYPE_READ32 = 12;
    public static final int TYPE_WRITE32 = 13;

    private static final Logger log = Trace.create(Z80TraceReader.class);

    private final WordInputStream in;

    private Z80Devices devices = null;

    private long step = 0;

    private Event lastEvent = null;

    private Z80StepEvent lastStep = null;

    private boolean irqlatch = false;
    private byte irq = 0;

    public Z80TraceReader(InputStream in) {
        this(new LEInputStream(in));
    }

    private Z80TraceReader(WordInputStream in) {
        this.in = in;
    }

    private static MmapEvent mmap(long start, long end, String name, boolean ro) {
        int prot = Mman.PROT_READ | Mman.PROT_EXEC;
        if (!ro) {
            prot |= Mman.PROT_WRITE;
        }
        return new MmapEvent(0, start, end - start + 1, prot, Mman.MAP_FIXED | Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS, -1, 0, name, start, null);
    }

    private Event in(Z80InputEvent evt) {
        if (devices != null) {
            return devices.getInputEvent(evt.getAddress(), evt.getValue());
        } else {
            return null;
        }

    }

    private Event out(Z80OutputEvent evt) {
        if (devices != null) {
            return devices.getOutputEvent(evt.getAddress(), evt.getValue());
        } else {
            return null;
        }
    }

    @Override
    public Event read() throws IOException {
        if (lastEvent != null) {
            Event evt = lastEvent;
            lastEvent = null;
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
                Z80CpuState state = new Z80CpuState(0, step++, in);
                if (irqlatch) {
                    Z80StepEvent evt = lastStep;
                    irqlatch = false;
                    lastEvent = state;
                    lastStep = state;
                    return new Z80InterruptEvent(0, irq, evt);
                }
                lastStep = state;
                return state;
            }
            case TYPE_SET_I: {
                int i = in.read8bit();
                return new Z80DeviceRegisterEvent(0, Z80Devices.CPU, Z80Devices.CPU_I, (byte) i, true);
            }
            case TYPE_SET_IM: {
                int im = in.read8bit();
                return new Z80DeviceRegisterEvent(0, Z80Devices.CPU, Z80Devices.CPU_IM, (byte) im, true);
            }
            case TYPE_SET_EI: {
                int ei = in.read8bit();
                return new Z80DeviceRegisterEvent(0, Z80Devices.CPU, Z80Devices.CPU_EI, (byte) ei, true);
            }
            case TYPE_IRQ: {
                irq = (byte) in.read8bit();
                irqlatch = true;
                return new Z80InterruptDeviceEvent(0, irq);
            }
            case TYPE_READ: {
                byte value = (byte) in.read8bit();
                int address = Short.toUnsignedInt(in.read16bit());
                return new MemoryEventI8(false, 0, address, false, value);
            }
            case TYPE_WRITE: {
                byte value = (byte) in.read8bit();
                int address = Short.toUnsignedInt(in.read16bit());
                return new MemoryEventI8(false, 0, address, true, value);
            }
            case TYPE_READ32: {
                byte value = (byte) in.read8bit();
                in.read16bit();
                int address = in.read32bit();
                return new MemoryEventI8(false, 0, Integer.toUnsignedLong(address), false, value);
            }
            case TYPE_WRITE32: {
                byte value = (byte) in.read8bit();
                in.read16bit();
                int address = in.read32bit();
                return new MemoryEventI8(false, 0, Integer.toUnsignedLong(address), true, value);
            }
            case TYPE_IN: {
                Z80InputEvent inp = new Z80InputEvent(0, in);
                Event evt = in(inp);
                if (evt != null) {
                    lastEvent = inp;
                    return evt;
                } else {
                    return inp;
                }
            }
            case TYPE_OUT: {
                Z80OutputEvent out = new Z80OutputEvent(0, in);
                Event evt = out(out);
                if (evt != null) {
                    lastEvent = out;
                    return evt;
                } else {
                    return out;
                }
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
            case TYPE_MAP: {
                int length = in.read8bit();
                in.read16bit();
                long addr = Integer.toUnsignedLong(in.read32bit());
                long len = Integer.toUnsignedLong(in.read32bit());
                long end = addr + len;
                byte[] buf = new byte[length & 0x7F];
                in.read(buf);
                boolean ro = (length & 0x80) != 0;
                String name = new String(buf, 0, buf.length, StandardCharsets.US_ASCII);
                return mmap(addr, end, name, ro);
            }
            case TYPE_DEVICES: {
                devices = new Z80Devices(in);
                return devices.getEvent();
            }
        }
        return null;
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
