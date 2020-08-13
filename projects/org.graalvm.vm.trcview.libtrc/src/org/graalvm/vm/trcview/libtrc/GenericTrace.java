package org.graalvm.vm.trcview.libtrc;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.util.io.BEOutputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericTrace<T> {
    public static final int NUMBERFMT_HEX = 0;
    public static final int NUMBERFMT_OCT = 1;

    public static final byte RECORD_STEP = 0;
    public static final byte RECORD_DELTA_STEP = 1;
    public static final byte RECORD_MMAP = 2;
    public static final byte RECORD_MUNMAP = 3;
    public static final byte RECORD_MPROTECT = 4;
    public static final byte RECORD_READ = 5;
    public static final byte RECORD_WRITE = 6;
    public static final byte RECORD_READ_8 = 7;
    public static final byte RECORD_READ_16 = 8;
    public static final byte RECORD_READ_32 = 9;
    public static final byte RECORD_READ_64 = 10;
    public static final byte RECORD_WRITE_8 = 11;
    public static final byte RECORD_WRITE_16 = 12;
    public static final byte RECORD_WRITE_32 = 13;
    public static final byte RECORD_WRITE_64 = 14;
    public static final byte RECORD_DUMP = 15;
    public static final byte RECORD_TRAP = 16;
    public static final byte RECORD_SYMBOLS = 17;
    public static final byte RECORD_ENDIANESS = 18;

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
    private final int masklen;

    private boolean isBE = true;

    private T lastState = null;
    private byte[] lastSerializedState = null;

    private int numberfmt = NUMBERFMT_HEX;
    private int addrwidth = 16;
    private int wordwidth = 16;
    private int machinecodesz = 1;

    public GenericTrace(OutputStream out, Class<T> state) throws IOException {
        this.out = new BEOutputStream(out);
        serializer = new StateSerializer<>(state);
        int mask = serializer.getSize() / 8;
        if ((serializer.getSize() % 8) != 0) {
            mask++;
        }
        masklen = mask;
        writeHeader();
    }

    public void setNumberFormat(int fmt) {
        numberfmt = fmt;
    }

    public void setAddressWidth(int width) {
        addrwidth = width;
    }

    public void setWordWidth(int width) {
        wordwidth = width;
    }

    public void setMachinecodeSize(int sz) {
        machinecodesz = sz;
    }

    private void write(String s) throws IOException {
        if (s == null) {
            out.write16bit((short) 0xFFFF);
        } else {
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            out.write16bit((short) data.length);
            out.write(data);
        }
    }

    private void write8(String s) throws IOException {
        if (s == null) {
            out.write8bit((byte) 0);
        } else {
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            out.write8bit((byte) data.length);
            out.write(data);
        }
    }

    private void writeHeader() throws IOException {
        byte[] magic = {(byte) 'X', (byte) 'T', (byte) 'R', (byte) 'C', 0, 0};
        out.write(magic);

        out.write8bit((byte) (numberfmt | (isBE ? 0x80 : 0)));
        out.write8bit((byte) addrwidth);
        out.write8bit((byte) wordwidth);
        out.write8bit((byte) machinecodesz);

        int statesz = serializer.getSize();
        out.write16bit((short) statesz);
        List<StateField> fields = serializer.getLayout();
        out.write16bit((short) fields.size());
        for (StateField field : fields) {
            out.write16bit((short) field.getOffset());
            out.write8bit((byte) (field.getType() | (field.getFormat() << 4) | 0x80));
            write8(field.getName());
        }

        write(serializer.getFormat());
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

    public void setBigEndian() {
        isBE = true;
    }

    public void setLittleEndian() {
        isBE = false;
    }

    public void step(int tid, long step, T state, String[] asm, byte[] machinecode, byte type) throws IOException {
        if (lastState == null) {
            fullStep(tid, step, state, asm, machinecode, type);
        } else {
            deltaStep(tid, step, state, asm, machinecode, type);
        }
        lastState = state;
    }

    public void fullStep(int tid, long step, T state, String[] asm, byte[] machinecode, byte type) throws IOException {
        long pc;
        try {
            pc = serializer.getPC(state);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IOException("Error while serializing state: " + e, e);
        }
        out.write8bit(RECORD_STEP);
        out.write32bit(tid);
        out.write64bit(step);
        out.write64bit(pc);
        try {
            lastSerializedState = serializer.serialize(state);
            out.write(lastSerializedState);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IOException("Error while serializing state: " + e, e);
        }
        out.write((byte) asm.length);
        for (String s : asm) {
            writeCmdPart(s);
        }
        out.write8bit((byte) machinecode.length);
        out.write(machinecode);
        out.write8bit(type);
    }

    public void deltaStep(int tid, long step, T state, String[] asm, byte[] machinecode, byte type) throws IOException {
        long pc;
        try {
            pc = serializer.getPC(state);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IOException("Error while serializing state: " + e, e);
        }
        out.write8bit(RECORD_DELTA_STEP);
        out.write32bit(tid);
        out.write64bit(step);
        out.write64bit(pc);
        try {
            byte[] current = serializer.serialize(state);
            byte[] mask = new byte[masklen];
            int deltalen = 0;
            for (int i = 0; i < current.length; i++) {
                if (current[i] != lastSerializedState[i]) {
                    mask[i / 8] |= 1 << (i % 8);
                    deltalen++;
                }
            }
            byte[] delta = new byte[deltalen];
            for (int i = 0, j = 0; i < current.length; i++) {
                if (current[i] != lastSerializedState[i]) {
                    delta[j++] = current[i];
                }
            }
            out.write(mask);
            out.write(delta);
            lastSerializedState = current;
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IOException("Error while serializing state: " + e, e);
        }
        out.write((byte) asm.length);
        for (String s : asm) {
            writeCmdPart(s);
        }
        out.write8bit((byte) machinecode.length);
        out.write(machinecode);
        out.write8bit(type);
    }

    public void mmap(int tid, long addr, long len, int prot, int flags, long off, int fd, long result, String filename) throws IOException {
        out.write8bit(RECORD_MMAP);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(len);
        out.write64bit(off);
        out.write64bit(result);
        out.write32bit(prot);
        out.write32bit(flags);
        out.write32bit(fd);
        write(filename);
    }

    public void munmap(int tid, long addr, long len, int result) throws IOException {
        out.write8bit(RECORD_MUNMAP);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(len);
        out.write32bit(result);
    }

    public void read(int tid, long addr, long val, byte size, boolean be) throws IOException {
        out.write8bit(RECORD_READ);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(val);
        out.write8bit(size);
        out.write8bit((byte) (be ? 3 : 2));
    }

    public void read(int tid, long addr, byte size, boolean be) throws IOException {
        out.write8bit(RECORD_READ);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(0);
        out.write8bit(size);
        out.write8bit((byte) (be ? 1 : 0));
    }

    public void write(int tid, long addr, long val, byte size, boolean be) throws IOException {
        out.write8bit(RECORD_WRITE);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(val);
        out.write8bit(size);
        out.write8bit((byte) (be ? 3 : 2));
    }

    public void readI8(int tid, long addr, byte val) throws IOException {
        out.write8bit(RECORD_READ_8);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write8bit(val);
    }

    public void readI8(int tid, long addr) throws IOException {
        read(tid, addr, (byte) 1, isBE);
    }

    public void readI16(int tid, long addr, short val) throws IOException {
        out.write8bit(RECORD_READ_16);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write16bit(val);
    }

    public void readI16(int tid, long addr) throws IOException {
        read(tid, addr, (byte) 2, isBE);
    }

    public void readI32(int tid, long addr, int val) throws IOException {
        out.write8bit(RECORD_READ_32);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write32bit(val);
    }

    public void readI32(int tid, long addr) throws IOException {
        read(tid, addr, (byte) 4, isBE);
    }

    public void readI64(int tid, long addr, long val) throws IOException {
        out.write8bit(RECORD_READ_64);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(val);
    }

    public void readI64(int tid, long addr) throws IOException {
        read(tid, addr, (byte) 8, isBE);
    }

    public void writeI8(int tid, long addr, byte val) throws IOException {
        out.write8bit(RECORD_WRITE_8);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write8bit(val);
    }

    public void writeI16(int tid, long addr, short val) throws IOException {
        out.write8bit(RECORD_WRITE_16);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write16bit(val);
    }

    public void writeI32(int tid, long addr, int val) throws IOException {
        out.write8bit(RECORD_WRITE_32);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write32bit(val);
    }

    public void writeI64(int tid, long addr, long val) throws IOException {
        out.write8bit(RECORD_WRITE_64);
        out.write32bit(tid);
        out.write64bit(addr);
        out.write64bit(val);
    }

    public void symbols(int tid, long loadbias, long address, long size, String filename, Collection<Symbol> symbols) throws IOException {
        out.write8bit(RECORD_SYMBOLS);
        out.write32bit(tid);
        out.write64bit(loadbias);
        out.write64bit(address);
        out.write64bit(size);
        write(filename);
        out.write32bit(symbols.size());
        for (Symbol sym : symbols) {
            out.write64bit(sym.getValue());
            out.write64bit(sym.getSize());
            out.write8bit((byte) (sym.getType() | (sym.getBind() << 4)));
            out.write8bit((byte) sym.getVisibility());
            out.write16bit(sym.getSectionIndex());
            write(sym.getName());
        }
    }

    public void dump(int tid, long address, byte[] data) throws IOException {
        out.write8bit(RECORD_DUMP);
        out.write32bit(tid);
        out.write64bit(address);
        out.write32bit(data.length);
        out.write(data);
    }
}
