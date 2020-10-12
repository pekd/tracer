package org.graalvm.vm.trcview.analysis.memory;

public class Protection {
    public final boolean r;
    public final boolean w;
    public final boolean x;

    public Protection(boolean r, boolean w, boolean x) {
        this.r = r;
        this.w = w;
        this.x = x;
    }

    @Override
    public int hashCode() {
        return (r ? 4 : 0) + (w ? 2 : 0) + (x ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null) {
            return false;
        } else if (!(o instanceof Protection)) {
            return false;
        }
        Protection p = (Protection) o;
        return p.r == r && p.w == w && p.x == x;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(3);

        if (r) {
            buf.append('R');
        } else {
            buf.append('-');
        }

        if (w) {
            buf.append('W');
        } else {
            buf.append('-');
        }

        if (x) {
            buf.append('X');
        } else {
            buf.append('-');
        }

        return buf.toString();
    }
}
