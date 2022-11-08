package org.graalvm.vm.trcview.io;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.arch.io.TraceSymbol;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class StandardSymbolTable {
    private NavigableMap<Long, Symbol> symbols;
    private String filename;
    private long loadBias;
    private long address;
    private long size;

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

    public void read32(WordInputStream in) throws IOException {
        symbols = new TreeMap<>();
        loadBias = Integer.toUnsignedLong(in.read32bit());
        address = Integer.toUnsignedLong(in.read32bit());
        size = Integer.toUnsignedLong(in.read32bit());
        filename = IO.readString(in);
        int count = in.read32bit();
        for (int i = 0; i < count; i++) {
            long value = Integer.toUnsignedLong(in.read32bit());
            long sz = Integer.toUnsignedLong(in.read32bit());
            short shndx = in.read16bit();
            int bind = in.read();
            int type = in.read();
            int visibility = in.read();
            String name = IO.readString(in);
            Symbol sym = new TraceSymbol(name, value, sz, bind, type, visibility, shndx);
            symbols.put(sym.getValue(), sym);
        }
    }

    public void read64(WordInputStream in) throws IOException {
        symbols = new TreeMap<>();
        loadBias = in.read64bit();
        address = in.read64bit();
        size = in.read64bit();
        filename = IO.readString(in);
        int count = in.read32bit();
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
    }

    public void write32(WordOutputStream out) throws IOException {
        out.write32bit((int) loadBias);
        out.write32bit((int) address);
        out.write32bit((int) size);
        IO.writeString(out, filename);
        out.write32bit(symbols.size());
        for (Symbol symbol : symbols.values()) {
            out.write32bit((int) symbol.getValue());
            out.write32bit((int) symbol.getSize());
            out.write16bit(symbol.getSectionIndex());
            out.write(symbol.getBind());
            out.write(symbol.getType());
            out.write(symbol.getVisibility());
            IO.writeString(out, symbol.getName());
        }
    }

    public void write64(WordOutputStream out) throws IOException {
        out.write64bit(loadBias);
        out.write64bit(address);
        out.write64bit(size);
        IO.writeString(out, filename);
        out.write32bit(symbols.size());
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
}
