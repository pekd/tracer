package org.graalvm.vm.x86.trcview.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.analysis.Analysis;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.analysis.MappedFiles;
import org.graalvm.vm.x86.trcview.analysis.Search;
import org.graalvm.vm.x86.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.x86.trcview.analysis.SymbolTable;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.ui.event.ChangeListener;

public class Local implements TraceAnalyzer {
    private static final Logger log = Trace.create(TraceAnalyzer.class);

    private SymbolResolver resolver;
    private SymbolTable symbols;
    private BlockNode root;
    private MemoryTrace memory;
    private MappedFiles files;
    private long steps;
    private List<ChangeListener> symbolChangeListeners;

    public Local(BlockNode root, Analysis analysis) {
        this.root = root;
        resolver = analysis.getSymbolResolver();
        symbols = analysis.getComputedSymbolTable();
        memory = analysis.getMemoryTrace();
        files = analysis.getMappedFiles();
        steps = analysis.getStepCount();
        symbolChangeListeners = new ArrayList<>();
    }

    @Override
    public Symbol getSymbol(long pc) {
        return resolver.getSymbol(pc);
    }

    @Override
    public ComputedSymbol getComputedSymbol(long pc) {
        return symbols.get(pc);
    }

    @Override
    public void renameSymbol(ComputedSymbol sym, String name) {
        symbols.renameSubroutine(sym, name);
    }

    @Override
    public void setPrototype(ComputedSymbol sym, Prototype prototype) {
        symbols.setPrototype(sym, prototype);
    }

    @Override
    public Set<ComputedSymbol> getSubroutines() {
        return symbols.getSubroutines();
    }

    @Override
    public Set<ComputedSymbol> getLocations() {
        return symbols.getLocations();
    }

    @Override
    public Collection<ComputedSymbol> getSymbols() {
        return symbols.getSymbols();
    }

    @Override
    public Map<String, List<ComputedSymbol>> getNamedSymbols() {
        return symbols.getNamedSymbols();
    }

    @Override
    public void addSymbolRenameListener(SymbolRenameListener listener) {
        symbols.addSymbolRenameListener(listener);
    }

    @Override
    public void addSymbolChangeListener(ChangeListener listener) {
        symbolChangeListeners.add(listener);
    }

    @Override
    public void addSubroutine(long pc, String name, Prototype prototype) {
        symbols.addSubroutine(pc, name);
        ComputedSymbol sym = symbols.get(pc);
        sym.prototype = prototype;
    }

    private void analyzeBlock(BlockNode block) {
        for (Node node : block.getNodes()) {
            symbols.visit(node);
            if (node instanceof BlockNode) {
                analyzeBlock((BlockNode) node);
            }
        }
    }

    @Override
    public void reanalyze() {
        getSymbols().forEach(ComputedSymbol::resetVisits);
        analyzeBlock(root);
        symbols.cleanup();
        for (ChangeListener l : symbolChangeListeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.warning("Error while executing listener: " + l);
            }
        }
    }

    @Override
    public void refresh() {
        // nothing
    }

    @Override
    public long getInstructionCount() {
        return steps;
    }

    @Override
    public BlockNode getRoot() {
        return root;
    }

    @Override
    public BlockNode getParent(Node node) {
        return node.getParent();
    }

    @Override
    public BlockNode getChildren(BlockNode node) {
        return node;
    }

    @Override
    public Node getNode(Node node) {
        return node;
    }

    @Override
    public Node getInstruction(long insn) {
        return Search.instruction(root, insn);
    }

    @Override
    public Node getNextStep(Node node) {
        return Search.nextStep(node);
    }

    @Override
    public Node getPreviousStep(Node node) {
        return Search.previousStep(node);
    }

    @Override
    public Node getNextPC(Node node, long pc) {
        return Search.nextPC(node, pc);
    }

    @Override
    public byte getI8(long address, long insn) throws MemoryNotMappedException {
        return memory.getByte(address, insn);
    }

    @Override
    public long getI64(long address, long insn) throws MemoryNotMappedException {
        return memory.getWord(address, insn);
    }

    @Override
    public MemoryRead getLastRead(long address, long insn) throws MemoryNotMappedException {
        return memory.getLastRead(address, insn);
    }

    @Override
    public MemoryRead getNextRead(long address, long insn) throws MemoryNotMappedException {
        return memory.getNextRead(address, insn);
    }

    @Override
    public MemoryUpdate getLastWrite(long address, long insn) throws MemoryNotMappedException {
        return memory.getLastWrite(address, insn);
    }

    @Override
    public Node getMapNode(long address, long insn) throws MemoryNotMappedException {
        return memory.getMapNode(address, insn);
    }

    @Override
    public long getBase(long pc) {
        return files.getBase(pc);
    }

    @Override
    public long getLoadBias(long pc) {
        return files.getLoadBias(pc);
    }

    @Override
    public long getOffset(long pc) {
        return files.getOffset(pc);
    }

    @Override
    public long getFileOffset(long pc) {
        return files.getFileOffset(pc);
    }

    @Override
    public String getFilename(long pc) {
        return files.getFilename(pc);
    }
}
