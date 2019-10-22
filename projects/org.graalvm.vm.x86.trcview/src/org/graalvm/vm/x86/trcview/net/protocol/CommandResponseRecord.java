package org.graalvm.vm.x86.trcview.net.protocol;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.trcview.net.protocol.cmd.Command;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.AddListenerResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.AddSubroutineResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetBaseResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetChildrenResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetComputedSymbolResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetFileOffsetResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetFilenameResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.GetI16Result;
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
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.NopResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.ReanalyzeResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.RenameSymbolResult;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.Result;
import org.graalvm.vm.x86.trcview.net.protocol.cmdresult.SetPrototypeResult;

public class CommandResponseRecord extends RpcRecord {
    public static final int MAGIC = 0x434D4431; // CMD1

    private Result result;

    protected CommandResponseRecord() {
        super(MAGIC);
    }

    public CommandResponseRecord(Result result) {
        super(MAGIC);
        this.result = result;
    }

    public Result getResult() {
        return result;
    }

    @Override
    protected void writeData(WordOutputStream out) throws IOException {
        out.write32bit(result.getType());
        result.write(out);
    }

    @Override
    protected void parse(WordInputStream in) throws IOException {
        int type = in.read32bit();

        switch (type) {
            case Command.NOP:
                result = new NopResult();
                break;
            case Command.GET_SYMBOL:
                result = new GetSymbolResult();
                break;
            case Command.GET_COMPUTED_SYMBOL:
                result = new GetComputedSymbolResult();
                break;
            case Command.RENAME_SYMBOL:
                result = new RenameSymbolResult();
                break;
            case Command.SET_PROTOTYPE:
                result = new SetPrototypeResult();
                break;
            case Command.GET_SUBROUTINES:
                result = new GetSubroutinesResult();
                break;
            case Command.GET_LOCATIONS:
                result = new GetLocationsResult();
                break;
            case Command.GET_SYMBOLS:
                result = new GetSymbolsResult();
                break;
            case Command.ADD_LISTENER:
                result = new AddListenerResult();
                break;
            case Command.ADD_SUBROUTINE:
                result = new AddSubroutineResult();
                break;
            case Command.REANALYZE:
                result = new ReanalyzeResult();
                break;
            case Command.GET_STEP_COUNT:
                result = new GetStepCountResult();
                break;
            case Command.GET_ROOT:
                result = new GetRootResult();
                break;
            case Command.GET_CHILDREN:
                result = new GetChildrenResult();
                break;
            case Command.GET_NODE:
                result = new GetNodeResult();
                break;
            case Command.GET_INSTRUCTION:
                result = new GetInstructionResult();
                break;
            case Command.GET_NEXT_STEP:
                result = new GetNextStepResult();
                break;
            case Command.GET_PREVIOUS_STEP:
                result = new GetPreviousStepResult();
                break;
            case Command.GET_NEXT_PC:
                result = new GetNextPCResult();
                break;
            case Command.GET_I8:
                result = new GetI8Result();
                break;
            case Command.GET_I16:
                result = new GetI16Result();
                break;
            case Command.GET_I64:
                result = new GetI64Result();
                break;
            case Command.GET_LAST_READ:
                result = new GetLastReadResult();
                break;
            case Command.GET_NEXT_READ:
                result = new GetNextReadResult();
                break;
            case Command.GET_LAST_WRITE:
                result = new GetLastWriteResult();
                break;
            case Command.GET_MAP_NODE:
                result = new GetMapNodeResult();
                break;
            case Command.GET_BASE:
                result = new GetBaseResult();
                break;
            case Command.GET_LOAD_BIAS:
                result = new GetLoadBiasResult();
                break;
            case Command.GET_OFFSET:
                result = new GetOffsetResult();
                break;
            case Command.GET_FILE_OFFSET:
                result = new GetFileOffsetResult();
                break;
            case Command.GET_FILENAME:
                result = new GetFilenameResult();
                break;
            default:
                throw new IOException("unknown type " + type);
        }
        result.read(in);
    }
}
