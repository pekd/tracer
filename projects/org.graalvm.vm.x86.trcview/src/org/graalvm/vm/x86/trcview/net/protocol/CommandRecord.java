package org.graalvm.vm.x86.trcview.net.protocol;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.AddListener;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.AddSubroutine;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetBase;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetChildren;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetComputedSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetFileOffset;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetFilename;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.GetI16;
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
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Nop;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Reanalyze;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.RenameSymbol;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.SetPrototype;

public class CommandRecord extends RpcRecord {
    public static final int MAGIC = 0x434D4430; // CMD0

    private Command cmd;

    protected CommandRecord() {
        super(MAGIC);
    }

    public CommandRecord(Command cmd) {
        super(MAGIC);
        this.cmd = cmd;
    }

    public Command getCommand() {
        return cmd;
    }

    @Override
    protected void writeData(WordOutputStream out) throws IOException {
        out.write32bit(cmd.getType());
        cmd.write(out);
    }

    @Override
    protected void parse(WordInputStream in) throws IOException {
        int type = in.read32bit();

        switch (type) {
            case Command.NOP:
                cmd = new Nop();
                break;
            case Command.GET_SYMBOL:
                cmd = new GetSymbol();
                break;
            case Command.GET_COMPUTED_SYMBOL:
                cmd = new GetComputedSymbol();
                break;
            case Command.RENAME_SYMBOL:
                cmd = new RenameSymbol();
                break;
            case Command.SET_PROTOTYPE:
                cmd = new SetPrototype();
                break;
            case Command.GET_SUBROUTINES:
                cmd = new GetSubroutines();
                break;
            case Command.GET_LOCATIONS:
                cmd = new GetLocations();
                break;
            case Command.GET_SYMBOLS:
                cmd = new GetSymbols();
                break;
            case Command.ADD_LISTENER:
                cmd = new AddListener();
                break;
            case Command.ADD_SUBROUTINE:
                cmd = new AddSubroutine();
                break;
            case Command.REANALYZE:
                cmd = new Reanalyze();
                break;
            case Command.GET_STEP_COUNT:
                cmd = new GetStepCount();
                break;
            case Command.GET_ROOT:
                cmd = new GetRoot();
                break;
            case Command.GET_CHILDREN:
                cmd = new GetChildren();
                break;
            case Command.GET_NODE:
                cmd = new GetNode();
                break;
            case Command.GET_INSTRUCTION:
                cmd = new GetInstruction();
                break;
            case Command.GET_NEXT_STEP:
                cmd = new GetNextStep();
                break;
            case Command.GET_PREVIOUS_STEP:
                cmd = new GetPreviousStep();
                break;
            case Command.GET_NEXT_PC:
                cmd = new GetNextPC();
                break;
            case Command.GET_I8:
                cmd = new GetI8();
                break;
            case Command.GET_I16:
                cmd = new GetI16();
                break;
            case Command.GET_I64:
                cmd = new GetI64();
                break;
            case Command.GET_LAST_READ:
                cmd = new GetLastRead();
                break;
            case Command.GET_NEXT_READ:
                cmd = new GetNextRead();
                break;
            case Command.GET_LAST_WRITE:
                cmd = new GetLastWrite();
                break;
            case Command.GET_MAP_NODE:
                cmd = new GetMapNode();
                break;
            case Command.GET_BASE:
                cmd = new GetBase();
                break;
            case Command.GET_LOAD_BIAS:
                cmd = new GetLoadBias();
                break;
            case Command.GET_OFFSET:
                cmd = new GetOffset();
                break;
            case Command.GET_FILE_OFFSET:
                cmd = new GetFileOffset();
                break;
            case Command.GET_FILENAME:
                cmd = new GetFilename();
                break;
            default:
                throw new IOException("unknown type " + type);
        }
        assert cmd.getType() == type;
        cmd.read(in);
    }
}
