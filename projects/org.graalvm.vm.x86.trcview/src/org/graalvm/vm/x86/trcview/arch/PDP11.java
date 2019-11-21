package org.graalvm.vm.x86.trcview.arch;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.x86.trcview.decode.CallDecoder;
import org.graalvm.vm.x86.trcview.decode.SyscallDecoder;
import org.graalvm.vm.x86.trcview.decode.pdp11.PDP11CallDecoder;
import org.graalvm.vm.x86.trcview.decode.pdp11.PDP11SyscallDecoder;
import org.graalvm.vm.x86.trcview.io.data.ArchTraceReader;
import org.graalvm.vm.x86.trcview.io.data.EventParser;
import org.graalvm.vm.x86.trcview.io.data.StepFormat;
import org.graalvm.vm.x86.trcview.io.data.pdp11.PDP11EventParser;
import org.graalvm.vm.x86.trcview.io.data.pdp11.PDP11TraceReader;

public class PDP11 extends Architecture {
    public static final short ID = Elf.EM_PDP11;
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_OCT, 6, 6, 2, false);

    private static final SyscallDecoder syscallDecoder = new PDP11SyscallDecoder();
    private static final CallDecoder callDecoder = new PDP11CallDecoder();
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
}
