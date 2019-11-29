package org.graalvm.vm.trcview.net;

import java.util.List;

import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.analysis.MappedFiles;
import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;

public class ServerData {
    public final SymbolResolver resolver;
    public final SymbolTable symbols;
    public final BlockNode root;
    public final MemoryTrace memory;
    public final MappedFiles files;
    public final long steps;
    public final List<Node> nodes;
    public final Architecture arch;

    public ServerData(Architecture arch, BlockNode root, Analysis analysis) {
        if (arch == null || root == null || analysis == null) {
            throw new NullPointerException();
        }
        this.root = root;
        this.arch = arch;
        resolver = analysis.getSymbolResolver();
        symbols = analysis.getComputedSymbolTable();
        memory = analysis.getMemoryTrace();
        files = analysis.getMappedFiles();
        steps = analysis.getStepCount();
        nodes = analysis.getNodes();
    }
}
