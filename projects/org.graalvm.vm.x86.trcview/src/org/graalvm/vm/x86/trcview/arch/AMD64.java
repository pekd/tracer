package org.graalvm.vm.x86.trcview.arch;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.x86.trcview.decode.CallDecoder;
import org.graalvm.vm.x86.trcview.decode.SyscallDecoder;
import org.graalvm.vm.x86.trcview.decode.x86.AMD64CallDecoder;
import org.graalvm.vm.x86.trcview.decode.x86.AMD64SyscallDecoder;
import org.graalvm.vm.x86.trcview.io.data.ArchTraceReader;
import org.graalvm.vm.x86.trcview.io.data.EventParser;
import org.graalvm.vm.x86.trcview.io.data.x86.AMD64EventParser;
import org.graalvm.vm.x86.trcview.io.data.x86.AMD64TraceReader;

public class AMD64 extends Architecture {
    public static final short ID = Elf.EM_X86_64;

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
}
