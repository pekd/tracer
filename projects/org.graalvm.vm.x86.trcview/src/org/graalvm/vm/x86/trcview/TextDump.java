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
package org.graalvm.vm.x86.trcview;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.graalvm.vm.posix.elf.DefaultSymbolResolver;
import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.x86.node.debug.trace.BrkRecord;
import org.graalvm.vm.x86.node.debug.trace.CallArgsRecord;
import org.graalvm.vm.x86.node.debug.trace.ExecutionTraceReader;
import org.graalvm.vm.x86.node.debug.trace.MemoryEventRecord;
import org.graalvm.vm.x86.node.debug.trace.MmapRecord;
import org.graalvm.vm.x86.node.debug.trace.MprotectRecord;
import org.graalvm.vm.x86.node.debug.trace.MunmapRecord;
import org.graalvm.vm.x86.node.debug.trace.Record;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.node.debug.trace.SymbolTableRecord;
import org.graalvm.vm.x86.node.debug.trace.SystemLogRecord;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.analysis.MappedFile;
import org.graalvm.vm.x86.trcview.analysis.MappedFiles;
import org.graalvm.vm.x86.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;
import org.graalvm.vm.x86.trcview.ui.Location;

public class TextDump {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: trcdump in.trc out.log [record types]");
            System.exit(1);
        }
        boolean dumpMemory = true;
        boolean dumpLog = true;
        boolean dumpCalls = true;
        boolean dumpPC = true;
        boolean dumpState = true;
        boolean dumpMman = true;
        if (args.length > 2) {
            dumpMemory = false;
            dumpLog = false;
            dumpCalls = false;
            dumpPC = false;
            dumpState = false;
            dumpMman = false;
            for (int i = 2; i < args.length; i++) {
                switch (args[i]) {
                    case "+mem":
                        dumpMemory = true;
                        break;
                    case "-mem":
                        dumpMemory = false;
                        break;
                    case "+mman":
                        dumpMman = true;
                        break;
                    case "-mman":
                        dumpMman = false;
                        break;
                    case "+log":
                        dumpLog = true;
                        break;
                    case "-log":
                        dumpLog = false;
                        break;
                    case "+calls":
                        dumpCalls = true;
                        break;
                    case "-calls":
                        dumpCalls = false;
                        break;
                    case "+pc":
                        dumpPC = true;
                        break;
                    case "-pc":
                        dumpPC = false;
                        break;
                    case "+state":
                        dumpState = true;
                        break;
                    case "-state":
                        dumpState = false;
                        break;
                    case "+step":
                        dumpPC = true;
                        dumpState = true;
                        break;
                    case "-step":
                        dumpPC = false;
                        dumpState = false;
                        break;
                    default:
                        System.err.println("Unknown option: " + args[i]);
                        System.exit(1);
                }
            }
        }
        try (InputStream fin = new BufferedInputStream(new FileInputStream(args[0]));
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[1])));
                        ExecutionTraceReader in = new ExecutionTraceReader(fin)) {
            Record record;
            NavigableMap<Long, Symbol> symbols = new TreeMap<>();
            NavigableMap<Long, MappedFile> filenames = new TreeMap<>();
            SymbolResolver resolver = new DefaultSymbolResolver(symbols);
            MappedFiles files = new MappedFiles(filenames);
            while ((record = in.read()) != null) {
                if (record instanceof MemoryEventRecord) {
                    if (dumpMemory) {
                        out.println(record.toString());
                    }
                } else if (record instanceof SystemLogRecord) {
                    if (dumpLog) {
                        out.println(record.toString());
                    }
                } else if (record instanceof CallArgsRecord) {
                    if (dumpCalls) {
                        out.println(record.toString());
                    }
                } else if (record instanceof MmapRecord) {
                    if (dumpMman) {
                        out.println(record.toString());
                    }
                } else if (record instanceof MunmapRecord) {
                    if (dumpMman) {
                        out.println(record.toString());
                    }
                } else if (record instanceof MprotectRecord) {
                    if (dumpMman) {
                        out.println(record.toString());
                    }
                } else if (record instanceof BrkRecord) {
                    if (dumpMman) {
                        out.println(record.toString());
                    }
                } else if (record instanceof SymbolTableRecord) {
                    SymbolTableRecord symtab = (SymbolTableRecord) record;
                    symbols.putAll(symtab.getSymbols());
                    long addr = symtab.getLoadBias();
                    long end = addr + symtab.getSize();
                    while (addr < end) {
                        Entry<Long, MappedFile> file = filenames.ceilingEntry(addr);
                        if (file != null && file.getValue().getFilename().equals(symtab.getFilename())) {
                            file.getValue().setLoadBias(symtab.getLoadBias());
                            addr = file.getKey() + 1;
                        } else {
                            break;
                        }
                    }
                    files = new MappedFiles(filenames);
                    resolver = new DefaultSymbolResolver(symbols);
                } else if (record instanceof MmapRecord) {
                    MmapRecord mmap = (MmapRecord) record;
                    filenames.put(mmap.getAddress(), new MappedFile(mmap.getFileDescriptor(), mmap.getAddress(), mmap.getOffset(), mmap.getLength(), mmap.getFilename(), -1));
                    files = new MappedFiles(filenames);
                } else if (record instanceof StepRecord) {
                    StepRecord step = (StepRecord) record;
                    if (dumpState) {
                        out.println("----------------");
                        out.println("[tid=" + step.getTid() + "]");
                    }
                    if (dumpPC) {
                        if (!dumpState) {
                            out.print("[tid=" + step.getTid() + "] ");
                        }
                        Location loc = Location.getLocation(new SimpleTraceAnalyzer(resolver, files), step);
                        out.println(encode(loc));
                    }
                    if (dumpState) {
                        out.println();
                        out.println(step.getState());
                    }
                }
            }
        }
    }

    private static String str(String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    private static String encode(Location location) {
        StringBuilder buf = new StringBuilder();
        buf.append("IN: ");
        buf.append(str(location.getSymbol()));
        if (location.getFilename() != null) {
            buf.append(" # ");
            buf.append(location.getFilename());
            if (location.getOffset() != -1) {
                buf.append(" @ 0x");
                buf.append(HexFormatter.tohex(location.getOffset(), 8));
            }
        }
        buf.append("\n0x");
        buf.append(HexFormatter.tohex(location.getPC(), 8));
        buf.append(":\t");
        if (location.getDisassembly() != null) {
            buf.append(location.getAsm());
            buf.append(" ; ");
            buf.append(location.getPrintableBytes());
        }
        return buf.toString();
    }

    private static class SimpleTraceAnalyzer implements TraceAnalyzer {
        private SymbolResolver resolver;
        private MappedFiles files;

        private SimpleTraceAnalyzer(SymbolResolver resolver, MappedFiles files) {
            this.resolver = resolver;
            this.files = files;
        }

        public Symbol getSymbol(long pc) {
            return resolver.getSymbol(pc);
        }

        public ComputedSymbol getComputedSymbol(long pc) {
            return null;
        }

        public void renameSymbol(ComputedSymbol sym, String name) {
        }

        public void setPrototype(ComputedSymbol sym, Prototype prototype) {
        }

        public Set<ComputedSymbol> getSubroutines() {
            return null;
        }

        public Set<ComputedSymbol> getLocations() {
            return null;
        }

        public Collection<ComputedSymbol> getSymbols() {
            return null;
        }

        public Map<String, List<ComputedSymbol>> getNamedSymbols() {
            return null;
        }

        public void addSymbolRenameListener(SymbolRenameListener listener) {
        }

        public void addSubroutine(long pc, String name, Prototype prototype) {
        }

        public void reanalyze() {
        }

        public BlockNode getRoot() {
            return null;
        }

        public BlockNode getParent(Node node) {
            return null;
        }

        public BlockNode getChildren(Node node) {
            return null;
        }

        public Node getInstruction(long insn) {
            return null;
        }

        public Node getNext(Node node) {
            return null;
        }

        public Node getNextStep(Node node) {
            return null;
        }

        public Node getPreviousStep(Node node) {
            return null;
        }

        public Node getNextPC(Node node, long pc) {
            return null;
        }

        public byte getI8(long address, long insn) throws MemoryNotMappedException {
            return 0;
        }

        public long getI64(long address, long insn) throws MemoryNotMappedException {
            return 0;
        }

        public MemoryRead getLastRead(long address, long insn) throws MemoryNotMappedException {
            return null;
        }

        public MemoryRead getNextRead(long address, long insn) throws MemoryNotMappedException {
            return null;
        }

        public MemoryUpdate getLastWrite(long address, long insn) throws MemoryNotMappedException {
            return null;
        }

        public Node getMapNode(long address, long insn) throws MemoryNotMappedException {
            return null;
        }

        public long getBase(long pc) {
            return files.getBase(pc);
        }

        public long getLoadBias(long pc) {
            return files.getLoadBias(pc);
        }

        public long getOffset(long pc) {
            return files.getOffset(pc);
        }

        public long getFileOffset(long pc) {
            return files.getFileOffset(pc);
        }

        public String getFilename(long pc) {
            return files.getFilename(pc);
        }
    }
}
