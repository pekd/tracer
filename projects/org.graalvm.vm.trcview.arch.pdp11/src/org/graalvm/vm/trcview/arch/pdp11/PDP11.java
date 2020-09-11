package org.graalvm.vm.trcview.arch.pdp11;

import java.io.InputStream;
import java.util.Arrays;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.pdp11.io.PDP11EventParser;
import org.graalvm.vm.trcview.arch.pdp11.io.PDP11TraceReader;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallDecoder;
import org.graalvm.vm.trcview.decode.GenericSyscallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
import org.graalvm.vm.trcview.expression.ast.XorNode;

public class PDP11 extends Architecture {
    public static final short ID = Elf.EM_PDP11;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_OCT, 6, 6, 2, false);

    private static final SyscallDecoder syscallDecoder = new GenericSyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();
    private static final EventParser eventParser = new PDP11EventParser();

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "LSI-11";
    }

    @Override
    public String getDescription() {
        return "PDP-11/03-L with RX02 and DLV11-J";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new PDP11TraceReader(in);
    }

    @Override
    public EventParser getEventParser() {
        return eventParser;
    }

    @Override
    public SyscallDecoder getSyscallDecoder() {
        return syscallDecoder;
    }

    @Override
    public CallDecoder getCallDecoder() {
        return callDecoder;
    }

    @Override
    public int getTabSize() {
        return 16;
    }

    @Override
    public StepFormat getFormat() {
        return FORMAT;
    }

    @Override
    public boolean isSystemLevel() {
        return true;
    }

    @Override
    public boolean isStackedTraps() {
        return true;
    }

    @Override
    public boolean isTaggedState() {
        return true;
    }

    @Override
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        abi.getCall().setArguments(Arrays.asList(new VariableNode("r0"), new VariableNode("r1"), new VariableNode("r2"), new VariableNode("r3")));
        abi.getCall().setReturn(new VariableNode("r0"));
        // use getU16(pc) ^ 0104000 to return the ID embedded in EMT instructions without making it
        // indistinguishable from BPT/IOT/TRAP
        abi.setSyscallId(new XorNode(new CallNode("getU16", Arrays.asList(new VariableNode("pc"))), new ValueNode(0104000)));
        abi.getSyscall().setArguments(Arrays.asList(new VariableNode("r0"), new VariableNode("r1"), new VariableNode("r2"), new VariableNode("r3")));
        abi.getSyscall().setReturn(new VariableNode("r0"));
        return abi;
    }
}
