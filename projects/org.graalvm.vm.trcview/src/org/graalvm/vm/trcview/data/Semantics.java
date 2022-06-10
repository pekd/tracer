package org.graalvm.vm.trcview.data;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.data.ir.IndexedMemoryOperand;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.util.BitTest;

public class Semantics {
    private final CodeTypeMap codeMap;
    private final MemoryTypeMap memoryMap;
    private final SymbolTable symbols;
    private long pc;
    private CpuState state;

    private final MemoryAccessMap memory;

    public Semantics(CodeTypeMap codeMap, MemoryTypeMap memoryMap, SymbolTable symbols, MemoryAccessMap memory) {
        this.codeMap = codeMap;
        this.memoryMap = memoryMap;
        this.symbols = symbols;
        this.memory = memory;
    }

    public void setPC(long pc) {
        this.pc = pc;
    }

    public void setState(CpuState state) {
        this.state = state;
    }

    private Operand resolve(Operand op) {
        if (op instanceof IndexedMemoryOperand) {
            return resolve((IndexedMemoryOperand) op);
        } else {
            return op;
        }
    }

    private MemoryOperand resolve(IndexedMemoryOperand op) {
        long reg = state.getRegisterById(op.getRegister());
        long offset = op.getOffset();
        long address = reg + offset;
        return new MemoryOperand(address);
    }

    private RegisterOperand getRegister(Operand op) {
        if (op instanceof RegisterOperand) {
            return (RegisterOperand) op;
        } else if (op instanceof IndexedMemoryOperand) {
            return new RegisterOperand(((IndexedMemoryOperand) op).getRegister());
        } else {
            return null;
        }
    }

    private long get(Operand op) {
        if (op instanceof RegisterOperand) {
            return codeMap.get(pc, (RegisterOperand) op);
        } else if (op instanceof MemoryOperand) {
            return memoryMap.get((MemoryOperand) op);
        } else if (op instanceof IndexedMemoryOperand) {
            return memoryMap.get(resolve((IndexedMemoryOperand) op));
        } else {
            return 0;
        }
    }

    public void arithmetic(Operand op, boolean mul) {
        long bits = mul ? VariableType.MUL_BIT : VariableType.ADDSUB_BIT;
        if (op instanceof RegisterOperand) {
            codeMap.setBit(pc, (RegisterOperand) op, bits);
        } else if (op instanceof MemoryOperand) {
            memoryMap.step((MemoryOperand) op, state.getStep());
            memoryMap.setBit((MemoryOperand) op, bits);
        } else if (op instanceof IndexedMemoryOperand) {
            MemoryOperand mdst = resolve((IndexedMemoryOperand) op);
            memoryMap.step(mdst, state.getStep());
            memoryMap.setBit(mdst, bits);
        }
    }

    public void move(Operand dst, Operand src) {
        if (dst instanceof RegisterOperand) {
            RegisterOperand rdst = (RegisterOperand) dst;
            codeMap.set(pc, rdst, get(src));
            codeMap.breakChain(pc, rdst.getRegister());
        } else if (dst instanceof MemoryOperand) {
            MemoryOperand mdst = (MemoryOperand) dst;
            memoryMap.step(mdst, state.getStep());
            memoryMap.set(mdst, get(src));
        } else if (dst instanceof IndexedMemoryOperand) {
            MemoryOperand mdst = resolve((IndexedMemoryOperand) dst);
            memoryMap.step(mdst, state.getStep());
            memoryMap.set(mdst, get(src));
        }
        unify(src, dst);
    }

    public void constraint(Operand op, VariableType type) {
        if (op instanceof RegisterOperand) {
            RegisterOperand rop = (RegisterOperand) op;
            codeMap.constrain(pc, rop, type);
        } else if (op instanceof MemoryOperand) {
            MemoryOperand mop = (MemoryOperand) op;
            memoryMap.constrain(mop, type);
        } else if (op instanceof IndexedMemoryOperand) {
            MemoryOperand mop = resolve((IndexedMemoryOperand) op);
            memoryMap.constrain(mop, type);
        }
    }

    public void set(Operand op, VariableType type) {
        if (op instanceof RegisterOperand) {
            RegisterOperand rop = (RegisterOperand) op;
            codeMap.set(pc, rop, type);
            codeMap.breakChain(pc, rop.getRegister());
        } else if (op instanceof MemoryOperand) {
            MemoryOperand mop = (MemoryOperand) op;
            memoryMap.step(mop, state.getStep());
            memoryMap.set(mop, type);
        } else if (op instanceof IndexedMemoryOperand) {
            MemoryOperand mop = resolve((IndexedMemoryOperand) op);
            memoryMap.step(mop, state.getStep());
            memoryMap.set(mop, type);
        }
    }

    public void reset(Operand op) {
        if (op instanceof RegisterOperand) {
            RegisterOperand rop = (RegisterOperand) op;
            codeMap.set(pc, rop, 0);
            codeMap.breakChain(pc, rop.getRegister());
        } else if (op instanceof MemoryOperand) {
            MemoryOperand mop = (MemoryOperand) op;
            memoryMap.set(mop, 0);
        } else if (op instanceof IndexedMemoryOperand) {
            MemoryOperand mop = resolve((IndexedMemoryOperand) op);
            memoryMap.set(mop, 0);
        }
    }

    public void unify(Operand a, Operand b) {
        // TODO
        if (a instanceof RegisterOperand && b instanceof RegisterOperand) {
            RegisterOperand ra = (RegisterOperand) a;
            RegisterOperand rb = (RegisterOperand) b;
            codeMap.chain(pc, ra, rb);
        } else {
            Operand opa = resolve(a);
            Operand opb = resolve(b);
            if (opa instanceof MemoryOperand && opb instanceof MemoryOperand) {
                // memory to memory
                memoryMap.forwardChain(((MemoryOperand) opa).getAddress(), getTarget(opb));
                memoryMap.reverseChain(((MemoryOperand) opb).getAddress(), getTarget(opa));
            } else if (opa instanceof MemoryOperand && opb instanceof RegisterOperand) {
                codeMap.chain(pc, (RegisterOperand) b, getTarget(opa));
                memoryMap.reverseChain(((MemoryOperand) opa).getAddress(), getTarget(opb));
            } else if (opa instanceof RegisterOperand && opb instanceof MemoryOperand) {
                codeMap.chain(pc, (RegisterOperand) a, getTarget(opb));
                memoryMap.reverseChain(((MemoryOperand) opb).getAddress(), getTarget(opa));
            }
        }
    }

    public void clear() {
        codeMap.clear(pc);
    }

    public ChainTarget getTarget(Operand op) {
        if (op instanceof RegisterOperand) {
            RegisterOperand reg = (RegisterOperand) op;
            return codeMap.getChainTarget(pc, reg);
        } else if (op instanceof MemoryOperand) {
            MemoryOperand mem = (MemoryOperand) op;
            return new MemoryChainTarget(mem.getAddress(), memoryMap.getStep(mem.getAddress()));
        } else if (op instanceof IndexedMemoryOperand) {
            MemoryOperand mem = resolve((IndexedMemoryOperand) op);
            return new MemoryChainTarget(mem.getAddress(), memoryMap.getStep(mem.getAddress()));
        } else {
            throw new IllegalArgumentException("not a valid operand");
        }
    }

    public void chain(long last) {
        codeMap.chain(pc, last);
    }

    public String getStatistics() {
        return codeMap.getStatistics();
    }

    private RegisterTypeMap get(@SuppressWarnings("hiding") long pc) {
        return codeMap.get(pc);
    }

    public long getChain(@SuppressWarnings("hiding") long pc) {
        return codeMap.getChain(pc);
    }

    public Set<RegisterTypeMap> getExtraChain(@SuppressWarnings("hiding") long pc) {
        return codeMap.getExtraChain(pc);
    }

    public Set<RegisterTypeMap> getForwardChain(@SuppressWarnings("hiding") long pc) {
        return codeMap.getForwardChain(pc);
    }

    public long get(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return codeMap.get(pc, op);
    }

    public long getMemory(long addr, long step) {
        return memoryMap.get(addr, step);
    }

    public Set<ChainTarget> getMemoryReverseChain(long addr, long step) {
        return memoryMap.getReverseChain(addr, step);
    }

    public Set<ChainTarget> getMemoryForwardChain(long addr, long step) {
        return memoryMap.getForwardChain(addr, step);
    }

    public void finish() {
        for (ComputedSymbol sym : symbols.getSubroutines()) {
            // cut all links at sym.address if the registers are saved according to
            // sym.getUnusedRegisters()
            codeMap.breakChain(sym.address, sym.getUnusedRegisters());

            // TODO: cut all the forward links at the return instructions too
            // the hard part here is finding all the return instructions of the subroutine
        }
    }

    // new function to resolve registers
    public long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return resolve(pc, op, null);
    }

    public long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op, Collection<ChainTarget> result) {
        return resolveFromSeed(new RegisterChainTarget(get(pc), op.getRegister()), result);
    }

    public long resolveFromSeed(ChainTarget start, Collection<ChainTarget> result) {
        long bits = 0;

        Set<ChainTarget> visited = new HashSet<>();
        Deque<ChainTarget> todo = new LinkedList<>();

        // all back links
        todo.add(start);

        while (!todo.isEmpty()) {
            // fetch next target
            ChainTarget target = todo.remove();

            if (visited.contains(target)) {
                continue;
            }

            visited.add(target);

            if (result != null) {
                result.add(target);
            }

            if (target instanceof RegisterChainTarget) {
                RegisterChainTarget tgt = (RegisterChainTarget) target;

                RegisterOperand reg = new RegisterOperand(tgt.register);
                RegisterTypeMap map = tgt.map;

                // process target
                long value = map.get(reg);
                bits |= value;

                // decide if reverse chain is used
                if (BitTest.test(value, VariableType.CHAIN_BIT)) {
                    for (RegisterTypeMap t : map.getExtraChain()) {
                        todo.add(new RegisterChainTarget(t, reg.getRegister()));
                    }

                    long last = map.getChain();
                    if (last != -1 && BitTest.test(value, VariableType.CHAIN_BIT) && !BitTest.test(value, VariableType.BREAK_BIT)) {
                        todo.add(new RegisterChainTarget(get(last), reg.getRegister()));
                    }
                }

                // decide if forward chain is used
                for (RegisterTypeMap r : map.getForwardChain()) {
                    if (BitTest.test(r.get(reg), VariableType.CHAIN_BIT) && !BitTest.test(r.get(reg), VariableType.BREAK_BIT)) {
                        todo.add(new RegisterChainTarget(r, reg.getRegister()));
                    }
                }

                // add all precise chain elements
                todo.addAll(map.getReverseChain(reg)); // op or reg?
                todo.addAll(map.getForwardChain(reg));
            } else if (target instanceof MemoryChainTarget) {
                MemoryChainTarget tgt = (MemoryChainTarget) target;

                // process target
                long value = getMemory(tgt.address, tgt.step);
                bits |= value;

                // follow reverse chain
                todo.addAll(getMemoryReverseChain(tgt.address, tgt.step));

                // follow forward chain
                todo.addAll(getMemoryForwardChain(tgt.address, tgt.step));
            }
        }

        return bits;
    }

    public long resolveData(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        long bits = 0;

        Set<ChainTarget> visited = new HashSet<>();
        Deque<ChainTarget> todo = new LinkedList<>();

        // all back links
        todo.add(new RegisterChainTarget(get(pc), op.getRegister()));

        while (!todo.isEmpty()) {
            // fetch next target
            ChainTarget target = todo.remove();

            if (visited.contains(target)) {
                continue;
            }

            visited.add(target);

            if (target instanceof RegisterChainTarget) {
                RegisterChainTarget tgt = (RegisterChainTarget) target;

                RegisterOperand reg = new RegisterOperand(tgt.register);
                RegisterTypeMap map = tgt.map;

                // process target
                long value = map.get(reg);
                bits |= value;

                // decide if reverse chain is used
                if (BitTest.test(value, VariableType.CHAIN_BIT)) {
                    for (RegisterTypeMap t : map.getExtraChain()) {
                        todo.add(new RegisterChainTarget(t, reg.getRegister()));
                    }

                    long last = map.getChain();
                    if (last != -1 && BitTest.test(value, VariableType.CHAIN_BIT) && !BitTest.test(value, VariableType.BREAK_BIT)) {
                        todo.add(new RegisterChainTarget(get(last), reg.getRegister()));
                    }
                }
            } else if (target instanceof MemoryChainTarget) {
                MemoryChainTarget tgt = (MemoryChainTarget) target;

                // process target
                long value = getMemory(tgt.address, tgt.step);
                bits |= value;

                // follow reverse chain
                todo.addAll(getMemoryReverseChain(tgt.address, tgt.step));
            }
        }

        return bits;
    }

    public long resolveMemory(long addr, long step) {
        return resolveMemory(addr, step, null);
    }

    public long resolveMemory(long addr, long step, Collection<ChainTarget> result) {
        return resolveFromSeed(new MemoryChainTarget(addr, step), result);
    }

    public Set<StepEvent> getSteps(long addr) {
        return memory.getSteps(addr);
    }

    public long[] getDataReads(long addr) {
        return getSteps(addr).stream().flatMap(x -> x.getDataReads().stream()).mapToLong(MemoryEvent::getAddress).distinct().toArray();
    }

    public long[] getDataWrites(long addr) {
        return getSteps(addr).stream().flatMap(x -> x.getDataWrites().stream()).mapToLong(MemoryEvent::getAddress).distinct().toArray();
    }

    public Set<Long> getUsedAddresses() {
        return memoryMap.getUsedAddresses();
    }
}
