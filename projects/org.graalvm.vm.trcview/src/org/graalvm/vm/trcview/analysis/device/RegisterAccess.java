package org.graalvm.vm.trcview.analysis.device;

public class RegisterAccess {
    public final long value;
    public final long step;

    public RegisterAccess(long value, long step) {
        this.value = value;
        this.step = step;
    }
}
