package org.graalvm.vm.trcview.arch.ppc;

import java.io.InputStream;
import java.util.Arrays;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.ppc.decode.PowerPCSyscallDecoder;
import org.graalvm.vm.trcview.arch.ppc.disasm.PowerPCDisassembler;
import org.graalvm.vm.trcview.arch.ppc.io.PowerPCTraceReader;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.expression.ast.AddNode;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.MulNode;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class PowerPC extends Architecture {
    public static final short ID = Elf.EM_PPC;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, true);

    private static final SyscallDecoder syscallDecoder = new PowerPCSyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "ppc32";
    }

    @Override
    public String getDescription() {
        return "PowerISA 2.07 (POWER8, 32bit)";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new PowerPCTraceReader(in);
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
        return 10;
    }

    @Override
    public StepFormat getFormat() {
        return FORMAT;
    }

    @Override
    public ArchitectureTypeInfo getTypeInfo() {
        return ArchitectureTypeInfo.ILP32;
    }

    @Override
    public boolean isSystemLevel() {
        return false;
    }

    @Override
    public boolean isStackedTraps() {
        return false;
    }

    @Override
    public boolean isTaggedState() {
        return true;
    }

    @Override
    public Disassembler getDisassembler(TraceAnalyzer trc) {
        return new PowerPCDisassembler(trc);
    }

    @Override
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        abi.getCall().setArguments(Arrays.asList(new VariableNode("r3"), new VariableNode("r4"), new VariableNode("r5"), new VariableNode("r6"), new VariableNode("r7"), new VariableNode("r8"),
                        new VariableNode("r9"), new VariableNode("r10")));
        abi.getCall().setReturn(new VariableNode("r3"));
        abi.getCall().setStack(new CallNode("getMem", Arrays.asList(new AddNode(new VariableNode("r2"), new MulNode(new VariableNode("arg_id"), new ValueNode(8))), new VariableNode("arg_size"))));
        return abi;
    }
}
