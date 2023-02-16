/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.x86.node.debug.trace;

import java.io.IOException;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class SymbolTableRecord extends Record {
    public static final byte ID = 0x40;

    private NavigableMap<Long, Symbol> symbols;
    private String filename;
    private long loadBias;
    private long address;
    private long size;

    public SymbolTableRecord() {
        super(ID);
    }

    public SymbolTableRecord(long loadBias, String filename, long address, long size, NavigableMap<Long, Symbol> symbols) {
        super(ID);
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

    private static int getSize(Symbol symbol) {
        return sizeString(symbol.getName()) + 21;
    }

    @Override
    protected int getDataSize() {
        return 4 + 3 * 8 + sizeString(filename) + symbols.values().stream().mapToInt(SymbolTableRecord::getSize).sum();
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        symbols = new TreeMap<>();
        loadBias = in.read64bit();
        address = in.read64bit();
        size = in.read64bit();
        filename = readString(in);
        int count = in.read32bit();
        for (int i = 0; i < count; i++) {
            long value = in.read64bit();
            long sz = in.read64bit();
            short shndx = in.read16bit();
            int bind = in.read();
            int type = in.read();
            int visibility = in.read();
            String name = readString(in);
            Symbol sym = new TraceSymbol(name, value, sz, bind, type, visibility, shndx);
            symbols.put(sym.getValue(), sym);
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(loadBias);
        out.write64bit(address);
        out.write64bit(size);
        writeString(out, filename);
        out.write32bit(symbols.size());
        for (Symbol symbol : symbols.values()) {
            out.write64bit(symbol.getValue());
            out.write64bit(symbol.getSize());
            out.write16bit(symbol.getSectionIndex());
            out.write(symbol.getBind());
            out.write(symbol.getType());
            out.write(symbol.getVisibility());
            writeString(out, symbol.getName());
        }
    }
}
