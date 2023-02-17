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
package org.graalvm.vm.trcview.arch.x86.decode.isa;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.disasm.Type;

public class MemoryOperand extends Operand {
    private final SegmentRegister segment;
    private final Register base;
    private final Register index;
    private final int scale;
    private final long displacement;
    private final boolean addressOverride;

    private MemoryOperand(boolean addressOverride, SegmentRegister segment, Register base, Register index, int scale, long displacement) {
        super(getTokens(segment, base, index, scale, displacement));
        this.addressOverride = addressOverride;
        this.segment = segment;
        this.base = base;
        this.index = index;
        this.scale = scale;
        this.displacement = displacement;
    }

    public MemoryOperand(SegmentRegister segment, Register base) {
        this(segment, base, false);
    }

    public MemoryOperand(SegmentRegister segment, Register base, boolean addressOverride) {
        this(addressOverride, segment, getRegister(addressOverride, base), null, 0, 0);
    }

    public MemoryOperand(SegmentRegister segment, Register base, long displacement, boolean addressOverride) {
        this(addressOverride, segment, getRegister(addressOverride, base), null, 0, displacement);
    }

    public MemoryOperand(SegmentRegister segment, Register base, Register index, int scale, boolean addressOverride) {
        this(addressOverride, segment, base != null ? getRegister(addressOverride, base) : null, index != null ? getRegister(addressOverride, index) : null, scale, 0);
    }

    public MemoryOperand(SegmentRegister segment, Register base, Register index, int scale, long displacement, boolean addressOverride) {
        this(addressOverride, segment, base != null ? getRegister(addressOverride, base) : base, index != null ? getRegister(addressOverride, index) : index, scale, displacement);
    }

    public MemoryOperand(SegmentRegister segment, long displacement, boolean addressOverride) {
        this(addressOverride, segment, null, null, 0, displacement);
    }

    public MemoryOperand getInSegment(SegmentRegister seg) {
        MemoryOperand op = new MemoryOperand(seg, base, index, scale, displacement, addressOverride);
        assert op.base == base;
        assert op.index == index;
        assert op.scale == scale;
        assert op.displacement == displacement;
        assert op.segment == seg;
        return op;
    }

    private static Register getRegister(boolean addressOverride, Register reg) {
        if (addressOverride) {
            return reg.get32bit();
        } else {
            return reg.get64bit();
        }
    }

    public SegmentRegister getSegment() {
        return segment;
    }

    public Register getBase() {
        return base;
    }

    public Register getIndex() {
        return index;
    }

    public int getScale() {
        return scale;
    }

    public long getDisplacement() {
        return displacement;
    }

    public boolean isAddressOverride() {
        return addressOverride;
    }

    @Override
    public Token[] getTokens() {
        if (base == Register.RIP && index == null && segment == null) {
            long addr = getPC() + displacement;
            String loc = getName(addr);
            if (loc != null) {
                return new Token[]{new Token(Type.LABEL, loc, addr)};
            }
        }

        return super.getTokens();
    }

    @Override
    public String toString() {
        if (base == Register.RIP && index == null && segment == null) {
            long addr = getPC() + displacement;
            String loc = getName(addr);
            if (loc != null) {
                return loc;
            }
        }

        StringBuilder buf = new StringBuilder();
        if (base != null) {
            buf.append(base.toString());
        }
        if (index != null) {
            if (buf.length() > 0) {
                buf.append("+");
            }
            buf.append(index.toString());
            if (scale > 0) {
                buf.append("*" + (1 << scale));
            }
        }
        if (buf.length() == 0 || displacement != 0) {
            boolean add = displacement >= 0;
            if (add && buf.length() > 0) {
                buf.append("+");
            }
            if (add) {
                buf.append(String.format("0x%x", displacement));
            } else {
                buf.append(String.format("-0x%x", -displacement));
            }
        }
        if (segment != null) {
            return segment + ":[" + buf + "]";
        } else {
            return "[" + buf + "]";
        }
    }

    private static Token[] getTokens(SegmentRegister segment, Register base, Register index, int scale, long displacement) {
        List<Token> tokens = new ArrayList<>();
        if (base != null) {
            tokens.add(new Token(Type.REGISTER, base.toString()));
        }
        if (index != null) {
            if (tokens.size() > 0) {
                tokens.add(new Token(Type.OTHER, "+"));
            }
            tokens.add(new Token(Type.REGISTER, index.toString()));
            if (scale > 0) {
                tokens.add(new Token(Type.OTHER, "*"));
                tokens.add(new Token(Type.NUMBER, Integer.toString(1 << scale)));
            }
        }
        if (tokens.size() == 0 || displacement != 0) {
            boolean add = displacement >= 0;
            boolean pcRelative = base == Register.RIP && index == null;
            if (add && tokens.size() > 0) {
                tokens.add(new Token(Type.OTHER, "+"));
            }
            if (add) {
                if (displacement < 10) {
                    tokens.add(new Token(Type.OFFSET, Long.toString(displacement), displacement, pcRelative));
                } else {
                    tokens.add(new Token(Type.OFFSET, String.format("0x%x", displacement), displacement, pcRelative));
                }
            } else {
                tokens.add(new Token(Type.OTHER, "-"));
                if (displacement > -10) {
                    tokens.add(new Token(Type.OFFSET, Long.toString(-displacement), displacement, pcRelative));
                } else {
                    tokens.add(new Token(Type.OFFSET, String.format("0x%x", -displacement), displacement, pcRelative));
                }
            }
        }

        if (segment != null) {
            Token[] result = new Token[tokens.size() + 3];
            result[0] = new Token(Type.REGISTER, segment.toString());
            result[1] = new Token(Type.OTHER, ":[");
            for (int i = 0; i < tokens.size(); i++) {
                result[i + 2] = tokens.get(i);
            }
            result[tokens.size() + 2] = new Token(Type.OTHER, "]");
            return result;
        } else {
            Token[] result = new Token[tokens.size() + 2];
            result[0] = new Token(Type.OTHER, "[");
            for (int i = 0; i < tokens.size(); i++) {
                result[i + 1] = tokens.get(i);
            }
            result[tokens.size() + 1] = new Token(Type.OTHER, "]");
            return result;
        }
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Register[] getRegisters() {
        if (base != null && index != null) {
            return new Register[]{base, index};
        } else if (base != null && index == null) {
            return new Register[]{base};
        } else if (base == null && index != null) {
            return new Register[]{index};
        } else {
            return new Register[0];
        }
    }
}
