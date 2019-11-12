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

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.NavigableMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.graalvm.vm.memory.vector.Vector128;
import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.util.io.BEOutputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.CpuState;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class ExecutionTraceWriter implements Closeable {
    private static final Logger log = Trace.create(ExecutionTraceWriter.class);

    private static final long STEP_THRESHOLD = 1000;

    private WordOutputStream out;
    private CpuState lastState;
    private long steps;

    public ExecutionTraceWriter(File out) throws IOException {
        this(new BufferedOutputStream(new FileOutputStream(out)));
    }

    public ExecutionTraceWriter(OutputStream out) {
        this.out = new BEOutputStream(out);
        steps = 0;
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            Record record = new EofRecord();
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing eof event: " + e.getMessage(), e);
        }
        out.close();
    }

    @TruffleBoundary
    public synchronized void step(CpuState state, AMD64Instruction insn) {
        CpuStateRecord stateRecord;
        if (steps > STEP_THRESHOLD || lastState == null) {
            stateRecord = new FullCpuStateRecord(state);
            steps = 0;
        } else {
            stateRecord = new DeltaCpuStateRecord(lastState, state);
            steps++;
        }
        lastState = state;
        StepRecord record = new StepRecord(insn.getBytes(), stateRecord);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing cpu step event: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void symbolTable(long loadBias, String filename, long address, long size, NavigableMap<Long, Symbol> symbols) {
        SymbolTableRecord record = new SymbolTableRecord(loadBias, filename, address, size, symbols);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing symbol table event: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void callArgs(long pc, String name, long[] args, byte[][] memory) {
        CallArgsRecord record = new CallArgsRecord(pc, name, args, memory);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing call args: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void log(long seq, long time, int level, int threadID, String logger, String clazz, String method, String msg, Throwable throwable) {
        SystemLogRecord record = new SystemLogRecord(seq, time, level, threadID, logger, clazz, method, msg, throwable);
        try {
            record.write(out);
        } catch (IOException e) {
            // don't log anything here because this method is called from a log handler!
        }
    }

    @TruffleBoundary
    public synchronized void memoryAccess(long address, boolean write, int size) {
        MemoryEventRecord record = new MemoryEventRecord(address, write, size);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing memory access: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void memoryAccess(long address, boolean write, int size, long value) {
        MemoryEventRecord record = new MemoryEventRecord(address, write, size, value);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing memory access: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void memoryAccess(long address, boolean write, int size, Vector128 value) {
        MemoryEventRecord record = new MemoryEventRecord(address, write, size, value);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing memory access: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void memoryDump(long address, byte[] data) {
        MemoryDumpRecord record = new MemoryDumpRecord(address, data);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing memory dump: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void mmap(long addr, long len, int prot, int flags, int fildes, long off, long result, String filename, byte[] data) {
        MmapRecord record = new MmapRecord(addr, len, prot, flags, fildes, off, filename, result);
        record.setData(data);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing mmap: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void munmap(long addr, long len, int result) {
        MunmapRecord record = new MunmapRecord(addr, len, result);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing munmap: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void mprotect(long addr, long len, int prot, int result) {
        MprotectRecord record = new MprotectRecord(addr, len, prot, result);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing mprotect: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void brk(long addr, long result) {
        BrkRecord record = new BrkRecord(addr, result);
        try {
            record.write(out);
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while writing brk: " + e.getMessage(), e);
        }
    }

    @TruffleBoundary
    public synchronized void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            log.log(Level.WARNING, "Error while flushing trace data: " + e.getMessage(), e);
        }
    }
}
