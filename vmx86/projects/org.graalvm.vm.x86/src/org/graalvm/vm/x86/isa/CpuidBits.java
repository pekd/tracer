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

import com.oracle.truffle.api.CompilerAsserts;

public class CpuidBits {
    // FN=1: EDX
    public static final int FPU = 1;
    public static final int VME = 1 << 1;
    public static final int DE = 1 << 2;
    public static final int PSE = 1 << 3;
    public static final int TSC = 1 << 4;
    public static final int MSR = 1 << 5;
    public static final int PAE = 1 << 6;
    public static final int MCE = 1 << 7;
    public static final int CX8 = 1 << 8;
    public static final int APIC = 1 << 9;
    public static final int SEP = 1 << 11;
    public static final int MTRR = 1 << 12;
    public static final int PGE = 1 << 13;
    public static final int MCA = 1 << 14;
    public static final int CMOV = 1 << 15;
    public static final int PAT = 1 << 16;
    public static final int PSE_36 = 1 << 17;
    public static final int PSN = 1 << 18;
    public static final int CLFSH = 1 << 19;
    public static final int DS = 1 << 21;
    public static final int ACPI = 1 << 22;
    public static final int MMX = 1 << 23;
    public static final int FXSR = 1 << 24;
    public static final int SSE = 1 << 25;
    public static final int SSE2 = 1 << 26;
    public static final int SS = 1 << 27;
    public static final int HTT = 1 << 28;
    public static final int TM = 1 << 29;
    public static final int IA64 = 1 << 30;
    public static final int PBE = 1 << 31;

    // FN=1: ECX
    public static final int SSE3 = 1;
    public static final int PCLMULQDQ = 1 << 1;
    public static final int DTES64 = 1 << 2;
    public static final int MONITOR = 1 << 3;
    public static final int DS_CPL = 1 << 4;
    public static final int VMX = 1 << 5;
    public static final int SMX = 1 << 6;
    public static final int EST = 1 << 7;
    public static final int TM2 = 1 << 8;
    public static final int SSSE3 = 1 << 9;
    public static final int CNXT_ID = 1 << 10;
    public static final int SDBG = 1 << 11;
    public static final int FMA = 1 << 12;
    public static final int CX16 = 1 << 13;
    public static final int XTPR = 1 << 14;
    public static final int PDCM = 1 << 15;
    public static final int PCID = 1 << 17;
    public static final int DCA = 1 << 18;
    public static final int SSE41 = 1 << 19;
    public static final int SSE42 = 1 << 20;
    public static final int X2APIC = 1 << 21;
    public static final int MOVBE = 1 << 22;
    public static final int POPCNT = 1 << 23;
    public static final int TSC_DEADLINE = 1 << 24;
    public static final int AES = 1 << 25;
    public static final int XSAVE = 1 << 26;
    public static final int OXSAVE = 1 << 27;
    public static final int AVX = 1 << 28;
    public static final int F16C = 1 << 29;
    public static final int RDRND = 1 << 30;
    public static final int HYPERVISOR = 1 << 31;

    // FN=7/0: EBX
    public static final int RDSEED = 1 << 18;

    // FN=80000001h: EDX
    public static final int LM = 1 << 29;

    // FN=80000001h: ECX
    public static final int LAHF = 1;

    // FN=80000006h
    public static final int CACHE_DISABLED = 0;
    public static final int CACHE_DIRECT_MAPPED = 1;
    public static final int CACHE_2WAY_ASSOC = 2;
    public static final int CACHE_4WAY_ASSOC = 4;
    public static final int CACHE_8WAY_ASSOC = 6;
    public static final int CACHE_16WAY_ASSOC = 8;
    public static final int CACHE_FULLY_ASSOC = 15;

    public static int[] getI32(String s, int len) {
        CompilerAsserts.neverPartOfCompilation();
        int[] i32 = new int[len];
        for (int i = 0; i < len; i++) {
            byte b1 = getI8(s, i * 4);
            byte b2 = getI8(s, i * 4 + 1);
            byte b3 = getI8(s, i * 4 + 2);
            byte b4 = getI8(s, i * 4 + 3);
            i32[i] = Byte.toUnsignedInt(b1) | Byte.toUnsignedInt(b2) << 8 | Byte.toUnsignedInt(b3) << 16 | Byte.toUnsignedInt(b4) << 24;
        }
        return i32;
    }

    public static byte getI8(String s, int offset) {
        CompilerAsserts.neverPartOfCompilation();
        if (offset >= s.length()) {
            return 0;
        } else {
            return (byte) s.charAt(offset);
        }
    }

    public static int getProcessorInfo(int type, int family, int model, int stepping) {
        CompilerAsserts.neverPartOfCompilation();
        int cpuidFamily = family & 0x0F;
        int cpuidModel = model & 0x0F;
        int cpuidStepping = stepping & 0x0F;
        int cpuidProcessorType = type & 0x0F;
        int cpuidExtendedModel = 0;
        int cpuidExtendedFamily = 0;

        if (model > 15) {
            cpuidExtendedModel = (model >> 4) & 0x0F;
            if (family != 6 && family < 16) {
                throw new IllegalArgumentException("model number too big for given family");
            }
        }
        if (family > 15) {
            cpuidFamily = 15;
            cpuidExtendedFamily = family - 15;
        }
        return cpuidStepping | (cpuidModel << 4) | (cpuidFamily << 8) | (cpuidProcessorType << 12) | (cpuidExtendedModel << 16) | (cpuidExtendedFamily << 20);
    }

    public static int getCacheLineInfo(int cacheLineSize, int assoc, int cacheSize) {
        return (cacheSize << 16) | (cacheLineSize & 0xFF) | (assoc << 12);
    }

    public static int getMemorySizes(int physicalBits, int virtualBits) {
        return physicalBits | (virtualBits << 8);
    }
}
