package org.graalvm.vm.trcview.net.protocol;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class InstructionRecord extends RpcRecord {
    public static final int MAGIC = 0x494e534e; // INSN

    private long pc;
    private long insncnt;
    private String[] asm;
    private byte[] machinecode;

    protected InstructionRecord() {
        super(MAGIC);
    }

    @Override
    protected void writeData(WordOutputStream out) throws IOException {
        out.write64bit(pc);
        out.write64bit(insncnt);
        IO.writeArray(out, machinecode);
        IO.writeStringArray(out, asm);
    }

    @Override
    protected void parse(WordInputStream in) throws IOException {
        pc = in.read64bit();
        insncnt = in.read64bit();
        machinecode = IO.readArray(in);
        asm = IO.readStringArray(in);
    }
}
