package org.graalvm.vm.x86.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.IO;

public class AddSubroutine extends Command {
    private ComputedSymbol sub;

    public AddSubroutine() {
        super(ADD_SUBROUTINE);
    }

    public AddSubroutine(ComputedSymbol sub) {
        super(ADD_SUBROUTINE);
        this.sub = sub;
    }

    public ComputedSymbol getSubroutine() {
        return sub;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        sub = IO.readComputedSymbol(in);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        IO.writeComputedSymbol(out, sub);
    }
}
