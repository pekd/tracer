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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class SymbolTable {
    private static Logger log = Trace.create(SymbolTable.class);

    private final NavigableMap<Long, Symbol> traceSymbols;

    private final Map<Long, ComputedSymbol> symbols = new HashMap<>();
    private final List<SymbolRenameListener> listeners = new ArrayList<>();

    private final SymbolName names;

    private ComputedSymbol sub(long pc) {
        return new ComputedSymbol(names.sub(pc), pc, ComputedSymbol.Type.SUBROUTINE);
    }

    private ComputedSymbol sc(long pc) {
        return new ComputedSymbol(names.sc(pc), pc, ComputedSymbol.Type.SUBROUTINE);
    }

    private ComputedSymbol loc(long pc) {
        return new ComputedSymbol(names.loc(pc), pc, ComputedSymbol.Type.LOCATION);
    }

    public SymbolTable(StepFormat format, NavigableMap<Long, Symbol> traceSymbols) {
        this.traceSymbols = traceSymbols;
        names = new SymbolName(format);
    }

    public NavigableMap<Long, Symbol> getTraceSymbols() {
        return Collections.unmodifiableNavigableMap(traceSymbols);
    }

    public void addSubroutine(long pc) {
        ComputedSymbol sym = symbols.get(pc);
        if (sym == null) {
            symbols.put(pc, sub(pc));
        } else {
            if (sym.type != ComputedSymbol.Type.SUBROUTINE) {
                sym.type = ComputedSymbol.Type.SUBROUTINE;
            }
            if (sym.name.equals(names.loc(pc))) {
                sym.name = names.sub(pc);
            }
        }
    }

    public void addSubroutine(long pc, String name) {
        ComputedSymbol sym = symbols.get(pc);
        if (sym == null) {
            symbols.put(pc, new ComputedSymbol(name, pc, ComputedSymbol.Type.SUBROUTINE));
        } else {
            if (sym.type != ComputedSymbol.Type.SUBROUTINE) {
                sym.type = ComputedSymbol.Type.SUBROUTINE;
            }
            if (sym.name.equals(names.loc(pc)) || sym.name.equals(names.sub(pc))) {
                sym.name = name;
            }
        }
    }

    public void addSyscall(long pc) {
        ComputedSymbol sym = symbols.get(pc);
        if (sym == null) {
            symbols.put(pc, sc(pc));
        } else {
            if (sym.type != ComputedSymbol.Type.SUBROUTINE) {
                sym.type = ComputedSymbol.Type.SUBROUTINE;
            }
            if (sym.name.equals(names.loc(pc))) {
                sym.name = names.sc(pc);
            }
        }
    }

    public void addSyscall(long pc, String name) {
        ComputedSymbol sym = symbols.get(pc);
        if (sym == null) {
            symbols.put(pc, new ComputedSymbol(name, pc, ComputedSymbol.Type.SUBROUTINE));
        } else {
            if (sym.type != ComputedSymbol.Type.SUBROUTINE) {
                sym.type = ComputedSymbol.Type.SUBROUTINE;
            }
            if (sym.name.equals(names.loc(pc)) || sym.name.equals(names.sub(pc))) {
                sym.name = name;
            }
        }
    }

    public void addLocation(long pc) {
        ComputedSymbol sym = symbols.get(pc);
        if (sym == null) {
            symbols.put(pc, loc(pc));
        }
    }

    public void visit(Node node) {
        if (node instanceof StepEvent) {
            StepEvent step = (StepEvent) node;
            long pc = step.getPC();
            ComputedSymbol sym = symbols.get(pc);
            if (sym != null) {
                sym.addVisit(node);
            }
        } else if (node instanceof BlockNode) {
            BlockNode block = (BlockNode) node;
            long pc = block.getHead().getPC();
            ComputedSymbol sym = symbols.get(pc);
            if (sym != null) {
                sym.addVisit(node);
            }
        }
    }

    public ComputedSymbol get(long pc) {
        return symbols.get(pc);
    }

    public Set<ComputedSymbol> getSubroutines() {
        return symbols.values().stream().filter(s -> s.type == ComputedSymbol.Type.SUBROUTINE).collect(Collectors.toSet());
    }

    public Set<ComputedSymbol> getLocations() {
        return symbols.values().stream().filter(s -> s.type == ComputedSymbol.Type.LOCATION).collect(Collectors.toSet());
    }

    public Collection<ComputedSymbol> getSymbols() {
        return Collections.unmodifiableCollection(symbols.values());
    }

    public void addSymbolRenameListener(SymbolRenameListener l) {
        listeners.add(l);
    }

    public void removeSymbolRenameListener(SymbolRenameListener l) {
        listeners.remove(l);
    }

    protected void fireSymbolRenameEvent(ComputedSymbol sym) {
        for (SymbolRenameListener l : listeners) {
            try {
                l.symbolRenamed(sym);
            } catch (Throwable t) {
                log.log(Levels.WARNING, "SymbolRenameListener failed: " + t, t);
            }
        }
    }

    public void renameSubroutine(ComputedSymbol sub, String name) {
        if (!sub.name.equals(name)) {
            sub.name = name;
            fireSymbolRenameEvent(sub);
        }
    }

    public void setFunctionType(ComputedSymbol sub, Function type) {
        sub.prototype = type.getPrototype();
        sub.name = type.getName();
        fireSymbolRenameEvent(sub);
    }

    public void setPrototype(ComputedSymbol sub, Prototype type) {
        sub.prototype = type;
        fireSymbolRenameEvent(sub);
    }

    public Map<String, List<ComputedSymbol>> getNamedSymbols() {
        Map<String, List<ComputedSymbol>> map = new HashMap<>();
        for (ComputedSymbol sym : symbols.values()) {
            List<ComputedSymbol> entry = map.get(sym.name);
            if (entry == null) {
                entry = new ArrayList<>();
                map.put(sym.name, entry);
            }
            entry.add(sym);
        }
        return map;
    }

    public void cleanup() {
        List<ComputedSymbol> unused = symbols.values().stream().filter(s -> s.type == ComputedSymbol.Type.SUBROUTINE).filter(s -> s.visits.size() == 0).collect(Collectors.toList());
        unused.forEach(s -> {
            log.info("Unused symbol " + s.name + " @ " + names.addr(s.address));
        });
    }
}
