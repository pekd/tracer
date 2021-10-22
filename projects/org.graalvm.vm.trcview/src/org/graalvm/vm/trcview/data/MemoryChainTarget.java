package org.graalvm.vm.trcview.data;

public class MemoryChainTarget extends ChainTarget {
    public final long address;

    public MemoryChainTarget(long address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return (int) address;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof MemoryChainTarget)) {
            return false;
        } else {
            MemoryChainTarget t = (MemoryChainTarget) o;
            return t.address == address;
        }
    }
}
