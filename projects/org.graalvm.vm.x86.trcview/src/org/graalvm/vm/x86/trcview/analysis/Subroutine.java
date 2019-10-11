package org.graalvm.vm.x86.trcview.analysis;

import org.graalvm.vm.x86.trcview.analysis.type.Prototype;

public interface Subroutine {
    String getName();

    Prototype getPrototype();
}
