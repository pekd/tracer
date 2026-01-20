package org.graalvm.vm.trcview.arch;

public class ShortCodeReader extends CodeReader {
    private final short[] code;

    public ShortCodeReader(short[] code, long pc) {
        super(pc, false);
        this.code = code;
    }

    @Override
    public byte nextI8() {
        throw new UnsupportedOperationException("only 16bit access supported");
    }

    @Override
    public short nextI16() {
        return code[n++];
    }

    @Override
    public int nextI32() {
        throw new UnsupportedOperationException("only 16bit access supported");
    }

    @Override
    public byte peekI8(int offset) {
        throw new UnsupportedOperationException("only 16bit access supported");
    }

    @Override
    public short peekI16(int offset) {
        return code[n + offset / 2];
    }

    @Override
    public int n() {
        return n << 1;
    }

    @Override
    public ShortCodeReader clone() {
        return new ShortCodeReader(code, getPC());
    }
}
