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
package org.graalvm.vm.x86.isa.test;

import org.graalvm.vm.x86.isa.instruction.Lfence;
import org.graalvm.vm.x86.test.InstructionTest;
import org.junit.Test;

public class LfenceTest extends InstructionTest {
    private static final byte[] MACHINECODE1 = {0x0f, (byte) 0xae, (byte) 0xe8};
    private static final String ASSEMBLY1 = "lfence";

    private static final byte[] MACHINECODE2 = {0x0f, (byte) 0xae, (byte) 0xe9};
    private static final String ASSEMBLY2 = "lfence";

    private static final byte[] MACHINECODE3 = {0x0f, (byte) 0xae, (byte) 0xea};
    private static final String ASSEMBLY3 = "lfence";

    private static final byte[] MACHINECODE4 = {0x0f, (byte) 0xae, (byte) 0xeb};
    private static final String ASSEMBLY4 = "lfence";

    private static final byte[] MACHINECODE5 = {0x0f, (byte) 0xae, (byte) 0xec};
    private static final String ASSEMBLY5 = "lfence";

    private static final byte[] MACHINECODE6 = {0x0f, (byte) 0xae, (byte) 0xed};
    private static final String ASSEMBLY6 = "lfence";

    private static final byte[] MACHINECODE7 = {0x0f, (byte) 0xae, (byte) 0xee};
    private static final String ASSEMBLY7 = "lfence";

    private static final byte[] MACHINECODE8 = {0x0f, (byte) 0xae, (byte) 0xef};
    private static final String ASSEMBLY8 = "lfence";

    @Test
    public void test1() {
        check(MACHINECODE1, ASSEMBLY1, Lfence.class);
    }

    @Test
    public void test2() {
        check(MACHINECODE2, ASSEMBLY2, Lfence.class);
    }

    @Test
    public void test3() {
        check(MACHINECODE3, ASSEMBLY3, Lfence.class);
    }

    @Test
    public void test4() {
        check(MACHINECODE4, ASSEMBLY4, Lfence.class);
    }

    @Test
    public void test5() {
        check(MACHINECODE5, ASSEMBLY5, Lfence.class);
    }

    @Test
    public void test6() {
        check(MACHINECODE6, ASSEMBLY6, Lfence.class);
    }

    @Test
    public void test7() {
        check(MACHINECODE7, ASSEMBLY7, Lfence.class);
    }

    @Test
    public void test8() {
        check(MACHINECODE8, ASSEMBLY8, Lfence.class);
    }
}
