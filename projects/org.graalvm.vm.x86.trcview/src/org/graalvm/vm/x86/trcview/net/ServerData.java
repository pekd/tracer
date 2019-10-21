package org.graalvm.vm.x86.trcview.net;

import java.util.List;

import org.graalvm.vm.posix.elf.SymbolResolver;
import org.graalvm.vm.x86.trcview.analysis.Analysis;
import org.graalvm.vm.x86.trcview.analysis.MappedFiles;
import org.graalvm.vm.x86.trcview.analysis.SymbolTable;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;

public class ServerData {
    public final SymbolResolver resolver;
    public final SymbolTable symbols;
    public final BlockNode root;
    public final MemoryTrace memory;
    public final MappedFiles files;
    public final long steps;
    public final List<Node> nodes;

    public ServerData(BlockNode root, Analysis analysis) {
        if (root == null || analysis == null) {
            throw new NullPointerException();
        }
        this.root = root;
        resolver = analysis.getSymbolResolver();
        symbols = analysis.getComputedSymbolTable();
        memory = analysis.getMemoryTrace();
        files = analysis.getMappedFiles();
        steps = analysis.getStepCount();
        nodes = analysis.getNodes();
    }
}
