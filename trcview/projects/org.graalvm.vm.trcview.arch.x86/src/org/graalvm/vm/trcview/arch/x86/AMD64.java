package org.graalvm.vm.trcview.arch.x86;

import java.io.InputStream;
import java.util.Arrays;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.x86.decode.AMD64Disassembler;
import org.graalvm.vm.trcview.arch.x86.decode.AMD64SyscallDecoder;
import org.graalvm.vm.trcview.arch.x86.io.AMD64TraceReader;
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

public class AMD64 extends Architecture {
    public static final short ID = Elf.EM_X86_64;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 16, 16, 1, false);

    private static final SyscallDecoder syscallDecoder = new AMD64SyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "x86_64";
    }

    @Override
    public String getDescription() {
        return "Generic x86_64 processor with Linux userspace";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new AMD64TraceReader(in);
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
        return false;
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
    public ArchitectureTypeInfo getTypeInfo() {
        return ArchitectureTypeInfo.LP64;
    }

    @Override
    public Disassembler getDisassembler(TraceAnalyzer trc) {
        return new AMD64Disassembler(trc);
    }

    @Override
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        abi.getCall().setArguments(Arrays.asList(new VariableNode("rdi"), new VariableNode("rsi"), new VariableNode("rdx"), new VariableNode("rcx"), new VariableNode("r8"), new VariableNode("r9")));
        abi.getCall().setReturn(new VariableNode("rax"));
        // getMem(rsp + arg_id * 8, arg_size)
        abi.getCall().setStack(new CallNode("getMem", Arrays.asList(new AddNode(new VariableNode("rsp"), new MulNode(new VariableNode("arg_id"), new ValueNode(8))), new VariableNode("arg_size"))));
        return abi;
    }
}
