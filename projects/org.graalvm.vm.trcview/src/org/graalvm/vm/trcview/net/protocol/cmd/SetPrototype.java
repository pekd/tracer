package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class SetPrototype extends Command {
    private long pc;
    private Prototype proto;

    public SetPrototype() {
        super(SET_PROTOTYPE);
    }

    public SetPrototype(long pc, Prototype prototype) {
        super(SET_PROTOTYPE);
        this.pc = pc;
        this.proto = prototype;
    }

    public long getPC() {
        return pc;
    }

    public Prototype getPrototype() {
        return proto;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        pc = in.read64bit();
        proto = IO.readPrototype(in);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(pc);
        IO.writePrototype(out, proto);
    }

}
