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
package org.graalvm.vm.trcview.analysis;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.HexFormatter;

public class ComputedSymbol {
    public static enum Type {
        SUBROUTINE,
        LOCATION,
        DATA,
        UNKNOWN;
    }

    public String name;
    public final long address;
    public Type type;
    public final List<Node> visits;

    public Prototype prototype;

    public final BitSet savedRegisters = new BitSet(); // these registers stayed the same
    public final BitSet destroyedRegisters = new BitSet(); // these registers changed
    private BitSet unusedRegisters = null; // derived set of "unused registers"

    public ComputedSymbol(String name, long address, Type type) {
        if (name == null) {
            throw new NullPointerException("name is null");
        }

        this.name = name;
        this.address = address;
        this.type = type;
        visits = new ArrayList<>();
    }

    public void addVisit(Node node) {
        visits.add(node);
    }

    public void resetVisits() {
        visits.clear();
    }

    public void computeUnusedRegisters(int regcount) {
        unusedRegisters = (BitSet) savedRegisters.clone();
        BitSet inverse = new BitSet(regcount);
        inverse.set(0, regcount);
        inverse.xor(destroyedRegisters); // invert destroyedRegisters
        unusedRegisters.and(inverse);
    }

    public boolean isRegisterUnused(int r) {
        if (unusedRegisters == null) {
            throw new IllegalStateException("register set not yet computed");
        }
        return unusedRegisters.get(r);
    }

    public BitSet getUnusedRegisters() {
        return (BitSet) unusedRegisters.clone();
    }

    @Override
    public int hashCode() {
        return (int) address;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof ComputedSymbol)) {
            return false;
        }
        ComputedSymbol s = (ComputedSymbol) o;
        return s.address == address && s.name.equals(name);
    }

    @Override
    public String toString() {
        return "<" + name + ">@0x" + HexFormatter.tohex(address);
    }
}
