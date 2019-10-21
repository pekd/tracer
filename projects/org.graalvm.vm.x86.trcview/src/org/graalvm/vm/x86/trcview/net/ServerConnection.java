package org.graalvm.vm.x86.trcview.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.util.io.BEInputStream;
import org.graalvm.vm.util.io.BEOutputStream;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.analysis.Search;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.net.protocol.CommandRecord;
import org.graalvm.vm.x86.trcview.net.protocol.CommandResponseRecord;
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
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetMapNode;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNextPC;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNextRead;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetNextStep;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetOffset;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetPreviousStep;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.RenameSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.SetPrototype;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.AddSubroutineResult;
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
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetOffsetResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetPreviousStepResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetRootResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetStepCountResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetSubroutinesResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetSymbolResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetSymbolsResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.ReanalyzeResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.RenameSymbolResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.Result;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.SetPrototypeResult;

public class ServerConnection extends Thread implements AutoCloseable {
    private static final Logger log = Trace.create(ServerConnection.class);

    private ServerData data;

    private Socket client;
    private WordInputStream in;
    private WordOutputStream out;

    public ServerConnection(Socket client, ServerData data) throws IOException {
        this.client = client;
        this.data = data;
        in = new BEInputStream(new BufferedInputStream(client.getInputStream()));
        out = new BEOutputStream(new BufferedOutputStream(client.getOutputStream()));
    }

    public void close() throws IOException {
        client.close();
    }

    @Override
    public void run() {
        try {
            while (true) {
                CommandRecord req = RpcRecord.read(in);
                Command cmd = req.getCommand();
                Result result;
                switch (cmd.getType()) {
                    case Command.GET_SYMBOL:
                        result = getSymbol((GetSymbol) cmd);
                        break;
                    case Command.GET_COMPUTED_SYMBOL:
                        result = getComputedSymbol((GetComputedSymbol) cmd);
                        break;
                    case Command.RENAME_SYMBOL:
                        result = renameSymbol((RenameSymbol) cmd);
                        break;
                    case Command.SET_PROTOTYPE:
                        result = setPrototype((SetPrototype) cmd);
                        break;
                    case Command.GET_SUBROUTINES:
                        result = getSubroutines();
                        break;
                    case Command.GET_LOCATIONS:
                        result = getLocations();
                        break;
                    case Command.GET_SYMBOLS:
                        result = getSymbols();
                        break;
                    case Command.ADD_SUBROUTINE:
                        result = addSubroutine((AddSubroutine) cmd);
                        break;
                    case Command.REANALYZE:
                        result = reanalyze();
                        break;
                    case Command.GET_STEP_COUNT:
                        result = getStepCount();
                        break;
                    case Command.GET_ROOT:
                        result = getRoot();
                        break;
                    case Command.GET_CHILDREN:
                        result = getChildren((GetChildren) cmd);
                        break;
                    case Command.GET_INSTRUCTION:
                        result = getInstruction((GetInstruction) cmd);
                        break;
                    case Command.GET_NEXT_STEP:
                        result = getNextStep((GetNextStep) cmd);
                        break;
                    case Command.GET_PREVIOUS_STEP:
                        result = getPreviousStep((GetPreviousStep) cmd);
                        break;
                    case Command.GET_NEXT_PC:
                        result = getNextPC((GetNextPC) cmd);
                        break;
                    case Command.GET_I8:
                        result = getI8((GetI8) cmd);
                        break;
                    case Command.GET_I64:
                        result = getI64((GetI64) cmd);
                        break;
                    case Command.GET_LAST_READ:
                        result = getLastRead((GetLastRead) cmd);
                        break;
                    case Command.GET_NEXT_READ:
                        result = getNextRead((GetNextRead) cmd);
                        break;
                    case Command.GET_LAST_WRITE:
                        result = getLastWrite((GetLastWrite) cmd);
                        break;
                    case Command.GET_MAP_NODE:
                        result = getMapNode((GetMapNode) cmd);
                        break;
                    case Command.GET_BASE:
                        result = getBase((GetBase) cmd);
                        break;
                    case Command.GET_LOAD_BIAS:
                        result = getLoadBias((GetLoadBias) cmd);
                        break;
                    case Command.GET_OFFSET:
                        result = getOffset((GetOffset) cmd);
                        break;
                    case Command.GET_FILE_OFFSET:
                        result = getFileOffset((GetFileOffset) cmd);
                        break;
                    case Command.GET_FILENAME:
                        result = getFilename((GetFilename) cmd);
                        break;
                    default:
                        throw new IOException("unknown cmd type: " + cmd.getType());
                }
                CommandResponseRecord rsp = new CommandResponseRecord(result);
                rsp.write(out);
                out.flush();
            }
        } catch (EOFException e) {
            log.log(Levels.INFO, "Client closed connection");
        } catch (IOException e) {
            log.log(Levels.WARNING, "Exception in RPC code: " + e, e);
        }
    }

    private GetSymbolResult getSymbol(GetSymbol req) {
        long pc = req.getPC();
        Symbol sym = data.resolver.getSymbol(pc);
        return new GetSymbolResult(sym);
    }

    private GetComputedSymbolResult getComputedSymbol(GetComputedSymbol req) {
        long pc = req.getPC();
        ComputedSymbol sym = data.symbols.get(pc);
        return new GetComputedSymbolResult(sym);
    }

    private RenameSymbolResult renameSymbol(RenameSymbol req) {
        long pc = req.getPC();
        String name = req.getName();
        ComputedSymbol sym = data.symbols.get(pc);
        if (sym != null) {
            data.symbols.renameSubroutine(sym, name);
        }
        return new RenameSymbolResult();
    }

    private SetPrototypeResult setPrototype(SetPrototype req) {
        long pc = req.getPC();
        Prototype proto = req.getPrototype();
        ComputedSymbol sym = data.symbols.get(pc);
        if (sym != null) {
            data.symbols.setPrototype(sym, proto);
        }
        return new SetPrototypeResult();
    }

    private GetSubroutinesResult getSubroutines() {
        return new GetSubroutinesResult(data.symbols.getSubroutines());
    }

    private GetLocationsResult getLocations() {
        return new GetLocationsResult(data.symbols.getLocations());
    }

    private GetSymbolsResult getSymbols() {
        return new GetSymbolsResult(data.symbols.getSymbols());
    }

    private AddSubroutineResult addSubroutine(AddSubroutine req) {
        ComputedSymbol sym = req.getSubroutine();
        Prototype proto = sym.prototype;
        data.symbols.addSubroutine(sym.address, sym.name);
        sym = data.symbols.get(sym.address);
        sym.prototype = proto;
        return new AddSubroutineResult();
    }

    private void analyzeBlock(BlockNode block) {
        for (Node node : block.getNodes()) {
            data.symbols.visit(node);
            if (node instanceof BlockNode) {
                analyzeBlock((BlockNode) node);
            }
        }
    }

    private ReanalyzeResult reanalyze() {
        data.symbols.getSymbols().forEach(ComputedSymbol::resetVisits);
        analyzeBlock(data.root);
        data.symbols.cleanup();
        return new ReanalyzeResult();
    }

    private GetStepCountResult getStepCount() {
        return new GetStepCountResult(data.steps);
    }

    private GetRootResult getRoot() {
        return new GetRootResult(data.root);
    }

    private Node get(long id) {
        if (id == -1) {
            return data.root;
        } else {
            return data.nodes.get((int) id);
        }
    }

    private GetChildrenResult getChildren(GetChildren req) {
        Node node = get(req.getInstructionCount());
        if (node instanceof BlockNode) {
            return new GetChildrenResult((BlockNode) node);
        } else {
            throw new IllegalArgumentException("not a block node: " + req.getInstructionCount() + " [" + node.getClass().getCanonicalName() + "]");
        }
    }

    private GetInstructionResult getInstruction(GetInstruction req) {
        long insn = req.getInstructionCount();
        Node node = insn == -1 ? data.root : Search.instruction(data.root, insn);
        return new GetInstructionResult(node);
    }

    private GetNextStepResult getNextStep(GetNextStep req) {
        Node node = get(req.getInstructionCount());
        return new GetNextStepResult(Search.nextStep(node));
    }

    private GetPreviousStepResult getPreviousStep(GetPreviousStep req) {
        Node node = get(req.getInstructionCount());
        return new GetPreviousStepResult(Search.previousStep(node));
    }

    private GetNextPCResult getNextPC(GetNextPC req) {
        Node node = get(req.getInstructionCount());
        long pc = req.getPC();
        return new GetNextPCResult(Search.nextPC(node, pc));
    }

    private GetI8Result getI8(GetI8 req) {
        long addr = req.getAddress();
        long insn = req.getInstructionCount();
        try {
            return new GetI8Result(data.memory.getByte(addr, insn), 0);
        } catch (MemoryNotMappedException e) {
            return new GetI8Result((byte) 0, 1);
        }
    }

    private GetI64Result getI64(GetI64 req) {
        long addr = req.getAddress();
        long insn = req.getInstructionCount();
        try {
            return new GetI64Result(data.memory.getByte(addr, insn), 0);
        } catch (MemoryNotMappedException e) {
            return new GetI64Result((byte) 0, 1);
        }
    }

    private GetLastReadResult getLastRead(GetLastRead req) {
        long addr = req.getAddress();
        long insn = req.getInstructionCount();
        try {
            return new GetLastReadResult(data.memory.getLastRead(addr, insn));
        } catch (MemoryNotMappedException e) {
            return new GetLastReadResult(null);
        }
    }

    private GetNextReadResult getNextRead(GetNextRead req) {
        long addr = req.getAddress();
        long insn = req.getInstructionCount();
        try {
            return new GetNextReadResult(data.memory.getNextRead(addr, insn));
        } catch (MemoryNotMappedException e) {
            return new GetNextReadResult(null);
        }
    }

    private GetLastWriteResult getLastWrite(GetLastWrite req) {
        long addr = req.getAddress();
        long insn = req.getInstructionCount();
        try {
            return new GetLastWriteResult(data.memory.getLastWrite(addr, insn));
        } catch (MemoryNotMappedException e) {
            return new GetLastWriteResult(null);
        }
    }

    private GetMapNodeResult getMapNode(GetMapNode req) {
        long addr = req.getAddress();
        long insn = req.getInstructionCount();
        try {
            return new GetMapNodeResult(data.memory.getMapNode(addr, insn));
        } catch (MemoryNotMappedException e) {
            return new GetMapNodeResult(null);
        }
    }

    private GetBaseResult getBase(GetBase req) {
        long pc = req.getPC();
        return new GetBaseResult(data.files.getBase(pc));
    }

    private GetLoadBiasResult getLoadBias(GetLoadBias req) {
        long pc = req.getPC();
        return new GetLoadBiasResult(data.files.getLoadBias(pc));
    }

    private GetOffsetResult getOffset(GetOffset req) {
        long pc = req.getPC();
        return new GetOffsetResult(data.files.getOffset(pc));
    }

    private GetFileOffsetResult getFileOffset(GetFileOffset req) {
        long pc = req.getPC();
        return new GetFileOffsetResult(data.files.getFileOffset(pc));
    }

    private GetFilenameResult getFilename(GetFilename req) {
        long pc = req.getPC();
        return new GetFilenameResult(data.files.getFilename(pc));
    }
}
