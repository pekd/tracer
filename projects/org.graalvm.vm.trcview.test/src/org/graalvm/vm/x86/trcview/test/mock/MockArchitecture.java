package org.graalvm.vm.x86.trcview.test.mock;

import java.io.InputStream;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;

public class MockArchitecture extends Architecture {
    public static final short ID = (short) 0xFFFE;

    private static final MockEventParser eventParser = new MockEventParser();

    private final StepFormat format;
    private final boolean systemLevel;

    public MockArchitecture(boolean be, boolean systemLevel) {
        this.systemLevel = systemLevel;
        format = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, be);
    }

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public String getDescription() {
        return "Mock architecture";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return null;
    }

    @Override
    public EventParser getEventParser() {
        return eventParser;
    }

    @Override
    public SyscallDecoder getSyscallDecoder() {
        return null;
    }

    @Override
    public CallDecoder getCallDecoder() {
        return null;
    }

    @Override
    public int getTabSize() {
        return 8;
    }

    @Override
    public StepFormat getFormat() {
        return format;
    }

    @Override
    public boolean isSystemLevel() {
        return systemLevel;
    }

    @Override
    public boolean isStackedTraps() {
        return true;
    }
}
