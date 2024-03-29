package org.graalvm.vm.trcview.data;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public class RegisterTypeMap {
    private final long[] registerTypes;
    private final long[] resolvedTypes;

    private long chain = -1;
    private Set<RegisterTypeMap> reverseChain = null;
    private Set<RegisterTypeMap> forwardChain = null;
    private Set<ChainTarget>[] reverseChainTargets;
    private Set<ChainTarget>[] forwardChainTargets;
    private boolean mark = false;

    private boolean[] live;

    private final long currentPC; // debugging

    @SuppressWarnings("unchecked")
    public RegisterTypeMap(int size, long pc) {
        this.currentPC = pc;
        registerTypes = new long[size];
        resolvedTypes = new long[size];
        reverseChainTargets = new Set[size];
        forwardChainTargets = new Set[size];
        live = new boolean[size];
        clear();
    }

    public boolean isMarked() {
        return mark;
    }

    public void mark() {
        mark = true;
    }

    public void clearMark() {
        mark = false;
    }

    public boolean isLive(int reg) {
        return live[reg];
    }

    public void setLive(int reg, boolean value) {
        live[reg] = value;
    }

    public void setResolvedType(int reg, long type) {
        resolvedTypes[reg] = type & ~(VariableType.CHAIN_BIT | VariableType.BREAK_BIT);
    }

    public void clear() {
        for (int i = 0; i < registerTypes.length; i++) {
            registerTypes[i] = VariableType.CHAIN_BIT;
        }
    }

    public long getPC() {
        return currentPC;
    }

    public boolean useOptimizedChain(long pc) {
        return chain == -1 || pc == chain;
    }

    public void chain(long last) {
        if (chain == last) {
            return;
        } else if (chain != -1) {
            throw new IllegalStateException("cannot override PC chain");
        }

        chain = last;
    }

    public void chain(RegisterTypeMap target) {
        if (target == this) {
            return;
        }

        if (reverseChain == null) {
            reverseChain = new HashSet<>();
        }

        reverseChain.add(target);

        target.forwardChain(this);
    }

    public void forwardChain(RegisterTypeMap target) {
        if (target == this) {
            return;
        }
        if (forwardChain == null) {
            forwardChain = new HashSet<>();
        }

        forwardChain.add(target);
    }

    public long getChain() {
        return chain;
    }

    public Set<RegisterTypeMap> getExtraChain() {
        if (reverseChain == null) {
            return Collections.emptySet();
        } else {
            return reverseChain;
        }
    }

    public Set<RegisterTypeMap> getForwardChain() {
        if (forwardChain == null) {
            return Collections.emptySet();
        } else {
            return forwardChain;
        }
    }

    public void chain(RegisterOperand op, ChainTarget target) {
        chain(op.getRegister(), target);
    }

    public void chain(int reg, ChainTarget target) {
        if (reverseChainTargets[reg] == null) {
            reverseChainTargets[reg] = new HashSet<>();
        }
        reverseChainTargets[reg].add(target);
    }

    public void forwardChain(RegisterOperand op, ChainTarget target) {
        forwardChain(op.getRegister(), target);
    }

    public void forwardChain(int reg, ChainTarget target) {
        if (forwardChainTargets[reg] == null) {
            forwardChainTargets[reg] = new HashSet<>();
        }
        forwardChainTargets[reg].add(target);
    }

    public Set<ChainTarget> getForwardChain(RegisterOperand op) {
        return getForwardChain(op.getRegister());
    }

    public Set<ChainTarget> getForwardChain(int reg) {
        return forwardChainTargets[reg] != null ? forwardChainTargets[reg] : Collections.emptySet();
    }

    public Set<ChainTarget> getReverseChain(RegisterOperand op) {
        return getReverseChain(op.getRegister());
    }

    public Set<ChainTarget> getReverseChain(int reg) {
        return reverseChainTargets[reg] != null ? reverseChainTargets[reg] : Collections.emptySet();
    }

    public void set(RegisterOperand op, VariableType type) {
        registerTypes[op.getRegister()] = type.getMask();
    }

    public void constrain(RegisterOperand op, VariableType type) {
        registerTypes[op.getRegister()] |= type.getMask();
    }

    public void set(RegisterOperand op, long type) {
        registerTypes[op.getRegister()] = type;
    }

    public void constrain(RegisterOperand op, long type) {
        registerTypes[op.getRegister()] |= type;
    }

    public long get(int reg) {
        return registerTypes[reg] | resolvedTypes[reg];
    }

    public long get(RegisterOperand op) {
        return registerTypes[op.getRegister()] | resolvedTypes[op.getRegister()];
    }

    public long getDirect(int reg) {
        return registerTypes[reg];
    }

    public long getDirect(RegisterOperand op) {
        return registerTypes[op.getRegister()];
    }

    public void breakChain(int reg) {
        registerTypes[reg] &= ~VariableType.CHAIN_BIT;
        registerTypes[reg] |= VariableType.BREAK_BIT;
        if (reverseChainTargets[reg] != null) {
            reverseChainTargets[reg].clear();
        }
    }

    public void breakChain(BitSet registers) {
        for (int i = 0; i < registerTypes.length; i++) {
            if (registers.get(i)) {
                breakChain(i);
            }
        }
    }

    // similar to constraint, but used for things unrelated to types
    public void setBit(RegisterOperand op, long bit) {
        registerTypes[op.getRegister()] |= bit;
    }
}
