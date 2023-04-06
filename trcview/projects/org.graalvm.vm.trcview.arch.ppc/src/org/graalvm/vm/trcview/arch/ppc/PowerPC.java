package org.graalvm.vm.trcview.arch.ppc;

import java.io.InputStream;
import java.util.Arrays;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
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
    public int getRegisterId(String name) {
        switch (name.toLowerCase()) {
            case "r0":
            case "gpr0":
                return 0;
            case "r1":
            case "gpr1":
            case "sp":
                return 1;
            case "r2":
            case "gpr2":
                return 2;
            case "r3":
            case "gpr3":
                return 3;
            case "r4":
            case "gpr4":
                return 4;
            case "r5":
            case "gpr5":
                return 5;
            case "r6":
            case "gpr6":
                return 6;
            case "r7":
            case "gpr7":
                return 7;
            case "r8":
            case "gpr8":
                return 8;
            case "r9":
            case "gpr9":
                return 9;
            case "r10":
            case "gpr10":
                return 10;
            case "r11":
            case "gpr11":
                return 11;
            case "r12":
            case "gpr12":
                return 12;
            case "r13":
            case "gpr13":
                return 13;
            case "r14":
            case "gpr14":
                return 14;
            case "r15":
            case "gpr15":
                return 15;
            case "r16":
            case "gpr16":
                return 16;
            case "r17":
            case "gpr17":
                return 17;
            case "r18":
            case "gpr18":
                return 18;
            case "r19":
            case "gpr19":
                return 19;
            case "r20":
            case "gpr20":
                return 20;
            case "r21":
            case "gpr21":
                return 21;
            case "r22":
            case "gpr22":
                return 22;
            case "r23":
            case "gpr23":
                return 23;
            case "r24":
            case "gpr24":
                return 24;
            case "r25":
            case "gpr25":
                return 25;
            case "r26":
            case "gpr26":
                return 26;
            case "r27":
            case "gpr27":
                return 27;
            case "r28":
            case "gpr28":
                return 28;
            case "r29":
            case "gpr29":
                return 29;
            case "r30":
            case "gpr30":
                return 30;
            case "r31":
            case "gpr31":
                return 31;
            default:
                return -1;
        }
    }

    @Override
    public int getRegisterCount() {
        return 32;
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

    @Override
    public void addStandardTypes(UserTypeDatabase types) {
        PowerPCTypes.add(types);
    }
}
