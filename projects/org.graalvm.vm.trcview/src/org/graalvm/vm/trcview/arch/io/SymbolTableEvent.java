package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class SymbolTableEvent extends Event {
    private final NavigableMap<Long, Symbol> symbols;
    private final String filename;
    private final long loadBias;
    private final long address;
    private final long size;

    public SymbolTableEvent(int tid, NavigableMap<Long, Symbol> symbols, String filename, long loadBias, long address, long size) {
        super(Elf.EM_NONE, SYMBOL_TABLE, tid);
        this.symbols = symbols;
        this.filename = filename;
        this.loadBias = loadBias;
        this.address = address;
        this.size = size;
    }

    public NavigableMap<Long, Symbol> getSymbols() {
        return symbols;
    }

    public String getFilename() {
        return filename;
    }

    public long getLoadBias() {
        return loadBias;
    }

    public long getAddress() {
        return address;
    }

    public long getSize() {
        return size;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(loadBias);
        out.write64bit(address);
        out.write64bit(size);
        out.write32bit(symbols.size());
        IO.writeString(out, filename);
        for (Symbol symbol : symbols.values()) {
            out.write64bit(symbol.getValue());
            out.write64bit(symbol.getSize());
            out.write16bit(symbol.getSectionIndex());
            out.write(symbol.getBind());
            out.write(symbol.getType());
            out.write(symbol.getVisibility());
            IO.writeString(out, symbol.getName());
        }
    }

    public static SymbolTableEvent readRecord(WordInputStream in, int tid) throws IOException {
        NavigableMap<Long, Symbol> symbols = new TreeMap<>();
        long loadBias = in.read64bit();
        long address = in.read64bit();
        long size = in.read64bit();
        int count = in.read32bit();
        String filename = IO.readString(in);
        for (int i = 0; i < count; i++) {
            long value = in.read64bit();
            long sz = in.read64bit();
            short shndx = in.read16bit();
            int bind = in.read();
            int type = in.read();
            int visibility = in.read();
            String name = IO.readString(in);
            Symbol sym = new TraceSymbol(name, value, sz, bind, type, visibility, shndx);
            symbols.put(sym.getValue(), sym);
        }
        return new SymbolTableEvent(tid, symbols, filename, loadBias, address, size);
    }
}
