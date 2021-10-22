package org.graalvm.vm.x86.trcview.test.mock;

import java.awt.Color;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.trcview.analysis.memory.MemorySegment;
import org.graalvm.vm.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.IoEvent;
import org.graalvm.vm.trcview.data.DynamicTypePropagation;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.ChangeListener;

public class MockTraceAnalyzer implements TraceAnalyzer {
    @Override
    public Symbol getSymbol(long pc) {
        return null;
    }

    @Override
    public ComputedSymbol getComputedSymbol(long pc) {
        return null;
    }

    @Override
    public void renameSymbol(ComputedSymbol sym, String name) {

    }

    @Override
    public void setPrototype(ComputedSymbol sym, Prototype prototype) {

    }

    @Override
    public Set<ComputedSymbol> getSubroutines() {
        return null;
    }

    @Override
    public Set<ComputedSymbol> getLocations() {
        return null;
    }

    @Override
    public Collection<ComputedSymbol> getSymbols() {
        return null;
    }

    @Override
    public Map<String, List<ComputedSymbol>> getNamedSymbols() {
        return null;
    }

    @Override
    public void addSymbolRenameListener(SymbolRenameListener listener) {

    }

    @Override
    public void removeSymbolRenameListener(SymbolRenameListener listener) {

    }

    @Override
    public void addSymbolChangeListener(ChangeListener listener) {

    }

    @Override
    public void removeSymbolChangeListener(ChangeListener listener) {

    }

    @Override
    public void addSubroutine(long pc, String name, Prototype prototype) {

    }

    @Override
    public void reanalyze() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public long getInstructionCount() {
        return 0;
    }

    @Override
    public Set<Integer> getThreadIds() {
        return null;
    }

    @Override
    public Map<Integer, Long> getThreadStarts() {
        return null;
    }

    @Override
    public BlockNode getRoot() {
        return null;
    }

    @Override
    public BlockNode getParent(Node node) {
        return null;
    }

    @Override
    public BlockNode getChildren(BlockNode node) {
        return null;
    }

    @Override
    public Node getNode(Node node) {
        return null;
    }

    @Override
    public List<Node> getSyscalls() {
        return null;
    }

    @Override
    public Map<Integer, List<IoEvent>> getIo() {
        return null;
    }

    @Override
    public Map<Integer, Device> getDevices() {
        return null;
    }

    @Override
    public Node getInstruction(long insn) {
        return null;
    }

    @Override
    public Node getNextStep(Node node) {
        return null;
    }

    @Override
    public Node getPreviousStep(Node node) {
        return null;
    }

    @Override
    public Node getNextPC(Node node, long pc) {
        return null;
    }

    @Override
    public byte getI8(long address, long insn) throws MemoryNotMappedException {
        return 0;
    }

    @Override
    public short getI16(long address, long insn) throws MemoryNotMappedException {
        return 0;
    }

    @Override
    public int getI32(long address, long insn) throws MemoryNotMappedException {
        return 0;
    }

    @Override
    public long getI64(long address, long insn) throws MemoryNotMappedException {
        return 0;
    }

    @Override
    public MemoryRead getLastRead(long address, long insn) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public MemoryRead getNextRead(long address, long insn) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public MemoryUpdate getLastWrite(long address, long insn) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public MemoryUpdate getNextWrite(long address, long insn) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public List<MemoryUpdate> getPreviousWrites(long address, long insn, long count) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public Node getMapNode(long address, long insn) throws MemoryNotMappedException {
        return null;
    }

    @Override
    public List<MemorySegment> getMemorySegments(long insn) {
        return null;
    }

    @Override
    public long getBase(long pc) {
        return 0;
    }

    @Override
    public long getLoadBias(long pc) {
        return 0;
    }

    @Override
    public long getOffset(long pc) {
        return 0;
    }

    @Override
    public long getFileOffset(long pc) {
        return 0;
    }

    @Override
    public String getFilename(long pc) {
        return null;
    }

    @Override
    public Architecture getArchitecture() {
        return null;
    }

    @Override
    public void addCommentChangeListener(ChangeListener l) {

    }

    @Override
    public void removeCommentChangeListener(ChangeListener l) {

    }

    @Override
    public void setCommentForPC(long pc, String comment) {

    }

    @Override
    public String getCommentForPC(long pc) {
        return null;
    }

    @Override
    public void setCommentForInsn(long insn, String comment) {

    }

    @Override
    public String getCommentForInsn(long insn) {
        return null;
    }

    @Override
    public Map<Long, String> getCommentsForInsns() {
        return null;
    }

    @Override
    public Map<Long, String> getCommentsForPCs() {
        return null;
    }

    @Override
    public void setExpression(long pc, String expression) throws ParseException {

    }

    @Override
    public String getExpression(long pc) {
        return null;
    }

    @Override
    public String evaluateExpression(CpuState state) throws EvaluationException {
        return null;
    }

    @Override
    public Map<Long, String> getExpressions() {
        return null;
    }

    @Override
    public void setColor(long pc, Color color) {

    }

    @Override
    public Color getColor(CpuState state) {
        return null;
    }

    @Override
    public Map<Long, Color> getColors() {
        return null;
    }

    @Override
    public ABI getABI() {
        return null;
    }

    @Override
    public void addABIChangeListener(ChangeListener l) {

    }

    @Override
    public UserTypeDatabase getTypeDatabase() {
        return null;
    }

    @Override
    public TypedMemory getTypedMemory() {
        return null;
    }

    public DynamicTypePropagation getTypeRecovery() {
        return null;
    }
}
