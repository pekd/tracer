package org.graalvm.vm.trcview.script.rt;

import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Type;
import org.graalvm.vm.util.io.Endianess;

public class Pointer {
    private final Type type;
    private final int offset;
    private final Record data;

    public Pointer(Type type, int offset, Record data) {
        this.type = type;
        this.offset = offset;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    public Record getData() {
        return data;
    }

    public int get(int off) {
        return getOffset() + off;
    }

    public Pointer add(Type pointee, int off) {
        return new Pointer(pointee, offset + off, data);
    }

    public byte getI8() {
        return data.getData()[offset];
    }

    public short getI16() {
        return Endianess.get16bitBE(data.getData(), offset);
    }

    public int getI32() {
        return Endianess.get32bitBE(data.getData(), offset);
    }

    public long getI64() {
        return Endianess.get64bitBE(data.getData(), offset);
    }

    public int getU8() {
        return Byte.toUnsignedInt(data.getData()[offset]);
    }

    public int getU16() {
        return Short.toUnsignedInt(Endianess.get16bitBE(data.getData(), offset));
    }

    public long getU32() {
        return Integer.toUnsignedLong(Endianess.get32bitBE(data.getData(), offset));
    }

    public long getU64() {
        return Endianess.get64bitBE(data.getData(), offset);
    }

    public void setI8(byte value) {
        data.getData()[offset] = value;
    }

    public void setI16(short value) {
        Endianess.set16bitBE(data.getData(), offset, value);
    }

    public void setI32(int value) {
        Endianess.set32bitBE(data.getData(), offset, value);
    }

    public void setI64(long value) {
        Endianess.set64bitBE(data.getData(), offset, value);
    }

    public long dereferenceScalar() {
        if (type instanceof PrimitiveType) {
            PrimitiveType t = (PrimitiveType) type;
            if (t.isUnsigned()) {
                switch (t.getBasicType()) {
                    case CHAR:
                        return getU8();
                    case SHORT:
                        return getU16();
                    case INT:
                        return getU32();
                    case LONG:
                        return getU64();
                    default:
                        throw new IllegalStateException("unsupported type");
                }
            } else {
                switch (t.getBasicType()) {
                    case CHAR:
                        return getI8();
                    case SHORT:
                        return getI16();
                    case INT:
                        return getI32();
                    case LONG:
                        return getI64();
                    default:
                        throw new IllegalStateException("unsupported type");
                }
            }
        } else {
            throw new IllegalStateException("not a pointer to a scalar type");
        }
    }

    public String cstr() {
        int size;
        byte[] bytes = data.getData();
        for (size = 0; bytes[offset + size] != 0; size++) {
            // nothing
        }
        return new String(bytes, offset, size);
    }
}
