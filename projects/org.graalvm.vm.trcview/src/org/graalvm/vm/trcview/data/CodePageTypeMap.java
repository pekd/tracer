package org.graalvm.vm.trcview.data;

import java.util.BitSet;
import java.util.Set;

import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;

public class CodePageTypeMap {
    private final RegisterTypeMap[] memory;

    public CodePageTypeMap(int bytes, int size, long pc) {
        memory = new RegisterTypeMap[bytes];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = new RegisterTypeMap(size, pc + i);
        }
    }

    public RegisterTypeMap getMap(int offset) {
        return memory[offset];
    }

    public void set(int offset, RegisterOperand op, VariableType type) {
        memory[offset].set(op, type);
    }

    public void constrain(int offset, RegisterOperand op, VariableType type) {
        memory[offset].constrain(op, type);
    }

    public void set(int offset, RegisterOperand op, long type) {
        memory[offset].set(op, type);
    }

    public void constrain(int offset, RegisterOperand op, long type) {
        memory[offset].constrain(op, type);
    }

    public long get(int offset, RegisterOperand op) {
        return memory[offset].get(op);
    }

    public long getChain(int offset) {
        return memory[offset].getChain();
    }

    public Set<RegisterTypeMap> getExtraChain(int offset) {
        return memory[offset].getExtraChain();
    }

    public Set<RegisterTypeMap> getForwardChain(int offset) {
        return memory[offset].getForwardChain();
    }

    public void clear(int offset) {
        memory[offset].clear();
    }

    public boolean useOptimizedChain(int offset, long last) {
        return memory[offset].useOptimizedChain(last);
    }

    public void chain(int offset, long last) {
        memory[offset].chain(last);
    }

    public void chain(int offset, RegisterTypeMap last) {
        memory[offset].chain(last);
    }

    public void forwardChain(int offset, RegisterTypeMap last) {
        memory[offset].forwardChain(last);
    }

    public void forwardChain(int offset, RegisterOperand reg, ChainTarget target) {
        memory[offset].forwardChain(reg, target);
    }

    public void breakChain(int offset, int register) {
        memory[offset].breakChain(register);
    }

    public void breakChain(int offset, BitSet registers) {
        memory[offset].breakChain(registers);
    }
}
