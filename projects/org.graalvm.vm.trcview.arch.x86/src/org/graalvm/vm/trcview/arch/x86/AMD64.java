package org.graalvm.vm.trcview.arch.x86;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.x86.decode.AMD64CallDecoder;
import org.graalvm.vm.trcview.arch.x86.decode.AMD64SyscallDecoder;
import org.graalvm.vm.trcview.arch.x86.io.AMD64EventParser;
import org.graalvm.vm.trcview.arch.x86.io.AMD64TraceReader;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;

public class AMD64 extends Architecture {
    public static final short ID = Elf.EM_X86_64;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 16, 16, 1, false);

    private static final SyscallDecoder syscallDecoder = new AMD64SyscallDecoder();
    private static final CallDecoder callDecoder = new AMD64CallDecoder();
    private static final EventParser eventParser = new AMD64EventParser();

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
}
