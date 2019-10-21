package org.graalvm.vm.x86.trcview.net.protocol;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class MemoryDataRecord extends RpcRecord {
    public static final int MAGIC = 0x4d454d30; // MEM0

    private long address;
    private long insn;
    private byte[] data;

    protected MemoryDataRecord() {
        super(MAGIC);
    }

    public long getAddress() {
        return address;
    }

    public long getInstruction() {
        return insn;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    protected void writeData(WordOutputStream out) throws IOException {
        out.write64bit(address);
        out.write64bit(insn);
        IO.writeArray(out, data);
    }

    @Override
    protected void parse(WordInputStream in) throws IOException {
        address = in.read64bit();
        insn = in.read64bit();
        data = IO.readArray(in);
    }
}
