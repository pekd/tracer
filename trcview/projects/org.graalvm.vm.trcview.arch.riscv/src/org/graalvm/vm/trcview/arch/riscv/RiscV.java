package org.graalvm.vm.trcview.arch.riscv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.NameAlreadyUsedException;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.analysis.type.TypeAlias;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.riscv.disasm.RiscVDisassembler;
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
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.ResourceLoader;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class RiscV extends Architecture {
    private static final Logger log = Trace.create(RiscV.class);

    public static final short ID = Elf.EM_RISCV;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 16, 16, 1, false);

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
    public Disassembler getDisassembler(TraceAnalyzer trc) {
        return new RiscVDisassembler(trc);
    }

    @Override
    public void addStandardTypes(UserTypeDatabase types) {
        try {
            types.add(new TypeAlias("mode_t", new Type(DataType.U32)));
            types.add(new TypeAlias("uid_t", new Type(DataType.U32)));
            types.add(new TypeAlias("gid_t", new Type(DataType.U32)));
            types.add(new TypeAlias("pid_t", new Type(DataType.S32)));
            types.add(new TypeAlias("loff_t", new Type(DataType.S64)));
            types.add(new TypeAlias("off_t", new Type(DataType.S32)));
            types.add(new TypeAlias("clockid_t", new Type(DataType.S32)));
            types.add(new TypeAlias("timer_t", new Type(DataType.S32)));
            types.add(new TypeAlias("key_serial_t", new Type(DataType.S32)));
            types.add(new TypeAlias("key_t", new Type(DataType.S32)));
            types.add(new TypeAlias("qid_t", new Type(DataType.S32)));
            types.add(new TypeAlias("mqd_t", new Type(DataType.S32)));
            types.add(new TypeAlias("aio_context_t", new Type(DataType.U64)));
            types.add(new TypeAlias("time_t", new Type(getTypeInfo().getLongType(false))));
            Struct __user_cap_header_struct = new Struct("__user_cap_header_struct");
            __user_cap_header_struct.add("version", new Type(DataType.U32));
            __user_cap_header_struct.add("pid", new Type(DataType.S32));
            types.add(__user_cap_header_struct);
            types.add(new TypeAlias("cap_user_header_t", new Type(new Type(__user_cap_header_struct), getTypeInfo())));
            Struct __user_cap_data_struct = new Struct("__user_cap_data_struct");
            __user_cap_data_struct.add("effective", new Type(DataType.U32));
            __user_cap_data_struct.add("permitted", new Type(DataType.U32));
            __user_cap_data_struct.add("inheritable", new Type(DataType.U32));
            types.add(__user_cap_data_struct);
            types.add(new TypeAlias("cap_user_data_t", new Type(new Type(__user_cap_data_struct), getTypeInfo())));
            Struct fd_set = new Struct("fd_set");
            fd_set.add("fds_bits", new Type(DataType.U32, false, 1024 / 8));
            types.add(fd_set);
            Struct siginfo_t = new Struct("siginfo_t");
            siginfo_t.add("_si_pad", new Type(DataType.S32, false, 128 / 4));
            types.add(siginfo_t);
            Struct sigset_t = new Struct("sigset_t");
            sigset_t.add("sig", new Type(DataType.U64, false, 1));
            types.add(sigset_t);
        } catch (NameAlreadyUsedException e) {
            log.info("Name already used: " + e);
        }
    }

    @Override
    public ABI createABI(UserTypeDatabase types) {
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

        try {
            byte[] rawSyscallInfo = ResourceLoader.load(RiscV.class, "syscalls.lst");
            String syscallInfo = new String(rawSyscallInfo, StandardCharsets.UTF_8);
            abi.loadSyscallDefinitions(syscallInfo, types);
        } catch (IOException e) {
            log.log(Levels.WARNING, "Failed to load syscall list", e);
        }

        return abi;
    }
}
