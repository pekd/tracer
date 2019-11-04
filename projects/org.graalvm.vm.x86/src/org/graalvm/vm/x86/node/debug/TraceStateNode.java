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
package org.graalvm.vm.x86.node.debug;

import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.x86.AMD64Context;
import org.graalvm.vm.x86.el.ast.BooleanExpression;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.isa.Register;
import org.graalvm.vm.x86.node.AMD64Node;
import org.graalvm.vm.x86.node.debug.trace.ExecutionTraceWriter;
import org.graalvm.vm.x86.node.init.CopyToCpuStateNode;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;

public class TraceStateNode extends AMD64Node {
    @Child private CopyToCpuStateNode read = new CopyToCpuStateNode();
    private final FrameSlot cpuStateSlot;
    private final FrameSlot gprMaskSlot;
    private final FrameSlot avxMaskSlot;
    private final ExecutionTraceWriter traceWriter;

    private final FrameSlot trace;

    @Child private BooleanExpression tron;
    @Child private BooleanExpression troff;

    private final ContextReference<AMD64Context> ctxref;

    public TraceStateNode(AMD64Context ctx) {
        cpuStateSlot = ctx.getCpuState();
        gprMaskSlot = ctx.getGPRMask();
        avxMaskSlot = ctx.getAVXMask();
        traceWriter = ctx.getTraceWriter();

        if (ctx.getTron() != null) {
            tron = ctx.getTron().clone();
        } else {
            tron = null;
        }
        if (ctx.getTroff() != null) {
            troff = ctx.getTroff().clone();
        } else {
            troff = null;
        }
        trace = ctx.getTrace();

        ctxref = getContextReference();
    }

    public Register[] getUsedGPRRead() {
        CompilerAsserts.neverPartOfCompilation();
        Set<Register> regs = new HashSet<>();
        Register[] r1 = tron != null ? tron.getUsedGPRRead() : new Register[0];
        Register[] r2 = troff != null ? troff.getUsedGPRRead() : new Register[0];
        for (Register r : r1) {
            regs.add(r);
        }
        for (Register r : r2) {
            regs.add(r);
        }
        return regs.toArray(new Register[regs.size()]);
    }

    private CpuState getState(VirtualFrame frame, long pc) {
        CompilerAsserts.partialEvaluationConstant(pc);
        CompilerAsserts.partialEvaluationConstant(gprMaskSlot);
        CompilerAsserts.partialEvaluationConstant(avxMaskSlot);
        CompilerAsserts.partialEvaluationConstant(cpuStateSlot);

        if (gprMaskSlot != null) {
            boolean[] gprMask = (boolean[]) FrameUtil.getObjectSafe(frame, gprMaskSlot);
            boolean[] avxMask = (boolean[]) FrameUtil.getObjectSafe(frame, avxMaskSlot);

            // CompilerAsserts.partialEvaluationConstant(gprMask);
            // CompilerAsserts.partialEvaluationConstant(avxMask);

            CpuState initialState = (CpuState) FrameUtil.getObjectSafe(frame, cpuStateSlot);
            if (gprMask != null) {
                return read.executeDynamic(frame, pc, initialState.clone(), gprMask, avxMask);
            } else {
                return read.execute(frame, pc);
            }
        } else {
            return read.execute(frame, pc);
        }
    }

    @TruffleBoundary
    private void writeTrace(CpuState state, AMD64Instruction insn) {
        traceWriter.step(state, insn);
    }

    public void execute(VirtualFrame frame, long pc, AMD64Instruction insn) {
        boolean record = true;
        if (tron != null) {
            record = FrameUtil.getBooleanSafe(frame, trace);
            if (record) {
                if (troff != null && troff.execute(frame, pc)) {
                    CompilerDirectives.transferToInterpreter();
                    frame.setBoolean(trace, false);
                    ctxref.get().setTraceStatus(false);
                }
            } else {
                if (tron != null) {
                    record = tron.execute(frame, pc);
                } else {
                    record = true;
                }
                if (record) {
                    CompilerDirectives.transferToInterpreter();
                    frame.setBoolean(trace, true);
                    ctxref.get().setTraceStatus(true);
                }
            }
        }

        if (record) {
            CpuState state = getState(frame, pc);
            writeTrace(state, insn);
        }
    }
}
