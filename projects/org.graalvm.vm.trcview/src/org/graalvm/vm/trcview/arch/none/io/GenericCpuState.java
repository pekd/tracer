package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.none.None;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericCpuState extends CpuState {
    private final long step;
    private final long pc;
    private final String data;

    protected GenericCpuState(WordInputStream in, int tid) throws IOException {
        super(None.ID, tid);
        step = in.read64bit();
        pc = in.read64bit();
        data = IO.readString(in);
    }

    @Override
    public long getStep() {
        return step;
    }

    @Override
    public long getPC() {
        return pc;
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "pc":
                return getPC();
            case "step":
                return getStep();
            case "tid":
                return getTid();
            default:
                throw new IllegalArgumentException("unknown variable " + name);
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        out.write64bit(step);
        out.write64bit(pc);
        IO.writeString(out, data);
    }

    @Override
    public String toString() {
        return data;
    }
}
