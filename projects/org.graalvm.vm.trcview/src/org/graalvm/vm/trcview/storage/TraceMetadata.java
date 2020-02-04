package org.graalvm.vm.trcview.storage;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.util.Units;

public class TraceMetadata {
    public final String id;
    public final String name;
    public final short arch;
    public final long steps;

    public TraceMetadata(String id, String name, short arch, long steps) {
        this.id = id;
        this.name = name;
        this.arch = arch;
        this.steps = steps;
    }

    @Override
    public String toString() {
        Architecture a = Architecture.getArchitecture(arch);
        if (a != null) {
            return name + " [" + Units.si(steps) + " steps, " + a.getName() + "]";
        } else {
            return name + " [" + Units.si(steps) + " steps, unknown architecture]";
        }
    }
}
