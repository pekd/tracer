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
package org.graalvm.vm.x86.trcview.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.posix.elf.DefaultSymbolResolver;
import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.x86.trcview.analysis.type.DataType;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.analysis.type.Type;
import org.graalvm.vm.x86.trcview.arch.Architecture;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.io.data.BrkEvent;
import org.graalvm.vm.x86.trcview.io.data.Event;
import org.graalvm.vm.x86.trcview.io.data.MemoryDumpEvent;
import org.graalvm.vm.x86.trcview.io.data.MemoryEvent;
import org.graalvm.vm.x86.trcview.io.data.MmapEvent;
import org.graalvm.vm.x86.trcview.io.data.StepEvent;
import org.graalvm.vm.x86.trcview.io.data.SymbolTableEvent;

public class Analysis {
    private static final Logger log = Trace.create(Analysis.class);

    private SymbolTable symbols;
    private StepEvent lastStep;

    private NavigableMap<Long, Symbol> symbolTable;
    private NavigableMap<Long, MappedFile> mappedFiles;
    private SymbolResolver resolver;
    private SymbolResolver augmentedResolver;

    private long steps;
    private long idcnt;

    private MemoryTrace memory;

    private List<Node> nodes;
    private boolean system;

    public Analysis(Architecture arch) {
        symbols = new SymbolTable();
        symbolTable = new TreeMap<>();
        mappedFiles = new TreeMap<>();
        resolver = new DefaultSymbolResolver(symbolTable);
        augmentedResolver = new AugmentingSymbolResolver(resolver, symbols);
        memory = new MemoryTrace();
        nodes = new ArrayList<>();
        system = arch.isSystemLevel();
    }

    public void start() {
        lastStep = null;
        steps = 0;
        idcnt = 0;
    }

    private void add(Node node) {
        assert nodes.size() == idcnt;
        node.setId(idcnt++);
        nodes.add(node);
    }

    public void process(Event event, Node node) {
        add(node);

        if (event instanceof StepEvent) {
            steps++;
            StepEvent step = (StepEvent) event;
            if (lastStep != null) {
                long pc = step.getPC();
                Symbol sym;
                switch (lastStep.getType()) {
                    case JMP:
                    case JCC:
                        sym = resolver.getSymbol(pc);
                        if (sym != null && sym.getType() == Symbol.FUNC && sym.getValue() == pc) {
                            symbols.addSubroutine(pc, sym.getName());
                        } else {
                            symbols.addLocation(pc);
                        }
                        break;
                    case CALL:
                        sym = resolver.getSymbol(pc);
                        if (sym != null && sym.getName() != null) {
                            symbols.addSubroutine(pc, sym.getName());
                        } else {
                            symbols.addSubroutine(pc);
                        }
                        break;
                    case SYSCALL:
                        if (system) {
                            sym = resolver.getSymbol(pc);
                            if (sym != null && sym.getName() != null) {
                                symbols.addSyscall(pc, sym.getName());
                            } else {
                                symbols.addSyscall(pc);
                            }
                        }
                        break;
                }
                symbols.visit(node);
            }
            lastStep = step;
        } else if (event instanceof SymbolTableEvent) {
            SymbolTableEvent symtab = (SymbolTableEvent) event;
            symbolTable.putAll(symtab.getSymbols());
            long addr = symtab.getLoadBias();
            long end = addr + symtab.getSize();
            while (addr < end) {
                Entry<Long, MappedFile> file = mappedFiles.ceilingEntry(addr);
                if (file != null && file.getValue().getFilename() != null && file.getValue().getFilename().equals(symtab.getFilename())) {
                    file.getValue().setLoadBias(symtab.getLoadBias());
                    addr = file.getKey() + 1;
                } else {
                    break;
                }
            }
        } else if (event instanceof MmapEvent) {
            MmapEvent mmap = (MmapEvent) event;
            mappedFiles.put(mmap.getResult(), new MappedFile(mmap.getFileDescriptor(), mmap.getResult(), mmap.getLength(), mmap.getOffset(), mmap.getFilename(), -1));
            long pc = 0;
            long insn = 0;
            if (lastStep != null) {
                pc = lastStep.getPC();
                insn = lastStep.getStep();
            }
            if (mmap.getResult() != -1) {
                if (mmap.getData() != null) {
                    memory.mmap(mmap.getResult(), mmap.getLength(), mmap.getData(), pc, insn, node);
                } else {
                    memory.mmap(mmap.getResult(), mmap.getLength(), pc, insn, node);
                }
            }
        } else if (event instanceof MemoryEvent) {
            MemoryEvent memevent = (MemoryEvent) event;
            if (memevent.isWrite()) {
                long pc = 0;
                long insn = 0;
                if (lastStep != null) {
                    pc = lastStep.getPC();
                    insn = lastStep.getStep();
                }
                long addr = memevent.getAddress();
                if (memevent.getSize() <= 8) {
                    long value = memevent.getValue();
                    memory.write(addr, (byte) memevent.getSize(), value, pc, insn, node);
                } else if (memevent.getSize() == 16) {
                    Vector128 value = memevent.getVector();
                    memory.write(addr, (byte) 8, value.getI64(1), pc, insn, node);
                    memory.write(addr + 8, (byte) 8, value.getI64(0), pc, insn, node);
                } else {
                    throw new AssertionError("unknown size: " + memevent.getSize());
                }
            } else { /* read */
                long pc = 0;
                long insn = 0;
                if (lastStep != null) {
                    pc = lastStep.getPC();
                    insn = lastStep.getStep();
                }
                long addr = memevent.getAddress();
                if (memevent.getSize() <= 8) {
                    memory.read(addr, (byte) memevent.getSize(), pc, insn, node);
                } else if (memevent.getSize() == 16) {
                    memory.read(addr, (byte) 8, pc, insn, node);
                    memory.read(addr + 8, (byte) 8, pc, insn, node);
                } else {
                    throw new AssertionError("unknown size: " + memevent.getSize());
                }
            }
        } else if (event instanceof MemoryDumpEvent) {
            MemoryDumpEvent dump = (MemoryDumpEvent) event;
            long pc = 0;
            long insn = 0;
            if (lastStep != null) {
                pc = lastStep.getPC();
                insn = lastStep.getStep();
            }
            long addr = dump.getAddress();
            byte[] data = dump.getData();
            int i;
            for (i = 0; i < data.length - 7; i += 8) {
                long value = Endianess.get64bitLE(data, i);
                memory.write(addr + i, (byte) 8, value, pc, insn, node);
            }
            for (; i < data.length; i++) {
                memory.write(addr + i, (byte) 1, data[i], pc, insn, node);
            }
        } else if (event instanceof BrkEvent) {
            BrkEvent brk = (BrkEvent) event;
            long newbrk = brk.getResult();
            long pc = 0;
            long insn = 0;
            if (lastStep != null) {
                pc = lastStep.getPC();
                insn = lastStep.getStep();
            }
            memory.brk(newbrk, pc, insn, node);
        }
    }

    public void finish(BlockNode root) {
        add(root);

        StepEvent first = root.getFirstStep();
        if (symbols.get(first.getPC()) == null) {
            symbols.addSubroutine(first.getPC(), "_start");
            symbols.visit(root.getFirstNode());
        }

        // set default signatures for well-known symbols
        for (ComputedSymbol sym : symbols.getSymbols()) {
            switch (sym.name) {
                case "main":
                    sym.prototype = new Prototype(new Type(DataType.S32),
                                    Arrays.asList(new Type(DataType.S32),
                                                    new Type(new Type(DataType.STRING)),
                                                    new Type(new Type(DataType.STRING))));
                    break;
            }
        }

        log.log(Levels.INFO, "The trace contains " + steps + " steps");
        memory.printStats();
    }

    public SymbolTable getComputedSymbolTable() {
        return symbols;
    }

    public SymbolResolver getSymbolResolver() {
        return augmentedResolver;
    }

    public MappedFiles getMappedFiles() {
        return new MappedFiles(mappedFiles);
    }

    public MemoryTrace getMemoryTrace() {
        return memory;
    }

    public long getStepCount() {
        return steps;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
