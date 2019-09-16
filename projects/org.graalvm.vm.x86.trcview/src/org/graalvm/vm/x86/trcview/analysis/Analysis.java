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

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.posix.elf.DefaultSymbolResolver;
import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.isa.AMD64InstructionQuickInfo;
import org.graalvm.vm.x86.node.debug.trace.BrkRecord;
import org.graalvm.vm.x86.node.debug.trace.MemoryEventRecord;
import org.graalvm.vm.x86.node.debug.trace.MmapRecord;
import org.graalvm.vm.x86.node.debug.trace.Record;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.node.debug.trace.SymbolTableRecord;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.x86.trcview.analysis.type.DataType;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.analysis.type.Type;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.io.RecordNode;

public class Analysis {
    private static final Logger log = Trace.create(Analysis.class);

    private SymbolTable symbols;
    private StepRecord lastStep;

    private NavigableMap<Long, Symbol> symbolTable;
    private NavigableMap<Long, MappedFile> mappedFiles;
    private SymbolResolver resolver;
    private SymbolResolver augmentedResolver;

    private long steps;

    private MemoryTrace memory;

    public Analysis() {
        symbols = new SymbolTable();
        symbolTable = new TreeMap<>();
        mappedFiles = new TreeMap<>();
        resolver = new DefaultSymbolResolver(symbolTable);
        augmentedResolver = new AugmentingSymbolResolver(resolver, symbols);
        memory = new MemoryTrace();
    }

    public void start() {
        lastStep = null;
        steps = 0;
    }

    public void process(Record record, Node node) {
        if (record instanceof StepRecord) {
            steps++;
            StepRecord step = (StepRecord) record;
            if (lastStep != null) {
                long pc = step.getPC();
                Symbol sym;
                switch (AMD64InstructionQuickInfo.getType(lastStep.getMachinecode())) {
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
                }
            }
            lastStep = step;
        } else if (record instanceof SymbolTableRecord) {
            SymbolTableRecord symtab = (SymbolTableRecord) record;
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
        } else if (record instanceof MmapRecord) {
            MmapRecord mmap = (MmapRecord) record;
            mappedFiles.put(mmap.getResult(), new MappedFile(mmap.getFileDescriptor(), mmap.getResult(), mmap.getLength(), mmap.getOffset(), mmap.getFilename(), -1));
            long pc = 0;
            long insn = 0;
            if (lastStep != null) {
                pc = lastStep.getPC();
                insn = lastStep.getInstructionCount();
            }
            if (mmap.getResult() != -1) {
                if (mmap.getData() != null) {
                    memory.mmap(mmap.getResult(), mmap.getLength(), mmap.getData(), pc, insn, node);
                } else {
                    memory.mmap(mmap.getResult(), mmap.getLength(), pc, insn, node);
                }
            }
        } else if (record instanceof MemoryEventRecord) {
            MemoryEventRecord event = (MemoryEventRecord) record;
            if (event.isWrite()) {
                long pc = 0;
                long insn = 0;
                if (lastStep != null) {
                    pc = lastStep.getPC();
                    insn = lastStep.getInstructionCount();
                }
                long addr = event.getAddress();
                if (event.getSize() <= 8) {
                    long value = event.getValue();
                    memory.write(addr, (byte) event.getSize(), value, pc, insn, node);
                } else if (event.getSize() == 16) {
                    Vector128 value = event.getVector();
                    memory.write(addr, (byte) 8, value.getI64(1), pc, insn, node);
                    memory.write(addr + 8, (byte) 8, value.getI64(0), pc, insn, node);
                } else {
                    throw new AssertionError("unknown size: " + event.getSize());
                }
            }
        } else if (record instanceof BrkRecord) {
            BrkRecord brk = (BrkRecord) record;
            long newbrk = brk.getResult();
            long pc = 0;
            long insn = 0;
            if (lastStep != null) {
                pc = lastStep.getPC();
                insn = lastStep.getInstructionCount();
            }
            memory.brk(newbrk, pc, insn, node);
        }
    }

    private void resolveCalls(BlockNode root) {
        StepRecord first = root.getFirstStep();
        if (symbols.get(first.getPC()) == null) {
            symbols.addSubroutine(first.getPC(), "_start");
        }
        for (Node node : root.getNodes()) {
            if (node instanceof BlockNode) {
                BlockNode block = (BlockNode) node;
                resolveCalls(block);
            } else if (node instanceof RecordNode) {
                RecordNode record = (RecordNode) node;
                symbols.visit(record);
            }
        }

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
    }

    public void finish(BlockNode root) {
        resolveCalls(root);
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
}
