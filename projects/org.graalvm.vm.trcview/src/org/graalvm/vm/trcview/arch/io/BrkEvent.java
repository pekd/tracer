package org.graalvm.vm.trcview.arch.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class BrkEvent extends Event {
    private final long brk;
    private final long result;

    public BrkEvent(int tid, long brk, long result) {
        super(Elf.EM_NONE, BRK, tid);
        this.brk = brk;
        this.result = result;
    }

    public long getBrk() {
        return brk;
    }

    public long getResult() {
        return result;
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(brk);
        out.write64bit(result);
    }

    public static BrkEvent readRecord(WordInputStream in, int tid) throws IOException {
        long brk = in.read64bit();
        long result = in.read64bit();
        return new BrkEvent(tid, brk, result);
    }

    @Override
    public String toString() {
        return "brk(0x" + HexFormatter.tohex(brk, 16) + ") = 0x" + HexFormatter.tohex(result, 16);
    }
}
