package org.graalvm.vm.trcview.arch.custom.io;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.custom.GenericArchitecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordOutputStream;

public class GenericCpuState extends CpuState {
    private final StateDescription desc;
    private final byte[] data;

    public GenericCpuState(int tid, StateDescription desc, byte[] data) {
        super(GenericArchitecture.ID, tid);
        this.desc = desc;
        this.data = data;
    }

    @Override
    public long getStep() {
        return desc.getStep(data);
    }

    @Override
    public long getPC() {
        return desc.getPC(data);
    }

    @Override
    public long get(String name) {
        switch (name) {
            case "step":
                return getStep();
            case "pc":
                return getPC();
            default:
                Member member = desc.struct.getMember(name);
                if (member == null) {
                    throw new IllegalArgumentException("unknown field " + name);
                }
                if (member.type instanceof PrimitiveType) {
                    PrimitiveType type = (PrimitiveType) member.type;
                    if (desc.bigEndian) {
                        if (type.isUnsigned()) {
                            switch (type.getBasicType()) {
                                case CHAR:
                                    return Byte.toUnsignedLong(data[member.offset]);
                                case SHORT:
                                    return Short.toUnsignedLong(Endianess.get16bitBE(data, member.offset));
                                case INT:
                                    return Integer.toUnsignedLong(Endianess.get32bitBE(data, member.offset));
                                case LONG:
                                    return Endianess.get64bitBE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + type.getBasicType());
                            }
                        } else {
                            switch (type.getBasicType()) {
                                case CHAR:
                                    return data[member.offset];
                                case SHORT:
                                    return Endianess.get16bitBE(data, member.offset);
                                case INT:
                                    return Endianess.get32bitBE(data, member.offset);
                                case LONG:
                                    return Endianess.get64bitBE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + type.getBasicType());
                            }
                        }
                    } else {
                        if (type.isUnsigned()) {
                            switch (type.getBasicType()) {
                                case CHAR:
                                    return Byte.toUnsignedLong(data[member.offset]);
                                case SHORT:
                                    return Short.toUnsignedLong(Endianess.get16bitLE(data, member.offset));
                                case INT:
                                    return Integer.toUnsignedLong(Endianess.get32bitLE(data, member.offset));
                                case LONG:
                                    return Endianess.get64bitLE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + type.getBasicType());
                            }
                        } else {
                            switch (type.getBasicType()) {
                                case CHAR:
                                    return data[member.offset];
                                case SHORT:
                                    return Endianess.get16bitLE(data, member.offset);
                                case INT:
                                    return Endianess.get32bitLE(data, member.offset);
                                case LONG:
                                    return Endianess.get64bitLE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + type.getBasicType());
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("invalid member type " + member.type);
                }
        }
    }

    @Override
    protected void writeRecord(WordOutputStream out) throws IOException {
        // nothing for now
    }

    @Override
    public String toString() {
        return desc.formatter.format(this);
    }
}
