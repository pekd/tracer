package org.graalvm.vm.x86.trcview.io.data;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.util.io.WordOutputStream;

public class EofEvent extends Event {
    public EofEvent() {
        super(Elf.EM_NONE, EOF, 0);
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // nothing
    }

    @Override
    public String toString() {
        return "EOF";
    }
}
