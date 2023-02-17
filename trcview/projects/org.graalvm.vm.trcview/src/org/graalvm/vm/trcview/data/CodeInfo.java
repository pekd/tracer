package org.graalvm.vm.trcview.data;

public class CodeInfo {
    private final long pc;
    private final int size;

    public CodeInfo(long pc, int size) {
        this.pc = pc;
        this.size = size;
    }

    public long getPC() {
        return pc;
    }

    public int getSize() {
        return size;
    }

    @Override
    public int hashCode() {
        return (int) pc ^ (int) (pc >> 8);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof CodeInfo)) {
            return false;
        }
        CodeInfo c = (CodeInfo) o;
        return c.pc == pc;
    }
}
