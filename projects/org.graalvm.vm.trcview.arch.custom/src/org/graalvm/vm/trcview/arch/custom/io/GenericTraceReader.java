package org.graalvm.vm.trcview.arch.custom.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.arch.io.MunmapEvent;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.script.Parser;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class GenericTraceReader extends ArchTraceReader {
    public static final int STEP = 0x53544550;
    public static final int MMAP = 0x4D4D4150;
    public static final int UMAP = 0x554D4150;
    public static final int MEMR = 0x4D454D52;
    public static final int MEMW = 0x4D454D57;
    public static final int STRG = 0x53545247;

    private static final String TYPES = "typedef uint8_t u8;\n" +
                    "typedef uint16_t u16;\n" +
                    "typedef uint32_t u32;\n" +
                    "typedef uint64_t u64;\n" +
                    "typedef int8_t s8;\n" +
                    "typedef int16_t s16;\n" +
                    "typedef int32_t s32;\n" +
                    "typedef int64_t s64;\n";

    private final WordInputStream in;
    private boolean initialized;
    private int stepsize;
    private Struct stepstruct;
    private String format;
    private StateDescription desc;
    private boolean bigEndian;

    private List<String> strings;

    public GenericTraceReader(InputStream in) {
        this(new BEInputStream(in));
    }

    public GenericTraceReader(WordInputStream in) {
        this.in = in;
        strings = new ArrayList<>();
        initialized = false;
        strings.add("");
    }

    private void init() throws IOException {
        if (initialized) {
            return;
        } else {
            initialized = true;
        }
        stepsize = in.read16bit();
        int pcoff = in.read16bit();
        int pcsz = in.read16bit();
        int stepoff = in.read16bit();
        int stepsz = in.read16bit();
        String structdef = IO.readString(in);
        format = IO.readString(in);
        Parser parser = new Parser(TYPES + "struct step { " + structdef + " };");
        parser.parse();
        if (parser.errors.numErrors() > 0) {
            throw new IOException("Invalid step record definition:\n" + parser.errors.dump());
        }
        stepstruct = parser.types.getStruct("step");
        bigEndian = in.read8bit() == 1;
        desc = new StateDescription(pcoff, pcsz, stepoff, stepsz, stepstruct, format, bigEndian);
    }

    private static InstructionType getType(int type) {
        switch (type) {
            default:
            case 0:
                return InstructionType.OTHER;
            case 1:
                return InstructionType.JCC;
            case 2:
                return InstructionType.JMP;
            case 3:
                return InstructionType.JMP_INDIRECT;
            case 4:
                return InstructionType.CALL;
            case 5:
                return InstructionType.RET;
            case 6:
                return InstructionType.SYSCALL;
            case 7:
                return InstructionType.RTI;
        }
    }

    @Override
    public Event read() throws IOException {
        if (!initialized) {
            init();
        }

        int magic;
        try {
            magic = in.read32bit();
        } catch (EOFException e) {
            return null;
        }
        int tid = in.read32bit();

        switch (magic) {
            case STEP: {
                byte[] data = new byte[stepsize];
                in.read(data);
                int idcnt = in.read8bit();
                String[] asm = new String[idcnt];
                for (int i = 0; i < asm.length; i++) {
                    int id = in.read32bit();
                    if (id == -1) {
                        String s = IO.readString(in);
                        strings.add(s);
                        asm[i] = s;
                    } else {
                        asm[i] = strings.get(id);
                    }
                }
                byte[] machinecode = IO.readShortArray(in);
                InstructionType type = getType(in.read8bit());
                return new GenericStepEvent(tid, desc, data, machinecode, asm, type);
            }
            case MMAP: {
                long address = in.read64bit();
                long length = in.read64bit();
                long offset = in.read64bit();
                long result = in.read64bit();
                int protection = in.read32bit();
                int flags = in.read32bit();
                int fd = in.read32bit();
                String filename = IO.readString(in);
                return new MmapEvent(tid, address, length, protection, flags, fd, offset, filename, result, null);
            }
            case UMAP: {
                long address = in.read64bit();
                long length = in.read64bit();
                int result = in.read32bit();
                return new MunmapEvent(tid, address, length, result);
            }
            case MEMR: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                int flags = in.read8bit();
                boolean be = BitTest.test(flags, 1);
                boolean hasvalue = BitTest.test(flags, 2);
                if (hasvalue) {
                    return new MemoryEvent(be, tid, address, size, false, value);
                } else {
                    return new MemoryEvent(be, tid, address, size, false);
                }
            }
            case MEMW: {
                long address = in.read64bit();
                long value = in.read64bit();
                byte size = (byte) in.read8bit();
                int flags = in.read8bit();
                boolean be = BitTest.test(flags, 1);
                boolean hasvalue = BitTest.test(flags, 2);
                if (hasvalue) {
                    return new MemoryEvent(be, tid, address, size, true, value);
                } else {
                    return new MemoryEvent(be, tid, address, size, true);
                }
            }
            case STRG: {
                String s = IO.readString(in);
                strings.add(s);
                return read();
            }
            default:
                throw new IOException("unknown record type 0x" + HexFormatter.tohex(magic));
        }
    }

    @Override
    public long tell() {
        return in.tell();
    }
}
