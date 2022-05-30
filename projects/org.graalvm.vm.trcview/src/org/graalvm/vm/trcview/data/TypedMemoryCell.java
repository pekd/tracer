package org.graalvm.vm.trcview.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.graalvm.vm.trcview.data.type.VariableType;

public class TypedMemoryCell {
    private long step;

    private long bitfield;
    private Set<ChainTarget> reverseChain;
    private Set<ChainTarget> forwardChain;

    public TypedMemoryCell() {
        this(0);
    }

    public TypedMemoryCell(long step) {
        this.step = step;
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public void set(VariableType type) {
        bitfield = type.getMask();
    }

    public void constrain(VariableType type) {
        bitfield |= type.getMask();
    }

    public void set(long type) {
        bitfield = type;
    }

    public void constrain(long type) {
        bitfield |= type;
    }

    public long get() {
        return bitfield;
    }

    public Set<ChainTarget> getForwardChain() {
        return forwardChain == null ? Collections.emptySet() : forwardChain;
    }

    public Set<ChainTarget> getReverseChain() {
        return reverseChain == null ? Collections.emptySet() : reverseChain;
    }

    public void clear() {
        bitfield = 0;
    }

    public void forwardChain(ChainTarget target) {
        if (forwardChain == null) {
            forwardChain = new HashSet<>();
        }
        forwardChain.add(target);
    }

    public void reverseChain(ChainTarget target) {
        if (reverseChain == null) {
            reverseChain = new HashSet<>();
        }
        reverseChain.add(target);
    }

    public void setBit(long bit) {
        bitfield |= bit;
    }
}
