package org.graalvm.vm.trcview.arch.x86.decode.isa;

import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;
import org.graalvm.vm.util.HexFormatter;

public class LocationOperand extends Operand {
    private long addr;

    public LocationOperand(long addr) {
        super(Type.LABEL, "0x" + HexFormatter.tohex(addr), addr);
        this.addr = addr;
    }

    @Override
    public Token[] getTokens() {
        String loc = getLocation(addr);
        if (loc != null) {
            return new Token[]{new Token(Type.LABEL, loc, addr)};
        } else {
            return super.getTokens();
        }
    }

    @Override
    public int getSize() {
        return 0;
    }
}
