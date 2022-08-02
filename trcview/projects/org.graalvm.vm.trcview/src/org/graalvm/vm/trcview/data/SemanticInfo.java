package org.graalvm.vm.trcview.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.data.ir.Operand;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public class SemanticInfo extends Semantics {
    public static final int OP_ADDSUB = 0;
    public static final int OP_MULDIV = 1;
    public static final int OP_MOVE = 2;
    public static final int OP_UNIFY = 3;
    public static final int OP_RESET = 4;
    public static final int OP_CONSTRAINT = 5;
    public static final int OP_SET = 6;

    private List<Operation> operations = new ArrayList<>();

    private static class Operation {
        private final int op;
        private final Operand dst;
        private final Operand src;
        private final VariableType type;

        public Operation(int op, Operand dst, Operand src) {
            this.op = op;
            this.dst = dst;
            this.src = src;
            this.type = null;
        }

        public Operation(int op, Operand operand) {
            this.op = op;
            this.dst = operand;
            this.src = null;
            this.type = null;
        }

        public Operation(int op, Operand operand, VariableType type) {
            this.op = op;
            this.dst = operand;
            this.src = null;
            this.type = type;
        }

        private String getOp() {
            switch (op) {
                case OP_ADDSUB:
                    return "ADD/SUB";
                case OP_MULDIV:
                    return "MUL/DIV";
                case OP_MOVE:
                    return "MOVE";
                case OP_UNIFY:
                    return "UNIFY";
                case OP_RESET:
                    return "RESET";
                case OP_CONSTRAINT:
                    return "CONSTRAINT";
                case OP_SET:
                    return "SET";
                default:
                    return "???";
            }
        }

        @Override
        public String toString() {
            String o = getOp();
            if (src == null) {
                if (type == null) {
                    return o + " " + dst;
                } else {
                    return o + " " + dst + ", " + type;
                }
            } else {
                return o + " " + dst + ", " + src;
            }
        }
    }

    private void add(int type, Operand op) {
        operations.add(new Operation(type, op));
    }

    private void add(int type, Operand dst, Operand src) {
        operations.add(new Operation(type, dst, src));
    }

    private void add(int type, Operand op, VariableType t) {
        operations.add(new Operation(type, op, t));
    }

    @Override
    public void read(int reg) {
        // nothing
    }

    @Override
    public void write(int reg) {
        // nothing
    }

    @Override
    public boolean isLive(long addr, int reg) {
        return false; // nothing
    }

    @Override
    public void arithmetic(Operand op, boolean mul) {
        add(mul ? OP_MULDIV : OP_ADDSUB, op);
    }

    @Override
    public void move(Operand dst, Operand src) {
        add(OP_MOVE, dst, src);
    }

    @Override
    public void constraint(Operand op, VariableType type) {
        add(OP_CONSTRAINT, op, type);
    }

    @Override
    public void set(Operand op, VariableType type) {
        add(OP_SET, op, type);
    }

    @Override
    public void reset(Operand op) {
        add(OP_RESET, op);
    }

    @Override
    public void unify(Operand a, Operand b) {
        add(OP_UNIFY, a, b);
    }

    @Override
    public void clear() {
        // nothing
    }

    @Override
    public ChainTarget getTarget(Operand op) {
        return null;
    }

    @Override
    public void chain(long last) {
        // nothing
    }

    @Override
    public String getStatistics() {
        return null;
    }

    @Override
    public long getChain(@SuppressWarnings("hiding") long pc) {
        return 0;
    }

    @Override
    public Set<RegisterTypeMap> getExtraChain(@SuppressWarnings("hiding") long pc) {
        return null;
    }

    @Override
    public Set<RegisterTypeMap> getForwardChain(@SuppressWarnings("hiding") long pc) {
        return null;
    }

    @Override
    public long get(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return 0;
    }

    @Override
    public long getMemory(long addr, long step) {
        return 0;
    }

    @Override
    public Set<ChainTarget> getMemoryReverseChain(long addr, long step) {
        return null;
    }

    @Override
    public Set<ChainTarget> getMemoryForwardChain(long addr, long step) {
        return null;
    }

    @Override
    public void finish() {
        // nothing
    }

    @Override
    public long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return 0;
    }

    @Override
    public long resolve(@SuppressWarnings("hiding") long pc, RegisterOperand op, Collection<ChainTarget> result) {
        return 0;
    }

    @Override
    public long resolveData(@SuppressWarnings("hiding") long pc, RegisterOperand op) {
        return 0;
    }

    @Override
    public long resolveMemory(long addr, long step) {
        return 0;
    }

    @Override
    public long resolveMemory(long addr, long step, Collection<ChainTarget> result) {
        return 0;
    }

    @Override
    public Set<StepEvent> getSteps(long addr) {
        return null;
    }

    @Override
    public long[] getDataReads(long addr) {
        return null;
    }

    @Override
    public long[] getDataWrites(long addr) {
        return null;
    }

    @Override
    public Set<Long> getUsedAddresses() {
        return null;
    }

    public String[] getOperations() {
        String[] result = new String[operations.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = operations.get(i).toString();
        }
        return result;
    }
}
