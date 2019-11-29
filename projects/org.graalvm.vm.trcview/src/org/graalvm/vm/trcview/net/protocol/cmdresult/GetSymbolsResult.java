package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetSymbolsResult extends Result {
    private Collection<ComputedSymbol> syms;

    public GetSymbolsResult() {
        super(Command.GET_SYMBOLS);
    }

    public GetSymbolsResult(Collection<ComputedSymbol> syms) {
        super(Command.GET_SYMBOLS);
        this.syms = syms;
    }

    public Collection<ComputedSymbol> getSymbols() {
        return syms;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        int count = in.read32bit();
        syms = new HashSet<>();
        for (int i = 0; i < count; i++) {
            syms.add(IO.readComputedSymbol(in));
        }
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write32bit(syms.size());
        for (ComputedSymbol sym : syms) {
            IO.writeComputedSymbol(out, sym);
        }
    }
}
