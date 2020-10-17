package org.graalvm.vm.trcview.arch.io;

public class EofEvent extends Event {
    public EofEvent() {
        super(0);
    }

    @Override
    public String toString() {
        return "EOF";
    }
}
