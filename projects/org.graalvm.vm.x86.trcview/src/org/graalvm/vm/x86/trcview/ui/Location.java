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
package org.graalvm.vm.x86.trcview.ui;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.x86.trcview.io.data.StepEvent;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

public class Location {
    private long pc;
    private Symbol symbol;
    private String filename;
    private long base;
    private long offset;
    private String[] disasm;
    private byte[] machinecode;

    public static Location getLocation(TraceAnalyzer trc, StepEvent step) {
        long pc = step.getPC();
        Location loc = new Location();
        loc.pc = pc;
        loc.symbol = trc.getSymbol(pc);
        loc.filename = trc.getFilename(pc);
        loc.base = trc.getBase(pc);
        loc.offset = trc.getOffset(pc);
        loc.disasm = step.getDisassemblyComponents();
        loc.machinecode = step.getMachinecode();
        return loc;
    }

    public long getPC() {
        return pc;
    }

    public String getSymbol() {
        return symbol == null ? null : symbol.getName();
    }

    public String getFilename() {
        return filename;
    }

    public long getBase() {
        return base;
    }

    public long getOffset() {
        return offset;
    }

    public byte[] getMachinecode() {
        return machinecode;
    }

    public String[] getDisassembly() {
        return disasm;
    }

    public String getAsm() {
        if (disasm == null) {
            return "<unreadable>";
        }
        if (disasm.length == 1) {
            return disasm[0];
        } else {
            return disasm[0] + "\t" + Stream.of(disasm).skip(1).collect(Collectors.joining(","));
        }
    }

    public String getMnemonic() {
        if (disasm == null) {
            return "<unreadable>";
        } else {
            return disasm[0];
        }
    }

    public String getPrintableBytes() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < machinecode.length; i++) {
            buf.append(' ');
            buf.append(HexFormatter.tohex(Byte.toUnsignedInt(machinecode[i]), 2));
        }
        return buf.toString().substring(1);
    }
}
