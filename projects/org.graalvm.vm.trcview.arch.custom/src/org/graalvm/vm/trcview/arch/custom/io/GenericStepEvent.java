package org.graalvm.vm.trcview.arch.custom.io;

import org.graalvm.vm.trcview.arch.custom.GenericArchitecture;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.util.io.Endianess;

public class GenericStepEvent extends StepEvent implements CpuState {
    private final byte[] machinecode;
    private final String[] asm;
    private final InstructionType type;

    private final StateDescription desc;
    private final byte[] data;

    protected GenericStepEvent(int tid, StateDescription desc, byte[] data, byte[] code, String[] asm, InstructionType type) {
        super(tid);
        this.desc = desc;
        this.data = data;
        this.machinecode = code;
        this.asm = asm;
        this.type = type;
    }

    @Override
    public byte[] getMachinecode() {
        return machinecode;
    }

    @Override
    public String[] getDisassemblyComponents() {
        return asm;
    }

    @Override
    public String getMnemonic() {
        return getDisassemblyComponents()[0];
    }

    @Override
    public InstructionType getType() {
        return type;
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
                    PrimitiveType memberType = (PrimitiveType) member.type;
                    if (desc.bigEndian) {
                        if (memberType.isUnsigned()) {
                            switch (memberType.getBasicType()) {
                                case CHAR:
                                    return Byte.toUnsignedLong(data[member.offset]);
                                case SHORT:
                                    return Short.toUnsignedLong(Endianess.get16bitBE(data, member.offset));
                                case INT:
                                    return Integer.toUnsignedLong(Endianess.get32bitBE(data, member.offset));
                                case LONG:
                                    return Endianess.get64bitBE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + memberType.getBasicType());
                            }
                        } else {
                            switch (memberType.getBasicType()) {
                                case CHAR:
                                    return data[member.offset];
                                case SHORT:
                                    return Endianess.get16bitBE(data, member.offset);
                                case INT:
                                    return Endianess.get32bitBE(data, member.offset);
                                case LONG:
                                    return Endianess.get64bitBE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + memberType.getBasicType());
                            }
                        }
                    } else {
                        if (memberType.isUnsigned()) {
                            switch (memberType.getBasicType()) {
                                case CHAR:
                                    return Byte.toUnsignedLong(data[member.offset]);
                                case SHORT:
                                    return Short.toUnsignedLong(Endianess.get16bitLE(data, member.offset));
                                case INT:
                                    return Integer.toUnsignedLong(Endianess.get32bitLE(data, member.offset));
                                case LONG:
                                    return Endianess.get64bitLE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + memberType.getBasicType());
                            }
                        } else {
                            switch (memberType.getBasicType()) {
                                case CHAR:
                                    return data[member.offset];
                                case SHORT:
                                    return Endianess.get16bitLE(data, member.offset);
                                case INT:
                                    return Endianess.get32bitLE(data, member.offset);
                                case LONG:
                                    return Endianess.get64bitLE(data, member.offset);
                                default:
                                    throw new IllegalStateException("invalid member type " + memberType.getBasicType());
                            }
                        }
                    }
                } else {
                    throw new IllegalStateException("invalid member type " + member.type);
                }
        }
    }

    @Override
    public String toString() {
        return desc.formatter.format(this);
    }

    @Override
    public CpuState getState() {
        return this;
    }

    @Override
    public StepFormat getFormat() {
        return GenericArchitecture.FORMAT;
    }
}
