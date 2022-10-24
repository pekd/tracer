package org.graalvm.vm.trcview.arch;

import org.graalvm.vm.util.io.Endianess;

public class ByteCodeReader extends CodeReader {
    private final byte[] code;

    public ByteCodeReader(byte[] code, long pc, boolean isBE) {
        super(pc, isBE);
        this.code = code;
    }

    @Override
    public byte nextI8() {
        return code[n++];
    }

    @Override
    public short nextI16() {
        int offset = n;
        n += 2;
        if (isBE()) {
            return Endianess.get16bitBE(code, offset);
        } else {
            return Endianess.get16bitLE(code, offset);
        }
    }

    @Override
    public int nextI32() {
        int offset = n;
        n += 4;
        if (isBE()) {
            return Endianess.get32bitBE(code, offset);
        } else {
            return Endianess.get32bitLE(code, offset);
        }
    }
}
