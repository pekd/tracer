package org.graalvm.vm.trcview.libtrc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.util.io.BEOutputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericTrace<T> {
    public static final byte RECORD_STEP = 0;
    public static final byte RECORD_MMAP = 1;
    public static final byte RECORD_MUNMAP = 2;
    public static final byte RECORD_MPROTECT = 3;
    public static final byte RECORD_READ = 4;
    public static final byte RECORD_WRITE = 5;
    public static final byte RECORD_DUMP = 6;

    public static final byte LITTLE_ENDIAN = 0;
    public static final byte BIG_ENDIAN = 1;

    public static final byte TYPE_OTHER = 0;
    public static final byte TYPE_JCC = 1;
    public static final byte TYPE_JMP = 2;
    public static final byte TYPE_JMP_INDIRECT = 3;
    public static final byte TYPE_CALL = 4;
    public static final byte TYPE_RET = 5;
    public static final byte TYPE_SYSCALL = 6;
    public static final byte TYPE_RTI = 7;

    private final WordOutputStream out;
    private final Map<String, Integer> strings = new HashMap<>();

    private final StateSerializer<T> serializer;

    public GenericTrace(OutputStream out, Class<T> state) throws IOException {
        this.out = new BEOutputStream(out);
        serializer = new StateSerializer<>(state);
        writeHeader();
    }

    private void write(String s) throws IOException {
        byte[] data = s.getBytes();
        out.write16bit((short) data.length);
        out.write(data);
    }

    private void writeHeader() throws IOException {
        byte[] magic = {(byte) 'X', (byte) 'T', (byte) 'R', (byte) 'C', -1, -1};
        out.write(magic);
        int stepoff = 0;
        int stepsz = 8;
        int statesz = stepsz + serializer.getSize();
        out.write16bit((short) statesz);
        out.write16bit((short) (serializer.getPCOffset() + stepsz));
        out.write16bit((short) serializer.getPCSize());
        out.write16bit((short) stepoff);
        out.write16bit((short) stepsz);
        write("u64 __step;" + serializer.getLayout());
        write(serializer.getFormat());
        out.write(BIG_ENDIAN);
    }

    private int getString(String s) {
        if (strings.containsKey(s)) {
            return strings.get(s);
        } else {
            strings.put(s, strings.size() + 1);
            return -1;
        }
    }

    private void writeCmdPart(String s) throws IOException {
        int id = getString(s);
        out.write32bit(id);
        if (id == -1) {
            write(s);
        }
    }

    public void step(int tid, long step, T state, String[] asm, byte[] machinecode, byte type) throws IOException {
        byte[] magic = {(byte) 'S', (byte) 'T', (byte) 'E', (byte) 'P'};
        out.write(magic);
        out.write32bit(tid);
        out.write64bit(step);
        try {
            out.write(serializer.serialize(state));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IOException("Error while serializing state: " + e, e);
        }
        out.write((byte) asm.length);
        for (String s : asm) {
            writeCmdPart(s);
        }
        out.write16bit((short) machinecode.length);
        out.write(machinecode);
        out.write(type);
    }

    public void mmap(int tid, long addr, long len, int prot, int flags, long off, int fd, long result, String filename) throws IOException {
        byte[] magic = {(byte) 'M', (byte) 'M', (byte) 'A', (byte) 'P'};
        out.write(magic);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(len);
        out.write64bit(off);
        out.write64bit(result);
        out.write32bit(prot);
        out.write32bit(prot);
        out.write32bit(flags);
        out.write32bit(fd);
        write(filename);
    }
}
