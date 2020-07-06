package org.graalvm.vm.trcview.analysis.device;

public class RegisterValue {
    public final int id;
    public final long value;

    public RegisterValue(int id, long value) {
        this.id = id;
        this.value = value;
    }
}
