package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;

public class GenericSymbol implements Symbol {
    private String name;
    private long value;
    private long size;
    private byte info;
    private byte other;
    private short shndx;

    public GenericSymbol(GenericSymbol sym) {
        name = sym.name;
        value = sym.value;
        size = sym.size;
        info = sym.info;
        other = sym.other;
        shndx = sym.shndx;
    }

    public GenericSymbol(WordInputStream in) throws IOException {
        value = in.read64bit();
        size = in.read64bit();
        info = (byte) in.read8bit();
        other = (byte) in.read8bit();
        shndx = in.read16bit();
        name = IO.readString(in);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getBind() {
        return info >>> 4;
    }

    @Override
    public int getType() {
        return info & 0xf;
    }

    @Override
    public int getVisibility() {
        return other & 0x3;
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
        return shndx;
    }

    @Override
    public GenericSymbol offset(long off) {
        GenericSymbol sym = new GenericSymbol(this);
        sym.value += off;
        return sym;
    }

    @Override
    public String toString() {
        return String.format("Symbol[%s=0x%x]", getName(), getValue());
    }
}
