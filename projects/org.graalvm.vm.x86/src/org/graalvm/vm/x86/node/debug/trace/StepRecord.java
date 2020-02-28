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

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.isa.AMD64Instruction;
import org.graalvm.vm.x86.isa.AMD64InstructionDecoder;
import org.graalvm.vm.x86.isa.CodeArrayReader;

public class StepRecord extends Record {
    public static final int MAGIC = 0x53544550; // STEP

    private byte[] machinecode;
    private CpuStateRecord cpuState;

    StepRecord() {
        super(MAGIC);
    }

    public StepRecord(byte[] machinecode, CpuStateRecord cpuState) {
        super(MAGIC);
        this.machinecode = machinecode;
        this.cpuState = cpuState;
    }

    public byte[] getMachinecode() {
        return machinecode;
    }

    public AMD64Instruction getInstruction() {
        try {
            return AMD64InstructionDecoder.decode(getPC(), new CodeArrayReader(machinecode, 0));
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private String code() {
        StringBuilder buf = new StringBuilder(machinecode.length * 4);
        for (byte b : machinecode) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append("0x" + HexFormatter.tohex(b & 0xFF, 2));
        }
        return buf.toString();
    }

    public String getDisassembly() {
        try {
            AMD64Instruction insn = getInstruction();
            if (insn == null) {
                return "db\t" + code();
            } else {
                return insn.getDisassembly();
            }
        } catch (Throwable t) {
            return "db\t" + code();
        }
    }

    public String[] getDisassemblyComponents() {
        if (machinecode != null) {
            try {
                AMD64Instruction insn = getInstruction();
                if (insn == null) {
                    return new String[]{"db", code()};
                } else {
                    return insn.getDisassemblyComponents();
                }
            } catch (Throwable t) {
                return new String[]{"db", code()};
            }
        } else {
            return null;
        }
    }

    public String getMnemonic() {
        if (machinecode == null) {
            return null;
        } else {
            AMD64Instruction insn = getInstruction();
            if (insn == null) {
                return "db";
            } else {
                String[] parts = insn.getDisassemblyComponents();
                return parts[0];
            }
        }
    }

    public CpuStateRecord getState() {
        return cpuState;
    }

    public long getPC() {
        return getState().getPC();
    }

    public long getInstructionCount() {
        return getState().getInstructionCount();
    }

    @Override
    protected int getDataSize() {
        return sizeArray(machinecode) + cpuState.size() + 8;
    }

    @Override
    protected void readRecord(WordInputStream in) throws IOException {
        machinecode = readArray(in);
        cpuState = Record.read(in, getLastStateSupplier());
        clearLastState();
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        writeArray(out, machinecode);
        cpuState.write(out);
    }

    @Override
    public String toString() {
        return String.format("0x%08x: %s # step %d", getPC(), getInstruction().getDisassembly(), getInstructionCount());
    }
}
