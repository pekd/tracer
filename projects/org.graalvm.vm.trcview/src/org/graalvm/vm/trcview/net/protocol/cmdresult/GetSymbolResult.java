package org.graalvm.vm.trcview.net.protocol.cmdresult;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.Subroutine;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GetSymbolResult extends Result {
    private Symbol sym;

    public GetSymbolResult() {
        super(Command.GET_SYMBOL);
    }

    public GetSymbolResult(Symbol sym) {
        super(Command.GET_SYMBOL);
        this.sym = sym;
    }

    public Symbol getSymbol() {
        return sym;
    }

    @Override
    public void read(WordInputStream in) throws IOException {
        int symtype = in.read();
        if (symtype == 0) {
            sym = null;
            return;
        }
        int bind = in.read();
        int type = in.read();
        int visibility = in.read();
        short section = in.read16bit();
        long value = in.read64bit();
        long size = in.read64bit();
        String name = IO.readString(in);
        if (symtype == 1) { /* normal type */
            sym = new SimpleSymbol(bind, type, visibility, section, value, size, name);
        } else if (symtype == 2) { /* subroutine */
            Prototype proto = IO.readPrototype(in);
            sym = new FunctionSymbol(bind, type, visibility, section, value, size, name, proto);
        } else {
            throw new IOException("Invalid symbol type " + symtype);
        }
    }

    @Override
    public void write(WordOutputStream out) throws IOException {
        if (sym == null) {
            out.write(0);
            return;
        }
        if (sym instanceof Subroutine) {
            out.write(2);
        } else {
            out.write(1);
        }
        out.write(sym.getBind());
        out.write(sym.getType());
        out.write(sym.getVisibility());
        out.write16bit(sym.getSectionIndex());
        out.write64bit(sym.getValue());
        out.write64bit(sym.getSize());
        IO.writeString(out, sym.getName());
        if (sym instanceof Subroutine) {
            Prototype proto = ((Subroutine) sym).getPrototype();
            IO.writePrototype(out, proto);
        }
    }

    private static class SimpleSymbol implements Symbol {
        private final String name;
        private final long value;
        private final long size;
        private final int type;
        private final int bind;
        private final int visibility;
        private final short section;

        public SimpleSymbol(int bind, int type, int visibility, short section, long value, long size, String name) {
            this.bind = bind;
            this.type = type;
            this.visibility = visibility;
            this.section = section;
            this.value = value;
            this.size = size;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getBind() {
            return bind;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public int getVisibility() {
            return visibility;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public short getSectionIndex() {
            return section;
        }

        @Override
        public Symbol offset(long off) {
            throw new UnsupportedOperationException();
        }
    }

    private static class FunctionSymbol implements Symbol, Subroutine {
        private final String name;
        private final long value;
        private final long size;
        private final int type;
        private final int bind;
        private final int visibility;
        private final short section;
        private final Prototype proto;

        public FunctionSymbol(int bind, int type, int visibility, short section, long value, long size, String name, Prototype proto) {
            this.bind = bind;
            this.type = type;
            this.visibility = visibility;
            this.section = section;
            this.value = value;
            this.size = size;
            this.name = name;
            this.proto = proto;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Prototype getPrototype() {
            return proto;
        }

        @Override
        public int getBind() {
            return bind;
        }

        @Override
        public int getType() {
            return type;
        }

        @Override
        public int getVisibility() {
            return visibility;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public short getSectionIndex() {
            return section;
        }

        @Override
        public Symbol offset(long off) {
            throw new UnsupportedOperationException();
        }
    }
}
