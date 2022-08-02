package org.graalvm.vm.trcview.data;

import java.util.Collection;
import java.util.Set;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public abstract class Semantics {
    protected long pc;
    protected CpuState state;

    public void setPC(long pc) {
        this.pc = pc;
    }

    public void setState(CpuState state) {
        this.state = state;
    }

    public abstract void read(int reg);

    public abstract void write(int reg);

    public abstract boolean isLive(@SuppressWarnings("hiding") long pc, int reg);

    public abstract void arithmetic(Operand op, boolean mul);

    public abstract void move(Operand dst, Operand src);

    public abstract void constraint(Operand op, VariableType type);

    public abstract void set(Operand op, VariableType type);

    public abstract void reset(Operand op);

    public abstract void unify(Operand a, Operand b);

    public abstract void clear();

    public abstract ChainTarget getTarget(Operand op);

    public abstract void chain(long last);

    public abstract String getStatistics();

    public abstract long getChain(@SuppressWarnings("hiding") long pc);

    public abstract Set<RegisterTypeMap> getExtraChain(@SuppressWarnings("hiding") long pc);

    public abstract Set<RegisterTypeMap> getForwardChain(@SuppressWarnings("hiding") long pc);

    public abstract long get(@SuppressWarnings("hiding") long pc, RegisterOperand op);

    public abstract long getMemory(long addr, long step);

    public abstract Set<ChainTarget> getMemoryReverseChain(long addr, long step);

    public abstract Set<ChainTarget> getMemoryForwardChain(long addr, long step);

    public abstract void finish();

    public abstract long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op);

    public abstract long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op, Collection<ChainTarget> result);

    public abstract long resolveData(@SuppressWarnings("hiding") long pc, RegisterOperand op);

    public abstract long resolveMemory(long addr, long step);

    public abstract long resolveMemory(long addr, long step, Collection<ChainTarget> result);

    public abstract Set<StepEvent> getSteps(long addr);

    public abstract long[] getDataReads(long addr);

    public abstract long[] getDataWrites(long addr);

    public abstract Set<Long> getUsedAddresses();
}
