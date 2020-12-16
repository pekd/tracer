package org.graalvm.vm.trcview.analysis.type;

import java.util.Objects;

import org.graalvm.vm.trcview.expression.ast.Expression;

public class Type {
    private final boolean isConst;
    private final DataType type;
    private final Type pointee;
    private final Struct struct;
    private final ArchitectureTypeInfo info;
    private long elements;
    private Representation representation;
    private Expression expr;

    public Type(DataType type) {
        this(type, false);
    }

    public Type(DataType type, Representation repr) {
        this(type, false, repr);
    }

    public Type(DataType type, boolean isConst) {
        this(type, isConst, -1, null);
    }

    public Type(DataType type, boolean isConst, Representation repr) {
        this(type, isConst, -1, repr);
    }

    public Type(DataType type, boolean isConst, int elements) {
        this(type, isConst, elements, null);
    }

    public Type(DataType type, boolean isConst, int elements, Representation repr) {
        this.type = type;
        this.pointee = null;
        this.struct = null;
        this.info = null;
        this.isConst = isConst;
        if (repr != null) {
            this.representation = repr;
        } else {
            this.representation = getDefaultRepresentation();
        }
        this.elements = elements;
    }

    public Type(Struct struct) {
        this(struct, false);
    }

    public Type(Struct struct, boolean isConst) {
        this(struct, isConst, -1);
    }

    public Type(Struct struct, boolean isConst, int elements) {
        this.type = DataType.STRUCT;
        this.pointee = null;
        this.struct = struct;
        this.info = null;
        this.isConst = isConst;
        this.representation = getDefaultRepresentation();
        this.elements = elements;
    }

    public Type(Type type, ArchitectureTypeInfo info) {
        this(type, false, info);
    }

    public Type(Type type, boolean isConst, ArchitectureTypeInfo info) {
        this(type, isConst, -1, info);
    }

    public Type(Type type, boolean isConst, long elements, ArchitectureTypeInfo info) {
        this.type = DataType.PTR;
        this.pointee = type;
        this.struct = null;
        this.info = info;
        this.isConst = isConst;
        if (type.getType() == DataType.U8 || type.getType() == DataType.S8) {
            this.representation = Representation.STRING;
        } else {
            this.representation = getDefaultRepresentation();
        }
        this.elements = elements;
    }

    public void setElements(long elements) {
        this.elements = elements;
    }

    public void clearElements() {
        this.elements = -1;
    }

    public long getElements() {
        return elements;
    }

    public Type getElementType() {
        if (getType() == DataType.PTR) {
            return new Type(pointee, isConst, info);
        } else {
            return new Type(type, isConst, representation);
        }
    }

    public void setRepresentation(Representation representation) {
        this.representation = representation;
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

    public long getSize() {
        long cnt = elements < 0 ? 1 : elements;
        return getElementSize() * cnt;
    }

    public long getElementSize() {
        switch (type) {
            case VOID:
                return 0;
            case STRING:
            case PTR:
                return info.getPointerSize();
            case U8:
            case S8:
                return 1;
            case U16:
            case S16:
            case FX16:
                return 2;
            case U32:
            case S32:
            case F32:
            case FX32:
                return 4;
            case U64:
            case S64:
            case F64:
                return 8;
            case STRUCT:
                return struct.getSize();
        }
        throw new AssertionError("this should be unreachable");
    }

    public Representation getDefaultRepresentation() {
        switch (type) {
            case PTR:
                return Representation.HEX;
            case STRING:
                return Representation.STRING;
            case F32:
            case F64:
                return Representation.FLOAT;
            case FX16:
                return Representation.FX16;
            case FX32:
                return Representation.FX32;
            default:
                return Representation.DEC;
        }
    }

    public Representation getRepresentation() {
        return representation;
    }

    public void setExpression(Expression expr) {
        this.expr = expr;
    }

    public Expression getExpression() {
        return expr;
    }

    public boolean isStringData() {
        return (type == DataType.U8 || type == DataType.S8) && representation == Representation.CHAR && elements > 1;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(String name) {
        if (elements > 1) {
            return toString(false) + " " + name + " [" + elements + "]";
        } else {
            return toString(false) + " " + name;
        }
    }

    public String toString(boolean array) {
        String conststr = isConst ? "const " : "";
        String reprstr = "";
        String exprstr = expr == null ? "" : ("<" + expr + ">");
        if (representation != getDefaultRepresentation()) {
            switch (representation) {
                case DEC:
                    reprstr = " $dec";
                    break;
                case OCT:
                    reprstr = " $oct";
                    break;
                case HEX:
                    reprstr = " $hex";
                    break;
                case CHAR:
                    reprstr = " $char";
                    break;
                case RAD50:
                    reprstr = " $rad50";
                    break;
                case FX16:
                    reprstr = " $fx16";
                    break;
                case FX32:
                    reprstr = " $fx32";
                    break;
                case FLOAT:
                    reprstr = " $float";
                    break;
            }
        } else if (type == DataType.PTR && pointee.getType() == DataType.S8 && representation == Representation.HEX) {
            // special handling for strings (char*) which were declared as $out
            reprstr = " $out";
        }
        String arraystr = "";
        if (array && elements > 1) {
            arraystr = "[" + elements + "]";
        }
        switch (type) {
            case VOID:
                return "void";
            case STRING:
                return conststr + "char*" + exprstr + arraystr;
            case U8:
                return conststr + "u8" + exprstr + reprstr + arraystr;
            case S8:
                return conststr + "s8" + exprstr + reprstr + arraystr;
            case U16:
                return conststr + "u16" + exprstr + reprstr + arraystr;
            case S16:
                return conststr + "s16" + exprstr + reprstr + arraystr;
            case U32:
                return conststr + "u32" + exprstr + reprstr + arraystr;
            case S32:
                return conststr + "s32" + exprstr + reprstr + arraystr;
            case U64:
                return conststr + "u64" + exprstr + reprstr + arraystr;
            case S64:
                return conststr + "s64" + exprstr + reprstr + arraystr;
            case F32:
                return conststr + "f32" + exprstr + reprstr + arraystr;
            case F64:
                return conststr + "f64" + exprstr + reprstr + arraystr;
            case FX16:
                return conststr + "fx16" + exprstr + reprstr + arraystr;
            case FX32:
                return conststr + "fx32" + exprstr + reprstr + arraystr;
            case PTR:
                if (isConst) {
                    return pointee.toString() + "* const" + exprstr + reprstr + arraystr;
                } else {
                    return pointee.toString() + "*" + exprstr + reprstr + arraystr;
                }
            case STRUCT:
                return conststr + struct.getName() + arraystr;
            default:
                return "/* unknown */";
        }
    }

    public String toCType() {
        switch (type) {
            case VOID:
                return "void";
            case STRING:
                return "char*";
            case U8:
                return "unsigned __int8";
            case S8:
                return "char";
            case U16:
                return "unsigned __int16";
            case S16:
                return "__int16";
            case U32:
                return "unsigned __int32";
            case S32:
                return "__int32";
            case U64:
                return "unsigned __int64";
            case S64:
                return "__int64";
            case F32:
                return "float";
            case F64:
                return "double";
            case FX16:
                return "fx16";
            case FX32:
                return "fx32";
            case PTR:
                if (isConst) {
                    return pointee.toCType() + "* const";
                } else {
                    return pointee.toCType() + "*";
                }
            default:
                return "int"; // unknown
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Type)) {
            return false;
        }
        Type t = (Type) o;

        return t.isConst == isConst && t.representation == representation && Objects.equals(t.type, type) && Objects.equals(t.struct, struct) && Objects.equals(t.expr, expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isConst, representation, type, struct, expr);
    }
}
