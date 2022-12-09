package org.graalvm.vm.trcview.data;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.SymbolTable;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.data.ir.ConstOperand;
import org.graalvm.vm.trcview.data.ir.IndexedMemoryOperand;
import org.graalvm.vm.trcview.data.ir.IndirectIndexedMemoryOperand;
import org.graalvm.vm.trcview.data.ir.IndirectMemoryOperand;
import org.graalvm.vm.trcview.data.ir.MemoryOperand;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.log.Trace;

public class CodeSemantics extends Semantics {
    private static final Logger log = Trace.create(CodeSemantics.class);

    private final CodeTypeMap codeMap;
    private final MemoryTypeMap memoryMap;
    private final SymbolTable symbols;

    private final MemoryAccessMap memory;
    private final MemoryTrace memtrc;
    private final int addrsize;

    private Set<Long>[] pcs;

    private long maxlen = 0;

    @SuppressWarnings("unchecked")
    public CodeSemantics(CodeTypeMap codeMap, MemoryTypeMap memoryMap, SymbolTable symbols, MemoryAccessMap memory, MemoryTrace memtrc, Architecture arch) {
        this.codeMap = codeMap;
        this.memoryMap = memoryMap;
        this.symbols = symbols;
        this.memory = memory;
        this.memtrc = memtrc;
        this.addrsize = arch.getTypeInfo().getPointerSize();

        // register live range tracking
        pcs = new HashSet[arch.getRegisterCount()];
        for (int i = 0; i < pcs.length; i++) {
            pcs[i] = new HashSet<>();
        }
    }

    private long addr(long addr) {
        switch (addrsize) {
            case 1:
                return Byte.toUnsignedLong((byte) addr);
            case 2:
                return Short.toUnsignedLong((short) addr);
            case 4:
                return Integer.toUnsignedLong((int) addr);
            case 8:
                return addr;
            default:
                throw new AssertionError("unknown address size " + addrsize);
        }
    }

    @Override
    public void setPC(long pc) {
        super.setPC(pc);

        for (Set<Long> trail : pcs) {
            trail.add(pc);
        }
    }

    @Override
    public void read(int reg) {
        // everything in pcs is "live"
        if (maxlen < pcs[reg].size()) {
            maxlen = pcs[reg].size();
        }
        if (!pcs[reg].isEmpty()) {
            for (long addr : pcs[reg]) {
                codeMap.setLive(addr, reg, true);
            }
        }
        pcs[reg].clear();
    }

    @Override
    public void write(int reg) {
        // everything in pcs is "dead"
        if (maxlen < pcs[reg].size()) {
            maxlen = pcs[reg].size();
        }
        if (!pcs[reg].isEmpty()) {
            for (long addr : pcs[reg]) {
                codeMap.setLive(addr, reg, false);
            }
        }
        pcs[reg].clear();
    }

    @Override
    public boolean isLive(long addr, int reg) {
        return codeMap.isLive(addr, reg);
    }

    private Operand resolve(Operand op) {
        if (op instanceof IndexedMemoryOperand) {
            return resolve((IndexedMemoryOperand) op);
        } else if (op instanceof IndirectMemoryOperand) {
            return resolve((IndirectMemoryOperand) op);
        } else if (op instanceof IndirectIndexedMemoryOperand) {
            return resolve((IndirectIndexedMemoryOperand) op);
        } else {
            return op;
        }
    }

    private MemoryOperand resolve(IndexedMemoryOperand op) {
        long reg = state.getRegisterById(op.getRegister());
        if (op.getOffsetRegister() != -1) {
            reg += state.getRegisterById(op.getOffsetRegister());
        }
        long offset = op.getOffset();
        long address = addr(reg + offset);
        return new MemoryOperand(address);
    }

    private MemoryOperand resolve(IndirectMemoryOperand op) {
        long address;
        try {
            switch (addrsize) {
                case 1:
                    address = Byte.toUnsignedInt(memtrc.getLastByte(addr(op.getAddress())));
                    break;
                case 2:
                    address = Short.toUnsignedInt(memtrc.getLastShort(addr(op.getAddress())));
                    break;
                case 4:
                    address = Integer.toUnsignedLong(memtrc.getLastInt(addr(op.getAddress())));
                    break;
                case 8:
                    address = memtrc.getLastWord(addr(op.getAddress()));
                    break;
                default:
                    throw new AssertionError("unknown address size " + addrsize);
            }
        } catch (MemoryNotMappedException e) {
            // do something here
            return null;
        }
        return new MemoryOperand(address);
    }

    private MemoryOperand resolve(IndirectIndexedMemoryOperand op) {
        long reg = state.getRegisterById(op.getRegister());
        long offset = op.getOffset();
        long addr = addr(reg + offset);
        long address;
        try {
            switch (addrsize) {
                case 1:
                    address = Byte.toUnsignedInt(memtrc.getLastByte(addr));
                    break;
                case 2:
                    address = Short.toUnsignedInt(memtrc.getLastShort(addr));
                    break;
                case 4:
                    address = Integer.toUnsignedLong(memtrc.getLastInt(addr));
                    break;
                case 8:
                    address = memtrc.getLastWord(addr);
                    break;
                default:
                    throw new AssertionError("unknown address size " + addrsize);
            }
        } catch (MemoryNotMappedException e) {
            // do something here
            return null;
        }
        return new MemoryOperand(address);
    }

    @SuppressWarnings("unused")
    private static RegisterOperand getRegister(Operand op) {
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
        } else if (op instanceof IndirectMemoryOperand) {
            return memoryMap.get(resolve((IndirectMemoryOperand) op));
        } else if (op instanceof ConstOperand) {
            ConstOperand cop = (ConstOperand) op;
            return cop.get();
        } else {
            return 0;
        }
    }

    @Override
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
        } else if (op instanceof IndirectMemoryOperand) {
            MemoryOperand mdst = resolve((IndirectMemoryOperand) op);
            memoryMap.step(mdst, state.getStep());
            memoryMap.setBit(mdst, bits);
        } else if (op instanceof IndirectIndexedMemoryOperand) {
            MemoryOperand mdst = resolve((IndirectIndexedMemoryOperand) op);
            memoryMap.step(mdst, state.getStep());
            memoryMap.setBit(mdst, bits);
        }
    }

    @Override
    public void move(Operand dst, Operand src) {
        if (dst.equals(src)) {
            // useless move
            return;
        }

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
        } else if (dst instanceof IndirectMemoryOperand) {
            MemoryOperand mdst = resolve((IndirectMemoryOperand) dst);
            memoryMap.step(mdst, state.getStep());
            memoryMap.set(mdst, get(src));
        } else if (dst instanceof IndirectIndexedMemoryOperand) {
            MemoryOperand mdst = resolve((IndirectIndexedMemoryOperand) dst);
            memoryMap.step(mdst, state.getStep());
            memoryMap.set(mdst, get(src));
        }
        unify(src, dst);
    }

    @Override
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
        } else if (op instanceof IndirectMemoryOperand) {
            MemoryOperand mop = resolve((IndirectMemoryOperand) op);
            memoryMap.constrain(mop, type);
        } else if (op instanceof IndirectIndexedMemoryOperand) {
            MemoryOperand mop = resolve((IndirectIndexedMemoryOperand) op);
            memoryMap.constrain(mop, type);
        } else if (op instanceof ConstOperand) {
            ConstOperand cop = (ConstOperand) op;
            cop.constrain(type);
        }
    }

    @Override
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
        } else if (op instanceof IndirectMemoryOperand) {
            MemoryOperand mop = resolve((IndirectMemoryOperand) op);
            memoryMap.step(mop, state.getStep());
            memoryMap.set(mop, type);
        } else if (op instanceof IndirectIndexedMemoryOperand) {
            MemoryOperand mop = resolve((IndirectIndexedMemoryOperand) op);
            memoryMap.step(mop, state.getStep());
            memoryMap.set(mop, type);
        } else if (op instanceof ConstOperand) {
            ConstOperand cop = (ConstOperand) op;
            cop.set(type);
        }
    }

    @Override
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
        } else if (op instanceof IndirectMemoryOperand) {
            MemoryOperand mop = resolve((IndirectMemoryOperand) op);
            memoryMap.set(mop, 0);
        } else if (op instanceof IndirectIndexedMemoryOperand) {
            MemoryOperand mop = resolve((IndirectIndexedMemoryOperand) op);
            memoryMap.set(mop, 0);
        }
    }

    @Override
    public void unify(Operand a, Operand b) {
        if (a.equals(b)) {
            return;
        }

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

    @Override
    public void clear() {
        codeMap.clear(pc);
    }

    @Override
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
        } else if (op instanceof IndirectMemoryOperand) {
            MemoryOperand mem = resolve((IndirectMemoryOperand) op);
            return new MemoryChainTarget(mem.getAddress(), memoryMap.getStep(mem.getAddress()));
        } else {
            throw new IllegalArgumentException("not a valid operand");
        }
    }

    @Override
    public void chain(long last) {
        codeMap.chain(pc, last);
    }

    @Override
    public String getStatistics() {
        return codeMap.getStatistics();
    }

    public RegisterTypeMap get(@SuppressWarnings("hiding") long pc) {
        return codeMap.get(pc);
    }

    @Override
    public long getChain(@SuppressWarnings("hiding") long pc) {
        return codeMap.getChain(pc);
    }

    @Override
    public Set<RegisterTypeMap> getExtraChain(@SuppressWarnings("hiding") long pc) {
        return codeMap.getExtraChain(pc);
    }

    @Override
    public Set<RegisterTypeMap> getForwardChain(@SuppressWarnings("hiding") long pc) {
        return codeMap.getForwardChain(pc);
    }

    @Override
    public long get(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return codeMap.get(pc, op);
    }

    @Override
    public long getMemory(long addr, long step) {
        return memoryMap.get(addr, step);
    }

    public void setMemory(long addr, long step, long value) {
        memoryMap.set(addr, step, value);
    }

    @Override
    public Set<ChainTarget> getMemoryReverseChain(long addr, long step) {
        return memoryMap.getReverseChain(addr, step);
    }

    @Override
    public Set<ChainTarget> getMemoryForwardChain(long addr, long step) {
        return memoryMap.getForwardChain(addr, step);
    }

    @Override
    public void finish() {
        for (ComputedSymbol sym : symbols.getSubroutines()) {
            // cut all links at sym.address if the registers are saved according to
            // sym.getUnusedRegisters()
            codeMap.breakChain(sym.address, sym.getUnusedRegisters());

            // TODO: cut all the forward links at the return instructions too
            // the hard part here is finding all the return instructions of the subroutine
        }

        log.info("Maximum PC set size: " + maxlen);
    }

    // new function to resolve registers
    @Override
    public long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return resolve(pc, op, null);
    }

    @Override
    public long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op, Collection<ChainTarget> result) {
        return resolveFromSeed(new RegisterChainTarget(get(pc), op.getRegister()), result);
    }

    private long resolveFromSeed(ChainTarget start, Collection<ChainTarget> result) {
        long bits = 0;

        Set<ChainTarget> visited = new HashSet<>();
        Set<ChainTarget> todo = new HashSet<>();

        // all back links
        todo.add(start);

        while (!todo.isEmpty()) {
            // fetch next target
            ChainTarget target = todo.iterator().next();
            todo.remove(target);

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
                boolean liveReverse = map.isLive(reg.getRegister());
                if (BitTest.test(value, VariableType.CHAIN_BIT) && liveReverse) {
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
                    boolean liveForward = r.isLive(reg.getRegister());
                    long rbits = r.get(reg);
                    if (BitTest.test(rbits, VariableType.CHAIN_BIT) && !BitTest.test(rbits, VariableType.BREAK_BIT) && liveForward) {
                        todo.add(new RegisterChainTarget(r, reg.getRegister()));
                    }
                }

                // add all precise chain elements
                if (liveReverse) {
                    todo.addAll(map.getReverseChain(reg)); // op or reg?
                }

                for (ChainTarget t : map.getForwardChain(reg)) {
                    if (t instanceof RegisterChainTarget) {
                        RegisterChainTarget rt = (RegisterChainTarget) t;
                        if (rt.map.isLive(rt.register)) {
                            todo.add(t);
                        }
                    } else {
                        todo.add(t);
                    }
                }
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

            if (VariableType.SOLVED.test(bits)) {
                return bits;
            }
        }

        // cache value
        bits |= VariableType.SOLVED.getMask();
        for (ChainTarget t : visited) {
            if (t instanceof RegisterChainTarget) {
                RegisterChainTarget rt = (RegisterChainTarget) t;
                rt.map.setResolvedType(rt.register, bits);
            } else if (t instanceof MemoryChainTarget) {
                MemoryChainTarget mt = (MemoryChainTarget) t;
                setMemory(mt.address, mt.step, bits);
            }
        }
        return bits;
    }

    @Override
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

    @Override
    public long resolveMemory(long addr, long step) {
        return resolveMemory(addr, step, null);
    }

    @Override
    public long resolveMemory(long addr, long step, Collection<ChainTarget> result) {
        return resolveFromSeed(new MemoryChainTarget(addr, step), result);
    }

    @Override
    public Set<StepEvent> getSteps(long addr) {
        return memory.getSteps(addr);
    }

    @Override
    public long[] getDataReads(long addr) {
        return getSteps(addr).stream().flatMap(x -> x.getDataReads().stream()).mapToLong(MemoryEvent::getAddress).distinct().toArray();
    }

    @Override
    public long[] getDataWrites(long addr) {
        return getSteps(addr).stream().flatMap(x -> x.getDataWrites().stream()).mapToLong(MemoryEvent::getAddress).distinct().toArray();
    }

    @Override
    public Set<Long> getUsedAddresses() {
        return memoryMap.getUsedAddresses();
    }
}
