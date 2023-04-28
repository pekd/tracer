package org.graalvm.vm.trcview.arch.vm;

import java.io.InputStream;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallDecoder;
import org.graalvm.vm.trcview.decode.GenericSyscallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;

public class VMArchitecture extends Architecture {
    private static final SyscallDecoder syscallDecoder = new GenericSyscallDecoder();
    private static final CallDecoder callDecoder = new GenericCallDecoder();

    private final short id;
    private final String name;
    private final String description;
    private final StepFormat format;

    public VMArchitecture(short id, String name, String description, StepFormat format) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.format = format;
    }

    @Override
    public short getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        return null;
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
        return 8;
    }

    @Override
    public StepFormat getFormat() {
        return format;
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
    public ABI createABI() {
        GenericABI abi = new GenericABI();
        return abi;
    }
}
