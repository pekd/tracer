package org.graalvm.vm.trcview.analysis;

import org.graalvm.vm.trcview.analysis.type.Prototype;

public interface Subroutine {
    String getName();

    Prototype getPrototype();
}
