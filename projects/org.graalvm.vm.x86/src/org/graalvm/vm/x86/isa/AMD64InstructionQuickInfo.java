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
package org.graalvm.vm.x86.isa;

public class AMD64InstructionQuickInfo {
    public static enum InstructionType {
        JMP,
        CALL,
        RET,
        JCC,
        OTHER;
    }

    public static int getOpcodeOffset(byte[] machinecode) {
        int i = 0;
        while (true) {
            switch (machinecode[i]) {
                case AMD64InstructionPrefix.ADDRESS_SIZE_OVERRIDE:
                case AMD64InstructionPrefix.OPERAND_SIZE_OVERRIDE:
                case AMD64InstructionPrefix.LOCK:
                case AMD64InstructionPrefix.REPNZ:
                case AMD64InstructionPrefix.REP:
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_CS:
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_SS:
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_DS:
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_ES:
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_FS:
                case AMD64InstructionPrefix.SEGMENT_OVERRIDE_GS:
                    i++;
                    continue;
                default:
                    if (AMD64RexPrefix.isREX(machinecode[i])) {
                        i++;
                        continue;
                    } else {
                        return i;
                    }
            }
        }
    }

    public static boolean isJcc(byte[] machinecode) {
        int op = getOpcodeOffset(machinecode);
        switch (machinecode[op]) {
            case AMD64Opcode.JA:
            case AMD64Opcode.JAE:
            case AMD64Opcode.JB:
            case AMD64Opcode.JBE:
            case AMD64Opcode.JE:
            case AMD64Opcode.JG:
            case AMD64Opcode.JGE:
            case AMD64Opcode.JL:
            case AMD64Opcode.JLE:
            case AMD64Opcode.JNE:
            case AMD64Opcode.JNO:
            case AMD64Opcode.JNP:
            case AMD64Opcode.JNS:
            case AMD64Opcode.JO:
            case AMD64Opcode.JP:
            case AMD64Opcode.JS:
                return true;
            case AMD64Opcode.ESCAPE:
                switch (machinecode[op + 1]) {
                    case AMD64Opcode.JA32:
                    case AMD64Opcode.JAE32:
                    case AMD64Opcode.JB32:
                    case AMD64Opcode.JBE32:
                    case AMD64Opcode.JE32:
                    case AMD64Opcode.JG32:
                    case AMD64Opcode.JGE32:
                    case AMD64Opcode.JL32:
                    case AMD64Opcode.JLE32:
                    case AMD64Opcode.JNE32:
                    case AMD64Opcode.JNO32:
                    case AMD64Opcode.JNP32:
                    case AMD64Opcode.JNS32:
                    case AMD64Opcode.JO32:
                    case AMD64Opcode.JP32:
                    case AMD64Opcode.JS32:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    public static boolean isCall(byte[] machinecode) {
        int op = getOpcodeOffset(machinecode);
        switch (machinecode[op]) {
            case AMD64Opcode.CALL_REL:
                return true;
            case AMD64Opcode.CALL_RM:
                return new ModRM(machinecode[op + 1]).getReg() == 2;
            default:
                return false;
        }
    }

    public static boolean isJmp(byte[] machinecode) {
        int op = getOpcodeOffset(machinecode);
        switch (machinecode[op]) {
            case AMD64Opcode.JMP_REL8:
            case AMD64Opcode.JMP_REL32:
                return true;
            case AMD64Opcode.JMP_R:
                return new ModRM(machinecode[op + 1]).getReg() == 4;
            default:
                return false;
        }
    }

    public static boolean isRet(byte[] machinecode) {
        int op = getOpcodeOffset(machinecode);
        switch (machinecode[op]) {
            case AMD64Opcode.RET_NEAR:
            case AMD64Opcode.RET_FAR:
                return true;
            default:
                return false;
        }
    }

    public static InstructionType getType(byte[] machinecode) {
        int op = getOpcodeOffset(machinecode);
        switch (machinecode[op]) {
            case AMD64Opcode.RET_NEAR:
            case AMD64Opcode.RET_FAR:
                return InstructionType.RET;
            case AMD64Opcode.JMP_REL8:
            case AMD64Opcode.JMP_REL32:
                return InstructionType.JMP;
            case AMD64Opcode.JMP_R:
                switch (new ModRM(machinecode[op + 1]).getReg()) {
                    case 2:
                        return InstructionType.CALL;
                    case 4:
                        return InstructionType.JMP;
                    default:
                        return InstructionType.OTHER;
                }
            case AMD64Opcode.CALL_REL:
                return InstructionType.CALL;
            case AMD64Opcode.JA:
            case AMD64Opcode.JAE:
            case AMD64Opcode.JB:
            case AMD64Opcode.JBE:
            case AMD64Opcode.JE:
            case AMD64Opcode.JG:
            case AMD64Opcode.JGE:
            case AMD64Opcode.JL:
            case AMD64Opcode.JLE:
            case AMD64Opcode.JNE:
            case AMD64Opcode.JNO:
            case AMD64Opcode.JNP:
            case AMD64Opcode.JNS:
            case AMD64Opcode.JO:
            case AMD64Opcode.JP:
            case AMD64Opcode.JS:
                return InstructionType.JCC;
            case AMD64Opcode.ESCAPE:
                switch (machinecode[op + 1]) {
                    case AMD64Opcode.JA32:
                    case AMD64Opcode.JAE32:
                    case AMD64Opcode.JB32:
                    case AMD64Opcode.JBE32:
                    case AMD64Opcode.JE32:
                    case AMD64Opcode.JG32:
                    case AMD64Opcode.JGE32:
                    case AMD64Opcode.JL32:
                    case AMD64Opcode.JLE32:
                    case AMD64Opcode.JNE32:
                    case AMD64Opcode.JNO32:
                    case AMD64Opcode.JNP32:
                    case AMD64Opcode.JNS32:
                    case AMD64Opcode.JO32:
                    case AMD64Opcode.JP32:
                    case AMD64Opcode.JS32:
                        return InstructionType.JCC;
                    default:
                        return InstructionType.OTHER;
                }
            default:
                return InstructionType.OTHER;
        }
    }
}
