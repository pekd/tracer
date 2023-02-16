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
package org.graalvm.vm.trcview.arch.x86.decode.isa.instruction;

import org.graalvm.vm.trcview.arch.x86.decode.isa.AMD64Instruction;

public abstract class Jcc extends AMD64Instruction {
    private final String name;
    protected final long bta;

    protected Jcc(long pc, byte[] instruction, int offset, String name) {
        super(pc, instruction);
        this.bta = getPC() + getSize() + offset;
        this.name = name;
    }

    public static class Ja extends Jcc {
        public Ja(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "ja");
        }
    }

    public static class Jae extends Jcc {
        public Jae(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jae");
        }
    }

    public static class Jb extends Jcc {
        public Jb(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jb");
        }
    }

    public static class Jbe extends Jcc {
        public Jbe(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jbe");
        }
    }

    public static class Jrcxz extends Jcc {
        public Jrcxz(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jrcxz");
        }
    }

    public static class Je extends Jcc {
        public Je(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "je");
        }
    }

    public static class Jg extends Jcc {
        public Jg(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jg");
        }
    }

    public static class Jge extends Jcc {
        public Jge(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jge");
        }
    }

    public static class Jl extends Jcc {
        public Jl(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jl");
        }
    }

    public static class Jle extends Jcc {
        public Jle(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jle");
        }
    }

    public static class Jne extends Jcc {
        public Jne(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jne");
        }
    }

    public static class Jno extends Jcc {
        public Jno(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jno");
        }
    }

    public static class Jnp extends Jcc {
        public Jnp(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jnp");
        }
    }

    public static class Jns extends Jcc {
        public Jns(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jns");
        }
    }

    public static class Jo extends Jcc {
        public Jo(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jo");
        }
    }

    public static class Jp extends Jcc {
        public Jp(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "jp");
        }
    }

    public static class Js extends Jcc {
        public Js(long pc, byte[] instruction, int offset) {
            super(pc, instruction, offset, "js");
        }
    }

    @Override
    public boolean isControlFlow() {
        return true;
    }

    @Override
    protected String[] disassemble() {
        return new String[]{name, String.format("0x%x", bta)};
    }
}
