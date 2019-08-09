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
package org.graalvm.vm.posix.elf;

public interface Symbol {
    public static final int LOCAL = 0;
    public static final int GLOBAL = 1;
    public static final int WEAK = 2;
    public static final int LOOS = 10;
    public static final int GNU_UNIQUE = 10;
    public static final int HIOS = 11;
    public static final int LOPROC = 13;
    public static final int HIPROC = 15;

    public static final int NOTYPE = 0;
    public static final int OBJECT = 1;
    public static final int FUNC = 2;
    public static final int SECTION = 3;
    public static final int FILE = 4;
    public static final int COMMON = 5;
    public static final int TLS = 6;
    public static final int NUM = 7;
    public static final int GNU_IFUNC = 10;

    public static final int DEFAULT = 0;
    public static final int INTERNAL = 1;
    public static final int HIDDEN = 2;
    public static final int PROTECTED = 3;
    public static final int EXPORTED = 4;
    public static final int SINGLETON = 5;
    public static final int ELIMINATE = 6;

    public static final short SHN_UNDEF = 0;
    public static final short SHN_BEFORE = (short) 0xff00;
    public static final short SHN_AFTER = (short) 0xff01;
    public static final short SHN_ABS = (short) 0xfff1;
    public static final short SHN_COMMON = (short) 0xfff2;
    public static final short SHN_XINDEX = (short) 0xffff;

    String getName();

    int getBind();

    int getType();

    int getVisibility();

    long getSize();

    long getValue();

    short getSectionIndex();

    Symbol offset(long off);
}
