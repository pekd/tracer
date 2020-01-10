package org.graalvm.vm.trcview.arch.custom;

import java.io.InputStream;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.custom.decode.CustomCallDecoder;
import org.graalvm.vm.trcview.arch.custom.io.GenericTraceReader;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;

public class GenericArchitecture extends Architecture {
    public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, true);
    public static final short ID = -1;

    @Override
    public short getId() {
        return ID;
    }

    @Override
    public String getName() {
        return "generic";
    }

    @Override
    public String getDescription() {
        return "Generic Architecture";
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return new GenericTraceReader(in);
    }

    @Override
    public EventParser getEventParser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyscallDecoder getSyscallDecoder() {
        return null;
    }

    @Override
    public CallDecoder getCallDecoder() {
        return new CustomCallDecoder();
    }

    @Override
    public int getTabSize() {
        return 8;
    }

    @Override
    public StepFormat getFormat() {
        return FORMAT;
    }

    @Override
    public boolean isSystemLevel() {
        return false;
    }
}
