package org.graalvm.vm.trcview.script.rt;

import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.io.Endianess;

public class StructContainer {
    private final Struct struct;
    private final byte[] data;

    public StructContainer(Struct struct) {
        this.struct = struct;
        this.data = new byte[struct.size()];
    }

    public Struct getType() {
        return struct;
    }

    public byte[] getData() {
        return data;
    }

    public Member getMember(String name) {
        Member member = struct.getMember(name);
        if (member == null) {
            throw new IllegalArgumentException("field " + name + " does not exist");
        }
        return member;
    }

    public byte getI8(String name) {
        Member member = struct.getMember(name);
        if (member.type.size() != 1) {
            throw new IllegalArgumentException("not an I8");
        }
        return data[member.offset];
    }

    public short getI16(String name) {
        Member member = struct.getMember(name);
        if (member.type.size() != 2) {
            throw new IllegalArgumentException("not an I16");
        }
        return Endianess.get16bitBE(data, member.offset);
    }

    public int getI32(String name) {
        Member member = struct.getMember(name);
        if (member.type.size() != 4) {
            throw new IllegalArgumentException("not an I32");
        }
        return Endianess.get32bitBE(data, member.offset);
    }

    public long getI64(String name) {
        Member member = struct.getMember(name);
        if (member.type.size() != 8) {
            throw new IllegalArgumentException("not an I64");
        }
        return Endianess.get64bitBE(data, member.offset);
    }
}
