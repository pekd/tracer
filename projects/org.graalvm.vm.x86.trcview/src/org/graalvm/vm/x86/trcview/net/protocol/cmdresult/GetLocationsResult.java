package org.graalvm.vm.x86.trcview.net.protocol.cmdresult;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.IO;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;

public class GetLocationsResult extends Result {
    private Set<ComputedSymbol> locs;

    public GetLocationsResult() {
        super(Command.GET_LOCATIONS);
    }

    public GetLocationsResult(Set<ComputedSymbol> locs) {
        super(Command.GET_LOCATIONS);
        this.locs = locs;
    }

    public Set<ComputedSymbol> getLocations() {
        return locs;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        int count = in.read32bit();
        locs = new HashSet<>();
        for (int i = 0; i < count; i++) {
            locs.add(IO.readComputedSymbol(in));
        }
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        out.write32bit(locs.size());
        for (ComputedSymbol loc : locs) {
            IO.writeComputedSymbol(out, loc);
        }
    }
}
