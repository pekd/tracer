package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.script.rt.StructContainer;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.io.WordOutputStream;

public class CustomCpuState extends CpuState {
    private final StructContainer data;

    protected CustomCpuState(short arch, int tid, StructContainer data) {
        super(arch, tid);
        this.data = data;
    }

    @Override
    public long getStep() {
        return data.getI64("step");
    }

    @Override
    public long getPC() {
        return data.getI64("pc");
    }

    @Override
    public long get(String name) {
        Member member = data.getMember(name);
        switch (member.type.size()) {
            case 1:
                return data.getI8(name);
            case 2:
                return data.getI16(name);
            case 4:
                return data.getI32(name);
            case 8:
                return data.getI64(name);
            default:
                throw new IllegalArgumentException("unknown size");
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }
}
