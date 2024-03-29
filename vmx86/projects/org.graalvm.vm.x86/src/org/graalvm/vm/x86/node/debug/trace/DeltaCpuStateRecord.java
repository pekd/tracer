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

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.isa.CpuState;

public class DeltaCpuStateRecord extends CpuStateRecord {
    public static final byte ID = 0x02;

    private CpuState current;

    private long deltaId;
    private long[] deltaValue;

    private long pc;
    private long instructionCount;

    public DeltaCpuStateRecord() {
        super(ID);
    }

    private DeltaCpuStateRecord(byte[] machinecode, CpuState lastState, CpuState current) {
        super(ID, machinecode);
        this.current = current;
        setLastState(lastState);
    }

    public static CpuStateRecord get(byte[] machinecode, CpuState lastState, CpuState current) {
        DeltaCpuStateRecord delta = new DeltaCpuStateRecord(machinecode, lastState, current);
        return delta.computeDelta(current);
    }

    public long getDelta() {
        return deltaId;
    }

    public boolean getDeltaId(int id) {
        return (deltaId & (1L << id)) != 0;
    }

    public boolean getDeltaXMM(int xmm) {
        return getDeltaId(ID_RFL + 2 * xmm + 1) | getDeltaId(ID_RFL + 2 * xmm + 2);
    }

    private void setDeltaId(int id) {
        deltaId |= 1L << id;
    }

    private CpuStateRecord computeDelta(CpuState state) {
        // compute number of differences
        CpuState lastState = getLastState();
        clearLastState();
        int cnt = 0;
        if (lastState.rax != state.rax) {
            cnt++;
        }
        if (lastState.rcx != state.rcx) {
            cnt++;
        }
        if (lastState.rdx != state.rdx) {
            cnt++;
        }
        if (lastState.rbx != state.rbx) {
            cnt++;
        }
        if (lastState.rsp != state.rsp) {
            cnt++;
        }
        if (lastState.rbp != state.rbp) {
            cnt++;
        }
        if (lastState.rsi != state.rsi) {
            cnt++;
        }
        if (lastState.rdi != state.rdi) {
            cnt++;
        }
        if (lastState.r8 != state.r8) {
            cnt++;
        }
        if (lastState.r9 != state.r9) {
            cnt++;
        }
        if (lastState.r10 != state.r10) {
            cnt++;
        }
        if (lastState.r11 != state.r11) {
            cnt++;
        }
        if (lastState.r12 != state.r12) {
            cnt++;
        }
        if (lastState.r13 != state.r13) {
            cnt++;
        }
        if (lastState.r14 != state.r14) {
            cnt++;
        }
        if (lastState.r15 != state.r15) {
            cnt++;
        }
        if (lastState.fs != state.fs) {
            cnt++;
        }
        if (lastState.gs != state.gs) {
            cnt++;
        }
        if (lastState.getRFL() != state.getRFL()) {
            cnt++;
        }

        int tmp = cnt;

        for (int i = 0; i < 16; i++) {
            if (!lastState.xmm[i].equals(state.xmm[i])) {
                cnt += 2;
            }
        }

        if (tmp == cnt) {
            if (cnt == 0) {
                return new TinyDeltaCpuStateRecord(getMachinecode(), state);
            } else if (cnt == 1) {
                return new SmallDeltaCpuStateRecord(getMachinecode(), lastState, state);
            }
        }

        // compute difference data
        deltaId = 0;
        deltaValue = new long[cnt];

        int pos = 0;
        if (lastState.rax != state.rax) {
            setDeltaId(ID_RAX);
            deltaValue[pos] = state.rax;
            pos++;
        }
        if (lastState.rcx != state.rcx) {
            setDeltaId(ID_RCX);
            deltaValue[pos] = state.rcx;
            pos++;
        }
        if (lastState.rdx != state.rdx) {
            setDeltaId(ID_RDX);
            deltaValue[pos] = state.rdx;
            pos++;
        }
        if (lastState.rbx != state.rbx) {
            setDeltaId(ID_RBX);
            deltaValue[pos] = state.rbx;
            pos++;
        }
        if (lastState.rsp != state.rsp) {
            setDeltaId(ID_RSP);
            deltaValue[pos] = state.rsp;
            pos++;
        }
        if (lastState.rbp != state.rbp) {
            setDeltaId(ID_RBP);
            deltaValue[pos] = state.rbp;
            pos++;
        }
        if (lastState.rsi != state.rsi) {
            setDeltaId(ID_RSI);
            deltaValue[pos] = state.rsi;
            pos++;
        }
        if (lastState.rdi != state.rdi) {
            setDeltaId(ID_RDI);
            deltaValue[pos] = state.rdi;
            pos++;
        }
        if (lastState.r8 != state.r8) {
            setDeltaId(ID_R8);
            deltaValue[pos] = state.r8;
            pos++;
        }
        if (lastState.r9 != state.r9) {
            setDeltaId(ID_R9);
            deltaValue[pos] = state.r9;
            pos++;
        }
        if (lastState.r10 != state.r10) {
            setDeltaId(ID_R10);
            deltaValue[pos] = state.r10;
            pos++;
        }
        if (lastState.r11 != state.r11) {
            setDeltaId(ID_R11);
            deltaValue[pos] = state.r11;
            pos++;
        }
        if (lastState.r12 != state.r12) {
            setDeltaId(ID_R12);
            deltaValue[pos] = state.r12;
            pos++;
        }
        if (lastState.r13 != state.r13) {
            setDeltaId(ID_R13);
            deltaValue[pos] = state.r13;
            pos++;
        }
        if (lastState.r14 != state.r14) {
            setDeltaId(ID_R14);
            deltaValue[pos] = state.r14;
            pos++;
        }
        if (lastState.r15 != state.r15) {
            setDeltaId(ID_R15);
            deltaValue[pos] = state.r15;
            pos++;
        }
        if (lastState.fs != state.fs) {
            setDeltaId(ID_FS);
            deltaValue[pos] = state.fs;
            pos++;
        }
        if (lastState.gs != state.gs) {
            setDeltaId(ID_GS);
            deltaValue[pos] = state.gs;
            pos++;
        }
        if (lastState.getRFL() != state.getRFL()) {
            setDeltaId(ID_RFL);
            deltaValue[pos] = state.getRFL();
            pos++;
        }
        for (int i = 0; i < 16; i++) {
            if (!lastState.xmm[i].equals(state.xmm[i])) {
                setDeltaId(19 + 2 * i);
                deltaValue[pos] = state.xmm[i].getI64(0);
                pos++;
                setDeltaId(20 + 2 * i);
                deltaValue[pos] = state.xmm[i].getI64(1);
                pos++;
            }
        }

        pc = state.rip;
        instructionCount = state.instructionCount;

        return this;
    }

    @Override
    public CpuState getState() {
        if (current != null) {
            return current;
        } else {
            // compute state
            CpuState state = getLastState().clone();
            state.rip = pc;
            state.instructionCount = instructionCount;
            clearLastState();
            int i = 0;
            if (getDeltaId(ID_RAX)) {
                state.rax = deltaValue[i++];
            }
            if (getDeltaId(ID_RCX)) {
                state.rcx = deltaValue[i++];
            }
            if (getDeltaId(ID_RDX)) {
                state.rdx = deltaValue[i++];
            }
            if (getDeltaId(ID_RBX)) {
                state.rbx = deltaValue[i++];
            }
            if (getDeltaId(ID_RSP)) {
                state.rsp = deltaValue[i++];
            }
            if (getDeltaId(ID_RBP)) {
                state.rbp = deltaValue[i++];
            }
            if (getDeltaId(ID_RSI)) {
                state.rsi = deltaValue[i++];
            }
            if (getDeltaId(ID_RDI)) {
                state.rdi = deltaValue[i++];
            }
            if (getDeltaId(ID_R8)) {
                state.r8 = deltaValue[i++];
            }
            if (getDeltaId(ID_R9)) {
                state.r9 = deltaValue[i++];
            }
            if (getDeltaId(ID_R10)) {
                state.r10 = deltaValue[i++];
            }
            if (getDeltaId(ID_R11)) {
                state.r11 = deltaValue[i++];
            }
            if (getDeltaId(ID_R12)) {
                state.r12 = deltaValue[i++];
            }
            if (getDeltaId(ID_R13)) {
                state.r13 = deltaValue[i++];
            }
            if (getDeltaId(ID_R14)) {
                state.r14 = deltaValue[i++];
            }
            if (getDeltaId(ID_R15)) {
                state.r15 = deltaValue[i++];
            }
            if (getDeltaId(ID_FS)) {
                state.fs = deltaValue[i++];
            }
            if (getDeltaId(ID_GS)) {
                state.gs = deltaValue[i++];
            }
            if (getDeltaId(ID_RFL)) {
                state.setRFL(deltaValue[i++]);
            }
            for (int n = 19; n < 64; n++) {
                if (getDeltaId(n)) {
                    if (n > 18 && n < (19 + 16 * 2)) {
                        int v = n - 19;
                        int reg = v / 2;
                        state.xmm[reg].setI64(v % 2 == 0 ? 0 : 1, deltaValue[i++]);
                    } else {
                        throw new RuntimeException("unknown id: " + n);
                    }
                }
            }
            current = state;
            return state;
        }
    }

    @Override
    public long getPC() {
        return pc;
    }

    @Override
    public long getInstructionCount() {
        return instructionCount;
    }

    @Override
    protected int getDataSize() {
        return 1 + deltaValue.length + 8 * deltaValue.length + 2 * 8 + super.getDataSize();
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        super.readRecord(in);
        pc = in.read64bit();
        instructionCount = in.read64bit();
        int cnt = in.read8bit();
        deltaId = 0;
        deltaValue = new long[cnt];
        for (int i = 0; i < cnt; i++) {
            setDeltaId(in.read8bit());
            deltaValue[i] = in.read64bit();
        }
    }

    private byte[] getDeltaId() {
        byte[] result = new byte[Long.bitCount(deltaId)];
        for (int i = 0, n = 0; i < 64; i++) {
            if (getDeltaId(i)) {
                result[n++] = (byte) i;
            }
        }
        return result;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        super.writeRecord(out);
        out.write64bit(pc);
        out.write64bit(instructionCount);
        out.write8bit((byte) deltaValue.length);
        byte[] deltaIds = getDeltaId();
        for (int i = 0; i < deltaValue.length; i++) {
            out.write8bit(deltaIds[i]);
            out.write64bit(deltaValue[i]);
        }
    }

    @Override
    public String toString() {
        return getState().toString();
    }
}
