package org.graalvm.vm.trcview.arch;

import org.graalvm.vm.util.io.Endianess;

public class ByteCodeReader extends CodeReader {
    private final byte[] code;
    private final int offs;

    public ByteCodeReader(byte[] code, long pc, boolean isBE) {
        super(pc, isBE);
        this.code = code;
        this.offs = 0;
    }

    public ByteCodeReader(byte[] code, long pc, int off, boolean isBE) {
        super(pc, isBE);
        this.code = code;
        this.offs = off;
    }

    @Override
    public byte nextI8() {
        return code[offs + n++];
    }

    @Override
    public short nextI16() {
        int offset = offs + n;
        n += 2;
        if (isBE()) {
            return Endianess.get16bitBE(code, offset);
        } else {
            return Endianess.get16bitLE(code, offset);
        }
    }

    @Override
    public int nextI32() {
        int offset = offs + n;
        n += 4;
        if (isBE()) {
            return Endianess.get32bitBE(code, offset);
        } else {
            return Endianess.get32bitLE(code, offset);
        }
    }

    @Override
    public long nextI64() {
        int offset = offs + n;
        n += 8;
        if (isBE()) {
            return Endianess.get64bitBE(code, offset);
        } else {
            return Endianess.get64bitLE(code, offset);
        }
    }

    @Override
    public byte peekI8(int off) {
        int ptr = offs + n + off;
        if (ptr < 0 || ptr >= code.length) {
            throw new IndexOutOfBoundsException(ptr);
        }
        return code[ptr];
    }

    @Override
    public short peekI16(int off) {
        int ptr = offs + n + off;
        if (ptr < 0 || ptr + 1 >= code.length) {
            throw new IndexOutOfBoundsException(ptr);
        }
        if (isBE()) {
            return Endianess.get16bitBE(code, ptr);
        } else {
            return Endianess.get16bitLE(code, ptr);
        }
    }

    @Override
    public int peekI32(int off) {
        int ptr = offs + n + off;
        if (ptr < 0 || ptr + 3 >= code.length) {
            throw new IndexOutOfBoundsException(ptr);
        }
        if (isBE()) {
            return Endianess.get32bitBE(code, ptr);
        } else {
            return Endianess.get32bitLE(code, ptr);
        }
    }

    @Override
    public long peekI64(int off) {
        int ptr = offs + n + off;
        if (ptr < 0 || ptr + 7 >= code.length) {
            throw new IndexOutOfBoundsException(ptr);
        }
        if (isBE()) {
            return Endianess.get64bitBE(code, ptr);
        } else {
            return Endianess.get64bitLE(code, ptr);
        }
    }

    @Override
    public ByteCodeReader clone() {
        return new ByteCodeReader(code, getPC(), offs, isBE());
    }
}
