package org.graalvm.vm.trcview.script.type;

import java.util.Objects;

public class PrimitiveType extends Type {
    public static final PrimitiveType VOID = new PrimitiveType(BasicType.VOID);
    public static final PrimitiveType CHAR = new PrimitiveType(BasicType.CHAR);
    public static final PrimitiveType SHORT = new PrimitiveType(BasicType.SHORT);
    public static final PrimitiveType INT = new PrimitiveType(BasicType.INT);
    public static final PrimitiveType LONG = new PrimitiveType(BasicType.LONG);
    public static final PrimitiveType UCHAR = new PrimitiveType(BasicType.CHAR, true);
    public static final PrimitiveType USHORT = new PrimitiveType(BasicType.SHORT, true);
    public static final PrimitiveType UINT = new PrimitiveType(BasicType.INT, true);
    public static final PrimitiveType ULONG = new PrimitiveType(BasicType.LONG, true);

    private BasicType type;
    private boolean unsigned;

    public PrimitiveType(BasicType type) {
        this.type = type;
        this.unsigned = false;
    }

    public PrimitiveType(BasicType type, boolean unsigned) {
        this.type = type;
        this.unsigned = unsigned;
    }

    public BasicType getBasicType() {
        return type;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    @Override
    public String toString() {
        if (unsigned) {
            return "unsigned " + type.getName();
        } else {
            return type.getName();
        }
    }

    @Override
    public int size() {
        switch (type) {
            case CHAR:
                return 1;
            case SHORT:
                return 2;
            case INT:
                return 4;
            case LONG:
                return 8;
            case LONGLONG:
                return 16;
            case VOID:
            default:
                return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ArrayType)) {
            return false;
        }
        PrimitiveType t = (PrimitiveType) o;
        return t.unsigned == unsigned && t.type == type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, unsigned);
    }
}
