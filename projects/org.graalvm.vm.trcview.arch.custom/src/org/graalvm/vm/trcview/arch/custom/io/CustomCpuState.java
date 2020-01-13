package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.io.WordOutputStream;

public class CustomCpuState extends CpuState {
    private final Pointer data;
    private final Struct struct;
    private final String pcName;

    public CustomCpuState(short arch, int tid, String pcName, Pointer data) {
        super(arch, tid);
        this.pcName = pcName;
        this.data = data;
        this.struct = (Struct) data.getType();
    }

    @Override
    public long getStep() {
        return get("step");
    }

    @Override
    public long getPC() {
        return get(pcName);
    }

    @Override
    public long get(String name) {
        Member member = struct.getMember(name);
        if (member == null) {
            throw new IllegalArgumentException("unknown field " + name);
        }
        if (member.type instanceof PrimitiveType) {
            PrimitiveType type = (PrimitiveType) member.type;
            Pointer ptr = data.add(type, member.offset);
            if (type.isUnsigned()) {
                switch (type.getBasicType()) {
                    case CHAR:
                        return Byte.toUnsignedLong(ptr.getI8());
                    case SHORT:
                        return Short.toUnsignedLong(ptr.getI16());
                    case INT:
                        return Integer.toUnsignedLong(ptr.getI32());
                    case LONG:
                        return ptr.getI64();
                    default:
                        throw new IllegalStateException("invalid member type " + type.getBasicType());
                }
            } else {
                switch (type.getBasicType()) {
                    case CHAR:
                        return ptr.getI8();
                    case SHORT:
                        return ptr.getI16();
                    case INT:
                        return ptr.getI32();
                    case LONG:
                        return ptr.getI64();
                    default:
                        throw new IllegalStateException("invalid member type " + type.getBasicType());
                }
            }
        } else {
            throw new IllegalStateException("invalid member type " + member.type);
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }
}
