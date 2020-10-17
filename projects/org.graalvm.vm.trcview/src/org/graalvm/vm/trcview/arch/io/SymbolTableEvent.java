package org.graalvm.vm.trcview.arch.io;

import java.util.NavigableMap;

import org.graalvm.vm.posix.elf.Symbol;

public class SymbolTableEvent extends Event {
    private final NavigableMap<Long, Symbol> symbols;
    private final String filename;
    private final long loadBias;
    private final long address;
    private final long size;

    public SymbolTableEvent(int tid, NavigableMap<Long, Symbol> symbols, String filename, long loadBias, long address, long size) {
        super(tid);
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
}
