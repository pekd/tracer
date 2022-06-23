package org.graalvm.vm.trcview.script.type;

public abstract class Type {
    private boolean isconst;

    public void setConst(boolean isconst) {
        this.isconst = isconst;
    }

    public boolean isConst() {
        return isconst;
    }

    public String vardecl(String name) {
        return toString() + " " + name;
    }

    public abstract int size();
}
