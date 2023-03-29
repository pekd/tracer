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
package org.graalvm.vm.x86;

import java.util.Collections;
import java.util.NavigableMap;

import org.graalvm.vm.memory.VirtualMemory;
import org.graalvm.vm.posix.api.Errno;
import org.graalvm.vm.posix.api.Stack;
import org.graalvm.vm.posix.elf.DefaultSymbolResolver;
import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.x86.el.ast.BooleanExpression;
import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.node.debug.trace.ExecutionTraceWriter;
import org.graalvm.vm.x86.node.debug.trace.LogStreamHandler;
import org.graalvm.vm.x86.node.debug.trace.MemoryAccessTracer;
import org.graalvm.vm.x86.node.debug.trace.TraceStatus;
import org.graalvm.vm.x86.node.flow.TraceRegistry;
import org.graalvm.vm.x86.posix.PosixEnvironment;
import org.graalvm.vm.x86.posix.SyscallException;
import org.graalvm.vm.x86.substitution.SubstitutionRegistry;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;

public class AMD64Context implements TraceStatus {
    private static final String ARCH_NAME = "x86_64";
    private static final String[] REGISTER_NAMES = {"rax", "rcx", "rdx", "rbx", "rsp", "rbp", "rsi", "rdi", "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15"};

    private static final ContextReference<AMD64Context> CTXREF = ContextReference.create(AMD64Language.class);

    private Env env;

    private final VirtualMemory memory;
    private final PosixEnvironment posix;
    private String[] args;

    private static final FrameDescriptor frameDescriptor;
    private static final int[] gpr;
    private static final int[] zmm;
    private static final int[] xmm;
    private static final int[] xmmF32;
    private static final int[] xmmF64;
    private static final int[] xmmType;
    private static final int fs;
    private static final int gs;
    private static final int pc;
    private static final int cf;
    private static final int pf;
    private static final int af;
    private static final int zf;
    private static final int sf;
    private static final int df;
    private static final int of;
    private static final int ac;
    private static final int id;

    private static final int instructionCount;

    private static final int cpuState;
    private static final int dispatchCpuState;
    private static final int dispatchTrace;
    private static final int gprMask;
    private static final int avxMask;

    private final ArchitecturalState state;

    private NavigableMap<Long, Symbol> symbols;
    private SymbolResolver symbolResolver;

    private final TraceRegistry traces;
    private final SubstitutionRegistry substitutions;

    private CpuState snapshot;
    private long returnAddress;
    private long scratchMemory;
    private long callbacks;

    private final ExecutionTraceWriter traceWriter;
    private final LogStreamHandler logHandler;

    private InteropCallback interopCallback;
    private InteropFunctionPointers interopPointers;

    @CompilationFinal private CallTarget interpreterMain;

    private final Assumption singleThreadedAssumption;

    private static final int trace;
    @CompilationFinal private BooleanExpression tron;
    @CompilationFinal private BooleanExpression troff;

    private boolean traceStatus;

    static {
        FrameDescriptor.Builder fd = FrameDescriptor.newBuilder();

        assert REGISTER_NAMES.length == 16;
        gpr = new int[REGISTER_NAMES.length];
        for (int i = 0; i < REGISTER_NAMES.length; i++) {
            gpr[i] = fd.addSlot(FrameSlotKind.Long, REGISTER_NAMES[i], null);
        }

        zmm = new int[32];
        xmm = new int[32];
        xmmF32 = new int[32];
        xmmF64 = new int[32];
        xmmType = new int[32];
        for (int i = 0; i < zmm.length; i++) {
            zmm[i] = fd.addSlot(FrameSlotKind.Object, "zmm" + i, null);
            xmm[i] = fd.addSlot(FrameSlotKind.Object, "xmm" + i, null);
            xmmF32[i] = fd.addSlot(FrameSlotKind.Float, "xmm" + i + "F32", null);
            xmmF64[i] = fd.addSlot(FrameSlotKind.Double, "xmm" + i + "F64", null);
            xmmType[i] = fd.addSlot(FrameSlotKind.Int, "xmm" + i + "Type", null);
        }

        fs = fd.addSlot(FrameSlotKind.Long, "fs", null);
        gs = fd.addSlot(FrameSlotKind.Long, "gs", null);
        pc = fd.addSlot(FrameSlotKind.Long, "rip", null);
        cf = fd.addSlot(FrameSlotKind.Boolean, "cf", null);
        pf = fd.addSlot(FrameSlotKind.Boolean, "pf", null);
        af = fd.addSlot(FrameSlotKind.Boolean, "af", null);
        zf = fd.addSlot(FrameSlotKind.Boolean, "zf", null);
        sf = fd.addSlot(FrameSlotKind.Boolean, "sf", null);
        df = fd.addSlot(FrameSlotKind.Boolean, "df", null);
        of = fd.addSlot(FrameSlotKind.Boolean, "of", null);
        ac = fd.addSlot(FrameSlotKind.Boolean, "ac", null);
        id = fd.addSlot(FrameSlotKind.Boolean, "id", null);
        instructionCount = fd.addSlot(FrameSlotKind.Boolean, "instructionCount", null);

        trace = fd.addSlot(FrameSlotKind.Boolean, "trace", null);

        cpuState = fd.addSlot(FrameSlotKind.Boolean, "cpustate", null);
        dispatchCpuState = fd.addSlot(FrameSlotKind.Boolean, "dispatchCpuState", null);
        dispatchTrace = fd.addSlot(FrameSlotKind.Boolean, "dispatchTrace", null);

        gprMask = fd.addSlot(FrameSlotKind.Boolean, "gprmask", null);
        avxMask = fd.addSlot(FrameSlotKind.Boolean, "avxmask", null);

        frameDescriptor = fd.build();
    }

    public AMD64Context(TruffleLanguage<AMD64Context> language, Env env) {
        this(language, env, null, null);
    }

    public AMD64Context(TruffleLanguage<AMD64Context> language, Env env, ExecutionTraceWriter traceWriter, LogStreamHandler logHandler) {
        this.env = env;
        this.traceWriter = traceWriter;
        this.logHandler = logHandler;
        memory = VirtualMemory.create();

        if (traceWriter != null) {
            MemoryAccessTracer memoryTracer = new MemoryAccessTracer(traceWriter, Options.getString(Options.EXEC_TRON) != null ? this : null);
            memory.setAccessLogger(memoryTracer);
        }

        posix = new PosixEnvironment(memory, ARCH_NAME, traceWriter);
        posix.setStandardIO(env.in(), env.out(), env.err());
        args = env.getApplicationArguments();

        singleThreadedAssumption = Truffle.getRuntime().createAssumption("single threaded");
        traces = new TraceRegistry(language, frameDescriptor);
        substitutions = new SubstitutionRegistry();
        state = new ArchitecturalState(this);
        symbols = Collections.emptyNavigableMap();
        symbolResolver = new DefaultSymbolResolver(symbols);
        scratchMemory = 0;
        traceStatus = true;
    }

    public static ContextReference<AMD64Context> getContextReference() {
        return CTXREF;
    }

    public void initialize() {
        traces.initialize(this);
    }

    public void patch(Env newEnv) {
        this.env = newEnv;
        posix.setStandardIO(newEnv.in(), newEnv.out(), newEnv.err());
        args = newEnv.getApplicationArguments();
    }

    public static FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public VirtualMemory getMemory() {
        return memory;
    }

    public PosixEnvironment getPosixEnvironment() {
        return posix;
    }

    @TruffleBoundary
    public void setSymbols(NavigableMap<Long, Symbol> symbols) {
        this.symbols = symbols;
        this.symbolResolver = new DefaultSymbolResolver(symbols);
    }

    public NavigableMap<Long, Symbol> getSymbols() {
        return symbols;
    }

    public String[] getArguments() {
        return args;
    }

    public int getGPR(int i) {
        return gpr[i];
    }

    public int getZMM(int i) {
        return zmm[i];
    }

    public int getFS() {
        return fs;
    }

    public int getGS() {
        return gs;
    }

    public int getPC() {
        return pc;
    }

    public int[] getGPRs() {
        return gpr;
    }

    public int[] getZMMs() {
        return zmm;
    }

    public int[] getXMMs() {
        return xmm;
    }

    public int[] getXMMF32() {
        return xmmF32;
    }

    public int[] getXMMF64() {
        return xmmF64;
    }

    public int[] getXMMType() {
        return xmmType;
    }

    public int getCF() {
        return cf;
    }

    public int getPF() {
        return pf;
    }

    public int getAF() {
        return af;
    }

    public int getZF() {
        return zf;
    }

    public int getSF() {
        return sf;
    }

    public int getDF() {
        return df;
    }

    public int getOF() {
        return of;
    }

    public int getAC() {
        return ac;
    }

    public int getID() {
        return id;
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public int getCpuState() {
        return cpuState;
    }

    public int getDispatchCpuState() {
        return dispatchCpuState;
    }

    public int getDispatchTrace() {
        return dispatchTrace;
    }

    public int getGPRMask() {
        return gprMask;
    }

    public int getAVXMask() {
        return avxMask;
    }

    public ArchitecturalState getState() {
        return state;
    }

    public SymbolResolver getSymbolResolver() {
        return symbolResolver;
    }

    public TraceRegistry getTraceRegistry() {
        return traces;
    }

    public SubstitutionRegistry getSubstitutionRegistry() {
        return substitutions;
    }

    public long getSigaltstack() {
        Stack stack = posix.getSigaltstack();
        if (stack == null) {
            return 0;
        } else {
            return stack.ss_sp + stack.ss_size;
        }
    }

    public void setStateSnapshot(CpuState state) {
        snapshot = state;
    }

    public CpuState getStateSnapshot() {
        return snapshot;
    }

    public void setReturnAddress(long address) {
        returnAddress = address;
    }

    public long getReturnAddress() {
        return returnAddress;
    }

    public void setScratchMemory(long address) {
        scratchMemory = address;
    }

    public long getScratchMemory() {
        return scratchMemory;
    }

    public void setCallbackMemory(long address) {
        callbacks = address;
    }

    public long getCallbackMemory() {
        return callbacks;
    }

    public ExecutionTraceWriter getTraceWriter() {
        return traceWriter;
    }

    LogStreamHandler getLogHandler() {
        return logHandler;
    }

    public void setInteropCallback(InteropCallback callback) {
        this.interopCallback = callback;
    }

    public void clearInteropCallback() {
        interopCallback = null;
    }

    public long interopCall(int nr, long a1, long a2, long a3, long a4, long a5, long a6) throws SyscallException {
        if (interopCallback == null) {
            throw new SyscallException(Errno.ENOSYS);
        } else {
            return interopCallback.call(nr, a1, a2, a3, a4, a5, a6);
        }
    }

    public long interopCall(int nr, long a1, long a2, long a3, long a4, long a5, long a6, long f1, long f2, long f3, long f4, long f5, long f6, long f7, long f8) throws SyscallException {
        if (interopCallback == null) {
            throw new SyscallException(Errno.ENOSYS);
        } else {
            return interopCallback.call(nr, a1, a2, a3, a4, a5, a6, f1, f2, f3, f4, f5, f6, f7, f8);
        }
    }

    public void setInteropFunctionPointers(InteropFunctionPointers pointers) {
        interopPointers = pointers;
    }

    public InteropFunctionPointers getInteropFunctionPointers() {
        return interopPointers;
    }

    public Thread createThread(int tid, Runnable runnable) {
        ThreadGroup group = posix.getThreadGroup();
        Thread thread = env.createThread(runnable, null, group);
        posix.addThread(tid, thread);
        return thread;
    }

    public void setInterpreter(CallTarget interpreter) {
        this.interpreterMain = interpreter;
    }

    public CallTarget getInterpreter() {
        return interpreterMain;
    }

    public Assumption getSingleThreadedAssumption() {
        return singleThreadedAssumption;
    }

    public void setTron(BooleanExpression tron) {
        this.tron = tron;
    }

    public BooleanExpression getTron() {
        return tron;
    }

    public void setTroff(BooleanExpression troff) {
        this.troff = troff;
    }

    public BooleanExpression getTroff() {
        return troff;
    }

    public int getTrace() {
        return trace;
    }

    @Override
    public boolean getTraceStatus() {
        return traceStatus;
    }

    @Override
    public void setTraceStatus(boolean status) {
        traceStatus = status;
    }
}
