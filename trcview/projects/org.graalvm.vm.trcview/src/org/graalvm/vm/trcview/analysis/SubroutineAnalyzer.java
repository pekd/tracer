package org.graalvm.vm.trcview.analysis;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.DefaultTypes;
import org.graalvm.vm.trcview.arch.BranchTarget;
import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.TraceCodeReader;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class SubroutineAnalyzer {
    private static final Logger log = Trace.create(SubroutineAnalyzer.class);

    private final TraceAnalyzer trc;
    private final long step;
    private final Disassembler disasm;
    private final TypedMemory mem;

    public SubroutineAnalyzer(TraceAnalyzer trc, long step) {
        this.trc = trc;
        this.step = step;
        this.disasm = trc.getArchitecture().getDisassembler(trc);
        this.mem = trc.getTypedMemory();
    }

    private CodeReader getCodeReader(long addr) {
        return new TraceCodeReader(trc, addr, trc.getArchitecture().getFormat().be, step);
    }

    public void analyzeStraightline(long address) {
        if (disasm == null) {
            return;
        }

        long addr = address;
        while (true) {
            try {
                trc.getMapNode(addr, step);
            } catch (MemoryNotMappedException e) {
                break;
            }

            // figure out instruction
            int len = disasm.getLength(getCodeReader(addr));
            if (len == 0) {
                break;
            }

            InstructionType type = disasm.getType(getCodeReader(addr));

            boolean finish;
            switch (type) {
                case JMP:
                case JMP_INDIRECT:
                case RET:
                case RTI:
                    finish = true;
                    break;
                default:
                    finish = false;
                    break;
            }

            Variable var = mem.get(addr);
            String name = null;
            if (var != null) {
                name = var.getRawName();
                trc.getTypedMemory().set(addr, null);
            }
            if (name != null) {
                mem.set(addr, DefaultTypes.getCodeType(len), name);
            } else {
                mem.set(addr, DefaultTypes.getCodeType(len));
            }

            if (finish) {
                break;
            } else {
                addr += len;
            }
        }
    }

    private void addLocation(long addr) {
        trc.addLocation(addr);
        log.log(Levels.INFO, () -> String.format("adding loc: 0x%X", addr));
    }

    private void addSubroutine(long addr) {
        // TODO: track the subroutine size here
        trc.addSubroutine(addr);
        log.log(Levels.INFO, () -> String.format("adding sub: 0x%X", addr));
    }

    public void analyzeCode(long address) {
        if (disasm == null) {
            return;
        }

        Set<Long> code = new HashSet<>();
        Set<Long> todo = new HashSet<>();

        todo.add(address);

        Node map;
        try {
            map = trc.getMapNode(address, step);
        } catch (MemoryNotMappedException e) {
            return;
        }

        while (!todo.isEmpty()) {
            long addr = todo.iterator().next();
            todo.remove(addr);
            if (!code.contains(addr)) {
                analyzeBlock(addr, code, todo, map);
            }
        }
    }

    public void analyzeBlock(long address, Set<Long> code, Set<Long> todo, Node node) {
        long addr = address;
        while (!code.contains(addr)) {
            try {
                Node n = trc.getMapNode(addr, step);
                // check if this is within the same memory segment; this is important to skip things
                // like empty RAM space
                if (n != node) {
                    break;
                }
            } catch (MemoryNotMappedException e) {
                break;
            }

            // figure out if this is a valid instruction as well as its size
            int len = disasm.getLength(getCodeReader(addr));
            if (len == 0) {
                break;
            }

            InstructionType type = disasm.getType(getCodeReader(addr));

            boolean finish;
            switch (type) {
                case JCC: {
                    BranchTarget bta = disasm.getBranchTarget(getCodeReader(addr));
                    if (bta != null && bta.isValid()) {
                        todo.add(bta.getBTA());
                        addLocation(bta.getBTA());
                    }
                    finish = false;
                    break;
                }
                case JMP: {
                    BranchTarget bta = disasm.getBranchTarget(getCodeReader(addr));
                    if (bta != null && bta.isValid()) {
                        todo.add(bta.getBTA());
                        addLocation(bta.getBTA());
                    }
                    finish = true;
                    break;
                }
                case JMP_INDIRECT:
                case RET:
                case RTI:
                    finish = true;
                    break;
                case CALL:
                    BranchTarget bta = disasm.getBranchTarget(getCodeReader(addr));
                    if (bta != null && bta.isValid()) {
                        todo.add(bta.getBTA());
                        addSubroutine(bta.getBTA());
                    }
                    finish = false;
                    break;
                default:
                    finish = false;
                    break;
            }

            code.add(addr);

            Variable var = mem.get(addr);
            String name = null;
            if (var != null) {
                name = var.getRawName();
                trc.getTypedMemory().set(addr, null);
            }

            if (name != null) {
                // there was a name; transfer it over
                ComputedSymbol sym = trc.getComputedSymbol(addr);
                if (sym != null) {
                    // this was a loc/sub, rename it
                    trc.renameSymbol(sym, name);
                    mem.set(addr, DefaultTypes.getCodeType(len));
                } else {
                    // this was just a normal instruction ... stupid, right?
                    mem.set(addr, DefaultTypes.getCodeType(len), name);
                }
            } else {
                mem.set(addr, DefaultTypes.getCodeType(len));
            }

            if (finish) {
                break;
            } else {
                addr += len;
            }
        }
    }
}
