package org.graalvm.vm.trcview.data;

public class RegisterChainTarget extends ChainTarget {
    public final int register;
    public final RegisterTypeMap map;

    public RegisterChainTarget(RegisterTypeMap map, int register) {
        this.map = map;
        this.register = register;
    }

    @Override
    public int hashCode() {
        return register ^ map.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof RegisterChainTarget)) {
            return false;
        } else {
            RegisterChainTarget t = (RegisterChainTarget) o;
            // pointer comparison!
            return t.register == register && t.map == map;
        }
    }
}
