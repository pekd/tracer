package org.graalvm.vm.x86.trcview.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.BEOutputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.net.protocol.CommandRecord;
import org.graalvm.vm.x86.trcview.net.protocol.CommandResponseRecord;
import org.graalvm.vm.x86.trcview.net.protocol.IO.FakeBlockNode;
import org.graalvm.vm.x86.trcview.net.protocol.RpcRecord;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.AddSubroutine;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetBase;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetChildren;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetComputedSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetFileOffset;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetFilename;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetI64;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetI8;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetInstruction;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetLastRead;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetLastWrite;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetLoadBias;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetLocations;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetMapNode;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNextPC;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNextRead;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNextStep;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNode;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetOffset;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetPreviousStep;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetRoot;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetStepCount;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetSubroutines;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetSymbols;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Reanalyze;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.RenameSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.SetPrototype;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetBaseResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetChildrenResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetComputedSymbolResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetFileOffsetResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetFilenameResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetI64Result;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetI8Result;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetInstructionResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetLastReadResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetLastWriteResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetLoadBiasResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetLocationsResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetMapNodeResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetNextPCResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetNextReadResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetNextStepResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetNodeResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetOffsetResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetPreviousStepResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetRootResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetStepCountResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetSubroutinesResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetSymbolResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetSymbolsResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.Result;
import org.graalvm.vm.x86.trcview.ui.event.ChangeListener;

public class Client implements TraceAnalyzer, Closeable {
    private static final Logger log = Trace.create(Client.class);

    private Socket remote;
    private WordInputStream in;
    private WordOutputStream out;

    private Map<Long, Node> nodeCache;

    private List<SymbolRenameListener> symbolRenameListeners;
    private List<ChangeListener> symbolChangeListeners;

    public Client(String hostname, int port) throws IOException {
        remote = new Socket(hostname, port);
        in = new BEInputStream(new BufferedInputStream(remote.getInputStream()));
        out = new BEOutputStream(new BufferedOutputStream(remote.getOutputStream()));
        nodeCache = new HashMap<>();
        symbolRenameListeners = new ArrayList<>();
        symbolChangeListeners = new ArrayList<>();
    }

    public void close() throws IOException {
        remote.close();
    }

    @SuppressWarnings("unchecked")
    private synchronized <T extends Result> T execute(Command cmd) {
        try {
            CommandRecord cmdrec = new CommandRecord(cmd);
            cmdrec.write(out);
            out.flush();
            CommandResponseRecord result = RpcRecord.read(in);
            if (result.getResult().getType() != cmd.getType()) {
                throw new IOException("Error: invalid response " + result.getResult().getType() + " for request " + cmd.getType());
            }
            return (T) result.getResult();
        } catch (IOException e) {
            log.log(Levels.ERROR, "RPC call failed: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Symbol getSymbol(long pc) {
        GetSymbolResult sym = execute(new GetSymbol(pc));
        if (sym == null) {
            return null;
        }
        return sym.getSymbol();
    }

    @Override
    public ComputedSymbol getComputedSymbol(long pc) {
        GetComputedSymbolResult sym = execute(new GetComputedSymbol(pc));
        if (sym == null) {
            return null;
        }
        return sym.getSymbol();
    }

    @Override
    public void renameSymbol(ComputedSymbol sym, String name) {
        execute(new RenameSymbol(sym.address, name));
        fireSymbolRename(sym);
    }

    @Override
    public void setPrototype(ComputedSymbol sym, Prototype prototype) {
        execute(new SetPrototype(sym.address, prototype));
        fireSymbolRename(sym);
    }

    @Override
    public Set<ComputedSymbol> getSubroutines() {
        GetSubroutinesResult subs = execute(new GetSubroutines());
        if (subs == null) {
            return Collections.emptySet();
        }
        return subs.getSubroutines();
    }

    @Override
    public Set<ComputedSymbol> getLocations() {
        GetLocationsResult subs = execute(new GetLocations());
        if (subs == null) {
            return Collections.emptySet();
        }
        return subs.getLocations();
    }

    @Override
    public Collection<ComputedSymbol> getSymbols() {
        GetSymbolsResult subs = execute(new GetSymbols());
        if (subs == null) {
            return Collections.emptySet();
        }
        return subs.getSymbols();
    }

    @Override
    public Map<String, List<ComputedSymbol>> getNamedSymbols() {
        Map<String, List<ComputedSymbol>> map = new HashMap<>();
        for (ComputedSymbol sym : getSymbols()) {
            List<ComputedSymbol> entry = map.get(sym.name);
            if (entry == null) {
                entry = new ArrayList<>();
                map.put(sym.name, entry);
            }
            entry.add(sym);
        }
        return map;
    }

    @Override
    public void addSymbolRenameListener(SymbolRenameListener listener) {
        symbolRenameListeners.add(listener);
    }

    @Override
    public void addSymbolChangeListener(ChangeListener listener) {
        symbolChangeListeners.add(listener);
    }

    protected void fireSymbolRename(ComputedSymbol sym) {
        for (SymbolRenameListener l : symbolRenameListeners) {
            try {
                l.symbolRenamed(sym);
            } catch (Throwable t) {
                log.log(Levels.WARNING, "Error while executing listener: " + t, t);
            }
        }
    }

    protected void fireSymbolChange() {
        for (ChangeListener l : symbolChangeListeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.log(Levels.WARNING, "Error while executing listener: " + t, t);
            }
        }
    }

    @Override
    public void addSubroutine(long pc, String name, Prototype prototype) {
        ComputedSymbol sym = new ComputedSymbol(name, pc, ComputedSymbol.Type.SUBROUTINE);
        sym.prototype = prototype;
        execute(new AddSubroutine(sym));
    }

    @Override
    public void reanalyze() {
        execute(new Reanalyze());
        fireSymbolChange();
    }

    @Override
    public void refresh() {
        fireSymbolChange();
    }

    @Override
    public long getInstructionCount() {
        GetStepCountResult steps = execute(new GetStepCount());
        if (steps == null) {
            return 0;
        } else {
            return steps.getSteps();
        }
    }

    @Override
    public BlockNode getRoot() {
        GetRootResult root = execute(new GetRoot());
        return root.getRoot();
    }

    @Override
    public BlockNode getParent(Node node) {
        BlockNode parent = node.getParent();
        if (parent instanceof FakeBlockNode) {
            FakeBlockNode fake = (FakeBlockNode) parent;
            return (BlockNode) getInstruction(fake.getInstructionCount());
        }
        if (parent == null) {
            return null;
        } else if (parent.getHead() == null) {
            return getRoot();
        }
        return (BlockNode) getInstruction(parent.getHead().getInstructionCount());
    }

    private static long getId(Node node) {
        return node.getId();
    }

    @Override
    public BlockNode getChildren(BlockNode node) {
        long id = getId(node);
        if (nodeCache.containsKey(id)) {
            return (BlockNode) nodeCache.get(id);
        }
        GetChildrenResult children = execute(new GetChildren(getId(node)));
        if (children == null) {
            return null;
        }
        nodeCache.put(id, children.getNode());
        return children.getNode();
    }

    @Override
    public Node getNode(Node node) {
        long id = getId(node);
        if (nodeCache.containsKey(id)) {
            return nodeCache.get(id);
        }
        GetNodeResult result = execute(new GetNode(getId(node)));
        if (result == null) {
            return null;
        }
        nodeCache.put(id, result.getNode());
        return result.getNode();
    }

    @Override
    public Node getInstruction(long insn) {
        GetInstructionResult result = execute(new GetInstruction(insn));
        if (result == null) {
            return null;
        }
        return result.getNode();
    }

    @Override
    public Node getNextStep(Node node) {
        GetNextStepResult result = execute(new GetNextStep(getId(node)));
        if (result == null) {
            return null;
        }
        return result.getNode();
    }

    @Override
    public Node getPreviousStep(Node node) {
        GetPreviousStepResult result = execute(new GetPreviousStep(getId(node)));
        if (result == null) {
            return null;
        }
        return result.getNode();
    }

    @Override
    public Node getNextPC(Node node, long pc) {
        GetNextPCResult result = execute(new GetNextPC(getId(node), pc));
        if (result == null) {
            return null;
        }
        return result.getNode();
    }

    @Override
    public byte getI8(long address, long insn) throws MemoryNotMappedException {
        GetI8Result i8 = execute(new GetI8(address, insn));
        if (i8.getError() != 0) {
            throw new MemoryNotMappedException("no memory mapped");
        }
        return i8.getValue();
    }

    @Override
    public long getI64(long address, long insn) throws MemoryNotMappedException {
        GetI64Result i64 = execute(new GetI64(address, insn));
        if (i64.getError() != 0) {
            throw new MemoryNotMappedException("no memory mapped");
        }
        return i64.getValue();
    }

    @Override
    public MemoryRead getLastRead(long address, long insn) throws MemoryNotMappedException {
        GetLastReadResult read = execute(new GetLastRead(address, insn));
        if (read.getRead() == null) {
            throw new MemoryNotMappedException("no memory mapped");
        }
        return read.getRead();
    }

    @Override
    public MemoryRead getNextRead(long address, long insn) throws MemoryNotMappedException {
        GetNextReadResult read = execute(new GetNextRead(address, insn));
        if (read.getRead() == null) {
            throw new MemoryNotMappedException("no memory mapped");
        }
        return read.getRead();
    }

    @Override
    public MemoryUpdate getLastWrite(long address, long insn) throws MemoryNotMappedException {
        GetLastWriteResult write = execute(new GetLastWrite(address, insn));
        if (write.getWrite() == null) {
            throw new MemoryNotMappedException("no memory mapped");
        }
        return write.getWrite();
    }

    @Override
    public Node getMapNode(long address, long insn) throws MemoryNotMappedException {
        GetMapNodeResult node = execute(new GetMapNode(address, insn));
        return node.getNode();
    }

    @Override
    public long getBase(long pc) {
        GetBaseResult base = execute(new GetBase(pc));
        return base.getBase();
    }

    @Override
    public long getLoadBias(long pc) {
        GetLoadBiasResult result = execute(new GetLoadBias(pc));
        return result.getLoadBias();
    }

    @Override
    public long getOffset(long pc) {
        GetOffsetResult result = execute(new GetOffset(pc));
        return result.getOffset();
    }

    @Override
    public long getFileOffset(long pc) {
        GetFileOffsetResult result = execute(new GetFileOffset(pc));
        return result.getOffset();
    }

    @Override
    public String getFilename(long pc) {
        GetFilenameResult result = execute(new GetFilename(pc));
        return result.getFilename();
    }
}
