package org.graalvm.vm.trcview.arch.none.io;

public abstract class Command {
    public abstract void execute(StringBuilder out, byte[] data);
}
