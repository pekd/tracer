package org.graalvm.vm.trcview.arch.ppc;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.ppc.decode.PowerPCCallDecoder;
import org.graalvm.vm.trcview.arch.ppc.decode.PowerPCSyscallDecoder;
import org.graalvm.vm.trcview.arch.ppc.io.PowerPCTraceReader;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;

public class PowerPC extends Architecture {
    public static final short ID = Elf.EM_PPC;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, true);

    private static final SyscallDecoder syscallDecoder = new PowerPCSyscallDecoder();
    private static final CallDecoder callDecoder = new PowerPCCallDecoder();

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
}
