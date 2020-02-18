package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.ElfStrings;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public abstract class Event extends Node {
    public static final byte EOF = 0;
    public static final byte CPU_STATE = 1;
    public static final byte STEP = 2;
    public static final byte DEVICE = 3;
    public static final byte INTERRUPT = 4;
    public static final byte MEMORY_DUMP = 5;
    public static final byte MEMORY = 6;
    public static final byte MMAP = 7;
    public static final byte MUNMAP = 8;
    public static final byte MPROTECT = 9;
    public static final byte BRK = 10;
    public static final byte SYMBOL_TABLE = 11;
    public static final byte SYSTEM_LOG = 12;
    public static final byte CONIO = 13;

    private final int tid;
    private byte id;
    private short arch;

    protected Event(short arch, byte id, int tid) {
        this.arch = arch;
        this.id = id;
        this.tid = tid;
    }

    @Override
    public final int getTid() {
        return tid;
    }

    public final short getArchitectureId() {
        return arch;
    }

    public final Architecture getArchitecture() {
        return Architecture.getArchitecture(arch);
    }

    protected abstract void writeRecord(WordOutputStream out) throws IOException;

    public final void write(WordOutputStream out) throws IOException {
        out.write16bit(arch);
        out.write16bit(id);
        out.write32bit(tid);
        writeRecord(out);
    }

    public final void writeRaw(WordOutputStream out) throws IOException {
        writeRecord(out);
    }

    public static final <T extends Event> T read(WordInputStream in) throws IOException {
        short archid = in.read16bit();
        short id = in.read16bit();
        int tid = in.read32bit();
        Architecture arch = Architecture.getArchitecture(archid);
        if (arch == null) {
            throw new IOException("unknown architecture " + ElfStrings.getElfMachine(archid));
        }
        return arch.getEventParser().parse(in, (byte) id, tid);
    }
}
