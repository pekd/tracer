package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetComputedSymbolResult extends Result {
    private ComputedSymbol sym;

    public GetComputedSymbolResult() {
        super(Command.GET_COMPUTED_SYMBOL);
    }

    public GetComputedSymbolResult(ComputedSymbol sym) {
        super(Command.GET_COMPUTED_SYMBOL);
        this.sym = sym;
    }

    public ComputedSymbol getSymbol() {
        return sym;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        sym = IO.readComputedSymbol(in);
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        IO.writeComputedSymbol(out, sym);
    }
}
