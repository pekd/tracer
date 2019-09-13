package org.graalvm.vm.x86.trcview.analysis;

public interface SymbolRenameListener {
    void symbolRenamed(ComputedSymbol sym);
}
