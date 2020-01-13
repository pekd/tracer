package org.graalvm.vm.x86.trcview.arch.custom.test.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.ChangeListener;

public class MockTraceAnalyzer implements TraceAnalyzer {
    public Symbol getSymbol(long pc) {
        // TODO Auto-generated method stub
        return null;
    }

    public ComputedSymbol getComputedSymbol(long pc) {
        // TODO Auto-generated method stub
        return null;
    }

    public void renameSymbol(ComputedSymbol sym, String name) {
        // TODO Auto-generated method stub

    }

    public void setPrototype(ComputedSymbol sym, Prototype prototype) {
        // TODO Auto-generated method stub

    }

    public Set<ComputedSymbol> getSubroutines() {
        // TODO Auto-generated method stub
        return null;
    }

    public Set<ComputedSymbol> getLocations() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<ComputedSymbol> getSymbols() {
        // TODO Auto-generated method stub
        return null;
    }

    public Map<String, List<ComputedSymbol>> getNamedSymbols() {
        // TODO Auto-generated method stub
        return null;
    }

    public void addSymbolRenameListener(SymbolRenameListener listener) {
        // TODO Auto-generated method stub

    }

    public void addSymbolChangeListener(ChangeListener listener) {
        // TODO Auto-generated method stub

    }

    public void addSubroutine(long pc, String name, Prototype prototype) {
        // TODO Auto-generated method stub

    }

    public void reanalyze() {
        // TODO Auto-generated method stub

    }

    public void refresh() {
        // TODO Auto-generated method stub

    }

    public long getInstructionCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    public BlockNode getRoot() {
        // TODO Auto-generated method stub
        return null;
    }

    public BlockNode getParent(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    public BlockNode getChildren(BlockNode node) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getNode(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getInstruction(long insn) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getNextStep(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getPreviousStep(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getNextPC(Node node, long pc) {
        // TODO Auto-generated method stub
        return null;
    }

    public byte getI8(long address, long insn) throws MemoryNotMappedException {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getI64(long address, long insn) throws MemoryNotMappedException {
        // TODO Auto-generated method stub
        return 0;
    }

    public MemoryRead getLastRead(long address, long insn) throws MemoryNotMappedException {
        // TODO Auto-generated method stub
        return null;
    }

    public MemoryRead getNextRead(long address, long insn) throws MemoryNotMappedException {
        // TODO Auto-generated method stub
        return null;
    }

    public MemoryUpdate getLastWrite(long address, long insn) throws MemoryNotMappedException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getMapNode(long address, long insn) throws MemoryNotMappedException {
        // TODO Auto-generated method stub
        return null;
    }

    public long getBase(long pc) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getLoadBias(long pc) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getOffset(long pc) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getFileOffset(long pc) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getFilename(long pc) {
        // TODO Auto-generated method stub
        return null;
    }

    public Architecture getArchitecture() {
        // TODO Auto-generated method stub
        return null;
    }
}
