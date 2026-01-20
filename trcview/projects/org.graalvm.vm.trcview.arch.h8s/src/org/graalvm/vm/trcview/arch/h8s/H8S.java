package org.graalvm.vm.trcview.arch.h8s;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.h8s.disasm.H8SDisassembler;
import org.graalvm.vm.trcview.arch.h8s.io.H8STraceReader;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallDecoder;
import org.graalvm.vm.trcview.decode.GenericSyscallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class H8S extends Architecture {
    public static final short ID = Elf.EM_H8S;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 6, 4, 2, true);
    public static final ArchitectureTypeInfo TYPE_INFO = new ArchitectureTypeInfo(4, 2, 2, 4);

    private static final SyscallDecoder syscallDecoder = new GenericSyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "H8S/2000";
    }

    @Override
    public String getDescription() {
        return "Hitachi H8S/2000 series";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new H8STraceReader(in);
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
        return new H8SDisassembler(trc);
    }

    @Override
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        return abi;
    }
}
