package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class RenameSymbol extends Command {
    private long pc;
    private String name;

    public RenameSymbol() {
        super(RENAME_SYMBOL);
    }

    public RenameSymbol(long pc, String name) {
        super(RENAME_SYMBOL);
        this.pc = pc;
        this.name = name;
    }

    public long getPC() {
        return pc;
    }

    public String getName() {
        return name;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        pc = in.read64bit();
        name = IO.readString(in);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write64bit(pc);
        IO.writeString(out, name);
    }
}
