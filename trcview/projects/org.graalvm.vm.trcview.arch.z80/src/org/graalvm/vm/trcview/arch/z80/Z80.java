package org.graalvm.vm.trcview.arch.z80;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.z80.disasm.Z80Disassembler;
import org.graalvm.vm.trcview.arch.z80.io.Z80TraceReader;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallDecoder;
import org.graalvm.vm.trcview.decode.GenericSyscallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class Z80 extends Architecture {
    public static final short ID = Elf.EM_Z80;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 4, 4, 1, false);
    public static final ArchitectureTypeInfo TYPE_INFO = new ArchitectureTypeInfo(2, 1, 1, 2);

    private static final SyscallDecoder syscallDecoder = new GenericSyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "Z80";
    }

    @Override
    public String getDescription() {
        return "Z80 CPU with support for PIO/SIO/CTC chips";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new Z80TraceReader(in);
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
    public ArchitectureTypeInfo getTypeInfo() {
        return TYPE_INFO;
    }

    @Override
    public Disassembler getDisassembler(TraceAnalyzer trc) {
        return new Z80Disassembler(trc);
    }

    @Override
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        // TODO: what even is "the standard ABI" on Z80?
        return abi;
    }
}
