package org.graalvm.vm.trcview.arch.riscv;

import java.io.InputStream;
import java.util.Arrays;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.riscv.io.RiscVTraceReader;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallDecoder;
import org.graalvm.vm.trcview.decode.GenericSyscallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.expression.ast.AddNode;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.MulNode;
import org.graalvm.vm.trcview.expression.ast.SubNode;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.expression.ast.VariableNode;

public class RiscV extends Architecture {
    public static final short ID = Elf.EM_RISCV;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 16, 16, 1, true);

    private static final SyscallDecoder syscallDecoder = new GenericSyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "RV64G";
    }

    @Override
    public String getDescription() {
        return "RISC-V (RV64G)";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new RiscVTraceReader(in);
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
    public boolean isSystemLevel() {
        return false; // not system-level for now
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
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        abi.getCall().setArguments(Arrays.asList(new VariableNode("a0"), new VariableNode("a1"),
                        new VariableNode("a2"), new VariableNode("a3"), new VariableNode("a4"),
                        new VariableNode("a5"), new VariableNode("a6"), new VariableNode("a7")));
        // getU32(sp + (arg - 8) * 4);
        abi.getCall().setStack(new CallNode("getU32",
                        Arrays.asList(new AddNode(new VariableNode("sp"),
                                        new MulNode(new SubNode(new VariableNode("arg_id"), new ValueNode(4)),
                                                        new ValueNode(8))))));
        abi.getCall().setReturn(new VariableNode("a0"));

        abi.setSyscallId(new VariableNode("a7"));
        abi.getSyscall().setArguments(Arrays.asList(new VariableNode("a0"), new VariableNode("a1"),
                        new VariableNode("a2"), new VariableNode("a3"), new VariableNode("a4"),
                        new VariableNode("a5"), new VariableNode("a6")));
        abi.getSyscall().setReturn(new VariableNode("a0"));

        return abi;
    }
}
