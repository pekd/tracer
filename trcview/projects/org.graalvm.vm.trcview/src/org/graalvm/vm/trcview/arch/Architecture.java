package org.graalvm.vm.trcview.arch;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.graalvm.vm.posix.elf.ElfStrings;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.none.None;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.log.Trace;

public abstract class Architecture {
    private static final Logger log = Trace.create(Architecture.class);
    private static final Map<Short, Architecture> architectures = new HashMap<>();
    private static final ServiceLoader<Architecture> architectureLoader = ServiceLoader.load(Architecture.class);

    static {
        register(new None());
        for (Architecture arch : architectureLoader) {
            register(arch);
        }
    }

    public abstract short getId();

    public abstract String getName();

    public String getDescription() {
        return null;
    }

    public abstract ArchTraceReader getTraceReader(InputStream in);

    public abstract SyscallDecoder getSyscallDecoder();

    public abstract CallDecoder getCallDecoder();

    public abstract int getTabSize();

    public abstract StepFormat getFormat();

    public abstract boolean isSystemLevel();

    public abstract boolean isStackedTraps();

    public boolean isTaggedState() {
        return false;
    }

    public ArchitectureTypeInfo getTypeInfo() {
        return ArchitectureTypeInfo.LP64;
    }

    public ABI createABI(@SuppressWarnings("unused") UserTypeDatabase types) {
        return createABI();
    }

    public ABI createABI() {
        return null;
    }

    public int getRegisterCount() {
        return 0;
    }

    public int getRegisterId(@SuppressWarnings("unused") String name) {
        return -1;
    }

    public Disassembler getDisassembler(@SuppressWarnings("unused") TraceAnalyzer trc) {
        return null;
    }

    public void addStandardTypes(@SuppressWarnings("unused") UserTypeDatabase types) {
        // empty
    }

    public static void register(Architecture arch) {
        log.info("Registering architecture support for " + ElfStrings.getElfMachine(arch.getId()) + " [" + arch.getName() + "]");
        architectures.put(arch.getId(), arch);
    }

    public static void unregister(Architecture arch) {
        log.info("Removing architecture support for " + ElfStrings.getElfMachine(arch.getId()) + " [" + arch.getName() + "]");
        architectures.remove(arch.getId(), arch);
    }

    public static Architecture getArchitecture(short arch) {
        return architectures.get(arch);
    }

    public static List<Architecture> getArchitectures() {
        return architectures.values().stream().collect(Collectors.toList());
    }
}
