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
package org.graalvm.vm.x86.node.flow;

import static org.graalvm.vm.x86.Options.getBoolean;
import static org.graalvm.vm.x86.util.Debug.printf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.posix.api.ProcessExitException;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.x86.AMD64Context;
import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.CpuRuntimeException;
import org.graalvm.vm.x86.Options;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.isa.IndirectException;
import org.graalvm.vm.x86.isa.Register;
import org.graalvm.vm.x86.isa.ReturnException;
import org.graalvm.vm.x86.isa.instruction.Call;
import org.graalvm.vm.x86.isa.instruction.Rdtsc;
import org.graalvm.vm.x86.isa.instruction.Rep;
import org.graalvm.vm.x86.node.AMD64Node;
import org.graalvm.vm.x86.node.ReadNode;
import org.graalvm.vm.x86.node.RegisterReadNode;
import org.graalvm.vm.x86.node.RegisterWriteNode;
import org.graalvm.vm.x86.node.WriteNode;
import org.graalvm.vm.x86.node.debug.TraceStateNode;
import org.graalvm.vm.x86.node.debug.trace.ExecutionTraceWriter;
import org.graalvm.vm.x86.node.init.CopyToCpuStateNode;
import org.graalvm.vm.x86.posix.InteropException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.profiles.BranchProfile;

public class AMD64BasicBlock extends AMD64Node {
    private static final boolean DEBUG = getBoolean(Options.DEBUG_EXEC);
    private static final boolean DEBUG_TRACE = getBoolean(Options.DEBUG_EXEC_TRACE);
    private final boolean EXEC_TRACE = getBoolean(Options.EXEC_TRACE);

    @CompilationFinal private static boolean DEBUG_COMPILER = false;

    @CompilationFinal private SymbolResolver symbolResolver;
    @CompilationFinal private ContextReference<AMD64Context> ctxref;

    @CompilationFinal ExecutionTraceWriter traceWriter;
    @Child private CopyToCpuStateNode readCpuState;
    @Child private TraceStateNode traceState;

    @Child private ReadNode readInstructionCount;
    @Child private WriteNode writeInstructionCount;

    @Children private AMD64Instruction[] instructions;
    @CompilationFinal(dimensions = 1) private AMD64BasicBlock[] successors;

    @CompilationFinal private int instructionCount;

    @CompilationFinal public long index;

    public final boolean indirect;
    @CompilationFinal public long pc1;
    @CompilationFinal public long pc2;

    @CompilationFinal public int successor1;
    @CompilationFinal public int successor2;

    private final BranchProfile profile = BranchProfile.create();
    private final BranchProfile exceptionProfile = BranchProfile.create();

    public AMD64BasicBlock(AMD64Instruction[] instructions) {
        this(instructions, true);
    }

    public AMD64BasicBlock(AMD64Instruction[] instructions, boolean createChildren) {
        assert instructions.length > 0;
        this.instructions = instructions;
        if (DEBUG_COMPILER) {
            printf("0x%016x: SIZE=%d\n", instructions[0].getPC(), instructions.length);
        }
        AMD64Instruction insn = getLastInstruction();
        indirect = insn.isControlFlow() && insn.getBTA() == null;
        if (insn.isControlFlow() && !indirect) {
            long[] bta = insn.getBTA();
            assert bta != null;
            assert bta.length > 0;
            assert bta.length <= 2;
            pc1 = bta[0];
            if (bta.length > 1) {
                pc2 = bta[1];
            } else {
                pc2 = pc1;
            }
        } else {
            pc1 = insn.next();
            pc2 = insn.next();
        }

        if (createChildren) {
            // don't create children in certain unit tests
            createChildren();
        }
    }

    private void createChildren() {
        AMD64Context ctx = getContextReference().get(this);
        ArchitecturalState state = ctx.getState();
        instructionCount = state.getInstructionCount();
        readInstructionCount = new RegisterReadNode(instructionCount);
        writeInstructionCount = new RegisterWriteNode(instructionCount);
        if (EXEC_TRACE) {
            traceState = new TraceStateNode(ctx);
        }
    }

    public boolean isIndirect() {
        return indirect;
    }

    public boolean contains(long address) {
        for (AMD64Instruction insn : instructions) {
            if (insn.getPC() == address) {
                return true;
            }
        }
        return false;
    }

    public void setSuccessors(AMD64BasicBlock[] successors) {
        this.successors = successors;
    }

    public AMD64BasicBlock[] getSuccessors() {
        return successors;
    }

    @ExplodeLoop(kind = LoopExplosionKind.FULL_EXPLODE_UNTIL_RETURN)
    public AMD64BasicBlock getSuccessor(long pc) {
        if (successors == null) {
            return null;
        }
        for (AMD64BasicBlock block : successors) {
            if (block.getAddress() == pc) {
                CompilerAsserts.partialEvaluationConstant(block);
                return block;
            }
        }
        return null;
    }

    public long[] getBTA() {
        return instructions[instructions.length - 1].getBTA();
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getIndex() {
        return index;
    }

    public long getAddress() {
        CompilerAsserts.partialEvaluationConstant(this);
        CompilerAsserts.partialEvaluationConstant(instructions);
        CompilerAsserts.partialEvaluationConstant(instructions[0]);
        long addr = instructions[0].getPC();
        CompilerAsserts.partialEvaluationConstant(addr);
        return addr;
    }

    public int getInstructionCount() {
        return instructions.length;
    }

    public AMD64Instruction getInstruction(long pc) {
        if (!contains(pc)) {
            return null;
        }
        for (AMD64Instruction insn : instructions) {
            if (insn.getPC() == pc) {
                return insn;
            }
        }
        return null;
    }

    @TruffleBoundary
    private void writeTrace(CpuState state, AMD64Instruction insn) {
        traceWriter.step(state, insn);
    }

    @TruffleBoundary
    private void flushTrace() {
        traceWriter.flush();
    }

    private void debug(VirtualFrame frame, long pc, AMD64Instruction insn) {
        if (DEBUG && DEBUG_TRACE) {
            if (readCpuState == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                ctxref = getContextReference();
                AMD64Context ctx = ctxref.get(this);
                readCpuState = insert(new CopyToCpuStateNode());
                symbolResolver = ctx.getSymbolResolver();
                traceWriter = ctx.getTraceWriter();
            }
            writeTrace(readCpuState.execute(frame, pc), insn);
        }
    }

    private void updateInstructionCount(VirtualFrame frame, long n) {
        long cnt = readInstructionCount.executeI64(frame);
        cnt += n;
        writeInstructionCount.executeI64(frame, cnt);
    }

    @TruffleBoundary
    private void dump() {
        System.out.println(this);
    }

    public boolean executeBlock(VirtualFrame frame) {
        long pc = execute(frame);
        if (isIndirect()) {
            throw new IndirectException(pc);
        } else if (pc == pc1) {
            return true;
        } else if (pc == pc2) {
            return false;
        } else {
            CompilerDirectives.transferToInterpreter();
            throw new AssertionError();
        }
    }

    @ExplodeLoop
    public long execute(VirtualFrame frame) {
        profile.enter();
        if (DEBUG_COMPILER) {
            if (CompilerDirectives.inInterpreter()) {
                printf("0x%016x: interpreter (%d insns)\n", instructions[0].getPC(), instructions.length);
            } else {
                printf("0x%016x: compiled code (%d insns)\n", instructions[0].getPC(), instructions.length);
            }
        }
        long pc = getAddress();
        long n = 0;
        CompilerAsserts.partialEvaluationConstant(pc);
        try {
            for (AMD64Instruction insn : instructions) {
                if (DEBUG || EXEC_TRACE) {
                    updateInstructionCount(frame, n);
                    n = 0;
                }
                if (DEBUG) {
                    debug(frame, pc, insn);
                }
                if (EXEC_TRACE) {
                    traceState.execute(frame, pc, insn);
                }
                // rdtsc/call needs current instruction count
                if (insn instanceof Rdtsc || insn instanceof Call) {
                    updateInstructionCount(frame, n);
                    n = 0;
                }
                pc = insn.executeInstruction(frame);
                if (!(insn instanceof Rep)) {
                    n++;
                }
            }
        } catch (ProcessExitException | ReturnException | InteropException e) {
            exceptionProfile.enter();
            updateInstructionCount(frame, n);
            throw e;
        } catch (Throwable t) {
            updateInstructionCount(frame, n);
            CompilerDirectives.transferToInterpreter();
            if (DEBUG && DEBUG_TRACE) {
                flushTrace();
            }
            throw new CpuRuntimeException(pc, t);
        }
        updateInstructionCount(frame, n);
        return pc;
    }

    public AMD64Instruction getLastInstruction() {
        return instructions[instructions.length - 1];
    }

    public AMD64BasicBlock split(long address) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        assert instructions.length > 1;
        assert address != getAddress();
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i].getPC() == address) {
                AMD64Instruction[] head = Arrays.copyOf(instructions, i);
                AMD64Instruction[] tail = new AMD64Instruction[instructions.length - i];
                System.arraycopy(instructions, i, tail, 0, tail.length);
                assert head.length + tail.length == instructions.length;
                assert head.length > 0;
                assert tail.length > 0;
                instructions = head;
                AMD64BasicBlock result = new AMD64BasicBlock(tail);
                result.setSuccessors(successors);
                result.successor1 = successor1;
                result.successor2 = successor2;
                pc1 = result.getAddress();
                pc2 = result.getAddress();
                successor1 = -1;
                successor2 = -1;
                successors = new AMD64BasicBlock[]{result};
                return result;
            }
        }
        return null;
    }

    public Set<Register> getGPRReads() {
        Set<Register> written = new HashSet<>();
        return getGPRReads(written);
    }

    public Set<Register> getGPRReads(Set<Register> written) {
        CompilerAsserts.neverPartOfCompilation();
        Set<Register> regs = new HashSet<>();
        for (AMD64Instruction insn : instructions) {
            Register[] read = insn.getUsedGPRRead();
            Register[] write = insn.getUsedGPRWrite();
            for (Register r : read) {
                if (!written.contains(r)) {
                    regs.add(r);
                }
            }
            for (Register r : write) {
                written.add(r);
            }
        }
        if (EXEC_TRACE) {
            Register[] read = traceState.getUsedGPRRead();
            for (Register r : read) {
                regs.add(r);
            }
        }
        return regs;
    }

    public Set<Register> getGPRWrites() {
        CompilerAsserts.neverPartOfCompilation();
        Set<Register> regs = new HashSet<>();
        for (AMD64Instruction insn : instructions) {
            Register[] write = insn.getUsedGPRWrite();
            for (Register r : write) {
                regs.add(r);
            }
        }
        return regs;
    }

    public Set<Integer> getAVXReads() {
        Set<Integer> written = new HashSet<>();
        return getAVXReads(written);
    }

    public Set<Integer> getAVXReads(Set<Integer> written) {
        CompilerAsserts.neverPartOfCompilation();
        Set<Integer> regs = new HashSet<>();
        for (AMD64Instruction insn : instructions) {
            int[] read = insn.getUsedAVXRead();
            int[] write = insn.getUsedAVXWrite();
            for (Integer r : read) {
                if (!written.contains(r)) {
                    regs.add(r);
                }
            }
            for (int r : write) {
                written.add(r);
            }
        }
        return regs;
    }

    public Set<Integer> getAVXWrites() {
        CompilerAsserts.neverPartOfCompilation();
        Set<Integer> regs = new HashSet<>();
        for (AMD64Instruction insn : instructions) {
            int[] write = insn.getUsedAVXWrite();
            for (Integer r : write) {
                regs.add(r);
            }
        }
        return regs;
    }

    @Override
    public String toString() {
        CompilerAsserts.neverPartOfCompilation();
        StringBuilder buf = new StringBuilder(String.format("%016x:\n", instructions[0].getPC()));
        for (AMD64Instruction insn : instructions) {
            buf.append(insn.getDisassembly()).append('\n');
        }
        return buf.toString();
    }
}
