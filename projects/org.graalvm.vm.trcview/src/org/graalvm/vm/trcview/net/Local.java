package org.graalvm.vm.trcview.net;

import java.awt.Color;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.MappedFiles;
import org.graalvm.vm.trcview.analysis.Search;
import org.graalvm.vm.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.trcview.analysis.memory.MemorySegment;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.info.Comments;
import org.graalvm.vm.trcview.info.Expressions;
import org.graalvm.vm.trcview.info.FormattedExpression;
import org.graalvm.vm.trcview.info.Highlighter;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Trace;

public class Local implements TraceAnalyzer {
    private static final Logger log = Trace.create(TraceAnalyzer.class);

    private Architecture arch;
    private SymbolResolver resolver;
    private SymbolTable symbols;
    private BlockNode root;
    private Map<Integer, BlockNode> threads;
    private MemoryTrace memory;
    private MappedFiles files;
    private List<Node> syscalls;
    private Map<Integer, List<IoEvent>> io;
    private Map<Integer, Device> devices;
    private long steps;
    private List<ChangeListener> symbolChangeListeners;
    private List<ChangeListener> commentChangeListeners;
    private Comments comments;
    private Expressions expressions;
    private Highlighter highlighter;
    private ABI abi;
    private UserTypeDatabase types;

    public Local(Architecture arch, BlockNode root, Map<Integer, BlockNode> threads, Analysis analysis) {
        this.arch = arch;
        this.root = root;
        this.threads = threads;
        abi = arch.createABI();
        resolver = analysis.getSymbolResolver();
        symbols = analysis.getComputedSymbolTable();
        memory = analysis.getMemoryTrace();
        files = analysis.getMappedFiles();
        syscalls = analysis.getSyscalls();
        io = analysis.getIo();
        devices = analysis.getDevices();
        steps = analysis.getStepCount();
        symbolChangeListeners = new ArrayList<>();
        commentChangeListeners = new ArrayList<>();
        comments = new Comments();
        expressions = new Expressions();
        highlighter = new Highlighter();
        types = new UserTypeDatabase(arch.getTypeInfo());
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
    public void removeSymbolRenameListener(SymbolRenameListener listener) {
        symbols.removeSymbolRenameListener(listener);
    }

    @Override
    public void addSymbolChangeListener(ChangeListener listener) {
        symbolChangeListeners.add(listener);
    }

    @Override
    public void removeSymbolChangeListener(ChangeListener listener) {
        symbolChangeListeners.remove(listener);
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
    public Set<Integer> getThreadIds() {
        return threads.keySet();
    }

    @Override
    public Map<Integer, Long> getThreadStarts() {
        Map<Integer, Long> result = new HashMap<>();
        for (Entry<Integer, BlockNode> entry : threads.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getStep());
        }
        return result;
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
    public List<Node> getSyscalls() {
        return syscalls;
    }

    @Override
    public Map<Integer, List<IoEvent>> getIo() {
        return io;
    }

    public Map<Integer, Device> getDevices() {
        return devices;
    }

    @Override
    public Node getInstruction(long insn) {
        for (BlockNode node : threads.values()) {
            Node result = Search.instruction(node, insn);
            if (result != null) {
                return result;
            }
        }
        return null;
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
    public short getI16(long address, long insn) throws MemoryNotMappedException {
        return (short) memory.getWord(address, insn);
    }

    @Override
    public int getI32(long address, long insn) throws MemoryNotMappedException {
        return (int) memory.getWord(address, insn);
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
    public MemoryUpdate getNextWrite(long address, long insn) throws MemoryNotMappedException {
        return memory.getNextWrite(address, insn);
    }

    @Override
    public List<MemoryUpdate> getPreviousWrites(long address, long insn, long count) throws MemoryNotMappedException {
        return memory.getPreviousWrites(address, insn, count);
    }

    @Override
    public Node getMapNode(long address, long insn) throws MemoryNotMappedException {
        return memory.getMapNode(address, insn);
    }

    @Override
    public List<MemorySegment> getMemorySegments(long insn) {
        return memory.getRegions(insn);
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

    @Override
    public Architecture getArchitecture() {
        return arch;
    }

    @Override
    public void addCommentChangeListener(ChangeListener l) {
        commentChangeListeners.add(l);
    }

    @Override
    public void removeCommentChangeListener(ChangeListener l) {
        commentChangeListeners.remove(l);
    }

    protected void fireCommentChanged() {
        for (ChangeListener l : commentChangeListeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.warning("Error while executing listener: " + l);
            }
        }
    }

    @Override
    public void setCommentForPC(long pc, String comment) {
        comments.setCommentForPC(pc, comment);
        fireCommentChanged();
    }

    @Override
    public String getCommentForPC(long pc) {
        return comments.getCommentForPC(pc);
    }

    @Override
    public void setCommentForInsn(long insn, String comment) {
        comments.setCommentForInsn(insn, comment);
        fireCommentChanged();
    }

    @Override
    public String getCommentForInsn(long insn) {
        return comments.getCommentForInsn(insn);
    }

    @Override
    public Map<Long, String> getCommentsForInsns() {
        return comments.getCommentsForInsns();
    }

    @Override
    public Map<Long, String> getCommentsForPCs() {
        return comments.getCommentsForPCs();
    }

    @Override
    public void setExpression(long pc, String expression) throws ParseException {
        expressions.setExpression(pc, arch.getFormat(), expression);
        fireCommentChanged();
    }

    @Override
    public String getExpression(long pc) {
        FormattedExpression expr = expressions.getExpression(pc);
        if (expr == null) {
            return null;
        } else {
            return expr.getExpression();
        }
    }

    @Override
    public String evaluateExpression(CpuState state) throws EvaluationException {
        return expressions.evaluate(state, this);
    }

    @Override
    public Map<Long, String> getExpressions() {
        return expressions.getExpressions();
    }

    @Override
    public void setColor(long pc, Color color) {
        highlighter.setColor(pc, color);
        fireCommentChanged();
    }

    @Override
    public Color getColor(CpuState state) {
        return highlighter.getColor(state, this);
    }

    @Override
    public Map<Long, Color> getColors() {
        return highlighter.getColors();
    }

    @Override
    public ABI getABI() {
        return abi;
    }

    @Override
    public void addABIChangeListener(ChangeListener l) {
        if (abi != null) {
            abi.addChangeListener(l);
        }
    }

    @Override
    public UserTypeDatabase getTypeDatabase() {
        return types;
    }
}
