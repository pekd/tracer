package org.graalvm.vm.trcview.arch;

public class BranchTarget {
    private final long bta;
    private final boolean valid;

    public BranchTarget() {
        bta = 0;
        valid = false;
    }

    public BranchTarget(long bta) {
        this.bta = bta;
        this.valid = true;
    }

    public long getBTA() {
        return bta;
    }

    public boolean isValid() {
        return valid;
    }
}
