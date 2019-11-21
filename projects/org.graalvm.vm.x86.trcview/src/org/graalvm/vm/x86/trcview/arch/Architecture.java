package org.graalvm.vm.x86.trcview.arch;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.ElfStrings;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.trcview.decode.CallDecoder;
import org.graalvm.vm.x86.trcview.decode.SyscallDecoder;
import org.graalvm.vm.x86.trcview.io.data.ArchTraceReader;
import org.graalvm.vm.x86.trcview.io.data.EventParser;
import org.graalvm.vm.x86.trcview.io.data.StepFormat;

public abstract class Architecture {
    private static final Logger log = Trace.create(Architecture.class);
    private static final Map<Short, Architecture> architectures = new HashMap<>();

    static {
        register(new None());
        register(new AMD64());
    }

    public abstract short getId();

    public abstract String getName();

    public abstract ArchTraceReader getTraceReader(InputStream in);

    public abstract EventParser getEventParser();

    public abstract SyscallDecoder getSyscallDecoder();

    public abstract CallDecoder getCallDecoder();

    public abstract int getTabSize();

    public abstract StepFormat getFormat();

    public static void register(Architecture arch) {
        log.info("Registering architecture support for " + ElfStrings.getElfMachine(arch.getId()) + " [" + arch.getName() + "]");
        architectures.put(arch.getId(), arch);
    }

    public static Architecture getArchitecture(short arch) {
        return architectures.get(arch);
    }
}
