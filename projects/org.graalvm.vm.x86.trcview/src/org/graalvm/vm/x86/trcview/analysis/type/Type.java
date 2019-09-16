package org.graalvm.vm.x86.trcview.analysis.type;

public class Type {
    private final boolean isConst;
    private final DataType type;
    private final Type pointee;
    private final Struct struct;

    public Type(DataType type) {
        this(type, false);
    }

    public Type(DataType type, boolean isConst) {
        this.type = type;
        this.pointee = null;
        this.struct = null;
        this.isConst = isConst;
    }

    public Type(Struct struct) {
        this(struct, false);
    }

    public Type(Struct struct, boolean isConst) {
        this.type = DataType.STRUCT;
        this.pointee = null;
        this.struct = struct;
        this.isConst = isConst;
    }

    public Type(Type type) {
        this(type, false);
    }

    public Type(Type type, boolean isConst) {
        this.type = DataType.PTR;
        this.pointee = type;
        this.struct = null;
        this.isConst = isConst;
    }

    public DataType getType() {
        return type;
    }

    public Struct getStruct() {
        assert type == DataType.STRUCT;
        return struct;
    }

    public Type getPointee() {
        assert type == DataType.PTR;
        return pointee;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getSize() {
        switch (type) {
            case VOID:
                return 0;
            case STRING:
            case PTR:
                return 8;
            case U8:
            case S8:
                return 1;
            case U16:
            case S16:
                return 2;
            case U32:
            case S32:
                return 4;
            case U64:
            case S64:
                return 8;
            case STRUCT:
                return struct.getSize();
        }
        throw new AssertionError("this should be unreachable");
    }

    @Override
    public String toString() {
        String conststr = isConst ? "const " : "";
        switch (type) {
            case VOID:
                return "void";
            case STRING:
                return "char*";
            case U8:
                return conststr + "u8";
            case S8:
                return conststr + "s8";
            case U16:
                return conststr + "u16";
            case S16:
                return conststr + "s16";
            case U32:
                return conststr + "u32";
            case S32:
                return conststr + "s32";
            case U64:
                return conststr + "u64";
            case S64:
                return conststr + "s64";
            case PTR:
                if (isConst) {
                    return pointee.toString() + "* const";
                } else {
                    return pointee.toString() + "*";
                }
            default:
                return "/* unknown */";
        }
    }
}
