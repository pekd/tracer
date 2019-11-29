package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetSubroutinesResult extends Result {
    private Set<ComputedSymbol> subs;

    public GetSubroutinesResult() {
        super(Command.GET_SUBROUTINES);
    }

    public GetSubroutinesResult(Set<ComputedSymbol> subs) {
        super(Command.GET_SUBROUTINES);
        this.subs = subs;
    }

    public Set<ComputedSymbol> getSubroutines() {
        return subs;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        int count = in.read32bit();
        subs = new HashSet<>();
        for (int i = 0; i < count; i++) {
            subs.add(IO.readComputedSymbol(in));
        }
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write32bit(subs.size());
        for (ComputedSymbol sub : subs) {
            IO.writeComputedSymbol(out, sub);
        }
    }
}
