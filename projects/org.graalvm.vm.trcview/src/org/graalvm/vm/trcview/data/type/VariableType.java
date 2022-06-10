package org.graalvm.vm.trcview.data.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.util.BitTest;

public class VariableType {
    public static final VariableType GENERIC_POINTER = new VariableType(0, 0, "void*");
    public static final VariableType POINTER_I8 = new VariableType(1, 0, "I8*");
    public static final VariableType POINTER_I16 = new VariableType(2, 0, "I16*");
    public static final VariableType POINTER_I32 = new VariableType(3, 0, "I32*");
    public static final VariableType POINTER_I64 = new VariableType(4, 0, "I64*");
    public static final VariableType POINTER_U8 = new VariableType(5, 0, "U8*");
    public static final VariableType POINTER_U16 = new VariableType(6, 0, "U16*");
    public static final VariableType POINTER_U32 = new VariableType(7, 0, "U32*");
    public static final VariableType POINTER_U64 = new VariableType(8, 0, "U64*");
    public static final VariableType POINTER_S8 = new VariableType(9, 0, "S8*");
    public static final VariableType POINTER_S16 = new VariableType(10, 0, "S16*");
    public static final VariableType POINTER_S32 = new VariableType(11, 0, "S32*");
    public static final VariableType POINTER_S64 = new VariableType(12, 0, "S64*");
    public static final VariableType POINTER_F32 = new VariableType(13, 0, "F32*");
    public static final VariableType POINTER_F64 = new VariableType(14, 0, "F64*");
    public static final VariableType POINTER_FX16 = new VariableType(15, 0, "FX16*");
    public static final VariableType POINTER_FX32 = new VariableType(16, 0, "FX32*");
    public static final VariableType POINTER_CODE = new VariableType(17, 0, "CODE*");
    public static final VariableType I8 = new VariableType(18, 1, "I8");
    public static final VariableType I16 = new VariableType(19, 2, "I16");
    public static final VariableType I32 = new VariableType(20, 4, "I32");
    public static final VariableType I64 = new VariableType(21, 8, "I64");
    public static final VariableType U8 = new VariableType(22, 1, "U8");
    public static final VariableType U16 = new VariableType(23, 2, "U16");
    public static final VariableType U32 = new VariableType(24, 4, "U32");
    public static final VariableType U64 = new VariableType(25, 8, "U64");
    public static final VariableType S8 = new VariableType(26, 1, "S8");
    public static final VariableType S16 = new VariableType(27, 2, "S16");
    public static final VariableType S32 = new VariableType(28, 4, "S32");
    public static final VariableType S64 = new VariableType(29, 8, "S64");
    public static final VariableType F32 = new VariableType(30, 4, "F32");
    public static final VariableType F64 = new VariableType(31, 8, "F64");
    public static final VariableType FX16 = new VariableType(32, 2, "FX16");
    public static final VariableType FX32 = new VariableType(33, 4, "FX32");
    public static final VariableType PC = new VariableType(34, 0, "PC");
    public static final VariableType SP = new VariableType(35, 0, "SP");
    public static final VariableType FLAGS = new VariableType(36, 0, "FLAGS");

    public static final VariableType CONFLICT = new VariableType(58, 1, "CONFLICT");
    public static final VariableType UNKNOWN = new VariableType(59, 1, "UNKNOWN");

    public static final long CHAIN_BIT = 0x80000000_00000000L;
    public static final long BREAK_BIT = 0x40000000_00000000L;

    public static final long ADDSUB_BIT = 0x20000000_00000000L;
    public static final long MUL_BIT = 0x10000000_00000000L;

    public static final long BIT_MASK = ~0xF0000000_00000000L;

    private final int bit;
    private final long mask;
    private final int size;
    private final String name;

    private static final List<VariableType> TYPES = Collections.unmodifiableList(
                    Arrays.asList(GENERIC_POINTER, POINTER_I8, POINTER_I16, POINTER_I32, POINTER_I32, POINTER_I64, POINTER_U8, POINTER_U16, POINTER_U32, POINTER_U64, POINTER_S8, POINTER_S16,
                                    POINTER_S32, POINTER_S64, POINTER_F32, POINTER_F64, POINTER_FX16, POINTER_FX32, POINTER_CODE, I8, I16, I32, I64, U8, U16, U32, U64, S8, S16, S32, S64, F32, F64,
                                    FX16, FX32, PC, SP, FLAGS));

    public VariableType(int bit, int size, String name) {
        this.bit = bit;
        mask = 1L << bit;
        this.size = size;
        this.name = name;
    }

    public long getMask() {
        return mask;
    }

    public int getSize(int ptrSize) {
        if (size == 0) {
            return ptrSize;
        } else {
            return size;
        }
    }

    public long combine(long bits) {
        return bits | mask;
    }

    public boolean test(long bits) {
        return BitTest.test(bits, mask);
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof VariableType)) {
            return false;
        }

        VariableType t = (VariableType) o;

        return t.mask == mask;
    }

    @Override
    public int hashCode() {
        return (int) (mask ^ (mask >> 32));
    }

    @Override
    public String toString() {
        return name;
    }

    public static List<VariableType> getTypeConstraints() {
        return TYPES;
    }

    public static VariableType resolve(long bitmask, int addrsize) {
        long bits = bitmask & ~(CHAIN_BIT | BREAK_BIT | ADDSUB_BIT | MUL_BIT | UNKNOWN.mask | CONFLICT.mask | PC.mask | SP.mask);
        long ptrbits = GENERIC_POINTER.mask | POINTER_I8.mask | POINTER_I16.mask | POINTER_I32.mask | POINTER_I64.mask | POINTER_U8.mask | POINTER_U16.mask | POINTER_U32.mask | POINTER_U64.mask |
                        POINTER_S8.mask | POINTER_S16.mask | POINTER_S32.mask | POINTER_S64.mask | POINTER_F32.mask | POINTER_F64.mask | POINTER_FX16.mask | POINTER_FX32.mask | POINTER_CODE.mask;

        if (bits == 0) {
            return null;
        }

        long addrbits;
        switch (addrsize) {
            case 1:
                addrbits = I8.mask | U8.mask | S8.mask;
                break;
            case 2:
                addrbits = I16.mask | U16.mask | S16.mask;
                break;
            case 4:
                addrbits = I32.mask | U32.mask | S32.mask;
                break;
            case 8:
                addrbits = I64.mask | U64.mask | S64.mask;
                break;
            default:
                throw new IllegalArgumentException("invalid address size: " + addrsize);
        }

        // check conflicts
        long intmask = I8.mask | I16.mask | I32.mask | I64.mask;
        long uintmask = U8.mask | U16.mask | U32.mask | U64.mask;
        long sintmask = S8.mask | S16.mask | S32.mask | S64.mask;

        long intbits = Long.bitCount(bits & intmask);
        long uintbits = Long.bitCount(bits & uintmask);
        long sintbits = Long.bitCount(bits & sintmask);

        if (intbits > 1 || uintbits > 1 || sintbits > 1) {
            return UNKNOWN;
        }

        // S8:
        if (S8.test(bits)) {
            if (U8.test(bits)) {
                long m = U8.mask | S8.mask | I8.mask;
                if ((bits & ~m) == 0) {
                    return I8;
                } else {
                    return UNKNOWN;
                }
            } else {
                long m = S8.mask | I8.mask;
                if ((bits & ~m) == 0) {
                    return S8;
                } else {
                    return UNKNOWN;
                }
            }
        }

        // U8:
        if (U8.test(bits)) {
            long m = U8.mask | I8.mask;
            if ((bits & ~m) == 0) {
                return U8;
            } else {
                return UNKNOWN;
            }
        }

        if (I8.test(bits) && (bits & ~I8.mask) == 0) {
            return I8;
        }

        // S16:
        if (S16.test(bits)) {
            if (U16.test(bits)) {
                long m = U16.mask | S16.mask | I16.mask;
                if ((bits & ~m) == 0) {
                    return I16;
                } else {
                    return UNKNOWN;
                }
            } else {
                long m = S16.mask | I16.mask;
                if ((bits & ~m) == 0) {
                    return S16;
                } else {
                    return UNKNOWN;
                }
            }
        }

        // U16:
        if (U16.test(bits)) {
            long m = U16.mask | I16.mask;
            if ((bits & ~m) == 0) {
                return U16;
            } else {
                return UNKNOWN;
            }
        }

        if (I16.test(bits) && (bits & ~I16.mask) == 0) {
            return I16;
        }

        // precise pointer types for f32/f64/fx16/fx32
        if (POINTER_F32.test(bits) && (bits & ~(addrbits | POINTER_F32.mask)) == 0) {
            return POINTER_F32;
        }

        if (POINTER_F64.test(bits) && (bits & ~(addrbits | POINTER_F64.mask)) == 0) {
            return POINTER_F64;
        }

        if (POINTER_FX16.test(bits) && (bits & ~(addrbits | POINTER_FX16.mask)) == 0) {
            return POINTER_FX16;
        }

        if (POINTER_FX32.test(bits) && (bits & ~(addrbits | POINTER_FX32.mask)) == 0) {
            return POINTER_FX32;
        }

        // precise pointer types with signed/unsigned
        if (POINTER_U8.test(bits) && (bits & ~(addrbits | POINTER_U8.mask | POINTER_I8.mask)) == 0) {
            return POINTER_U8;
        }

        if (POINTER_S8.test(bits) && (bits & ~(addrbits | POINTER_S8.mask | POINTER_I8.mask)) == 0) {
            return POINTER_S8;
        }

        if (POINTER_U16.test(bits) && (bits & ~(addrbits | POINTER_U16.mask | POINTER_I16.mask)) == 0) {
            return POINTER_U16;
        }

        if (POINTER_S16.test(bits) && (bits & ~(addrbits | POINTER_S16.mask | POINTER_I16.mask)) == 0) {
            return POINTER_S16;
        }

        if (POINTER_U32.test(bits) && (bits & ~(addrbits | POINTER_U32.mask | POINTER_I32.mask)) == 0) {
            return POINTER_U32;
        }

        if (POINTER_S32.test(bits) && (bits & ~(addrbits | POINTER_S32.mask | POINTER_I32.mask)) == 0) {
            return POINTER_S32;
        }

        // generic pointer types
        // I8*
        if (POINTER_I8.test(bits) && (bits & ~(addrbits | POINTER_I8.mask | POINTER_U8.mask | POINTER_S8.mask)) == 0) {
            return POINTER_I8;
        }

        // I16*
        if (POINTER_I16.test(bits) && (bits & ~(addrbits | POINTER_I16.mask | POINTER_U16.mask | POINTER_S16.mask)) == 0) {
            return POINTER_I16;
        }

        // I32*
        if (POINTER_I32.test(bits) && (bits & ~(addrbits | POINTER_I32.mask | POINTER_U32.mask | POINTER_S32.mask)) == 0) {
            return POINTER_I32;
        }

        // I64*
        if (POINTER_I64.test(bits) && (bits & ~(addrbits | POINTER_I64.mask | POINTER_U64.mask | POINTER_S64.mask)) == 0) {
            return POINTER_I64;
        }

        // check for generic pointers
        if ((bits & addrbits) != 0 && (bits & ~(addrbits | ptrbits)) == 0) {
            return GENERIC_POINTER;
        }

        // conflicting pointer types => generic pointer
        if ((bits & ~(addrbits | ptrbits)) == 0 && (bits & ptrbits) != 0) {
            return GENERIC_POINTER;
        }

        return UNKNOWN;
    }

    public Type toType(ArchitectureTypeInfo info) {
        if (bit == GENERIC_POINTER.bit) {
            return new Type(new Type(DataType.VOID), info);
        } else if (bit == POINTER_I8.bit || bit == POINTER_U8.bit) {
            return new Type(new Type(DataType.U8), info);
        } else if (bit == POINTER_S8.bit) {
            return new Type(new Type(DataType.S8), info);
        } else if (bit == POINTER_I16.bit || bit == POINTER_U16.bit) {
            return new Type(new Type(DataType.U16), info);
        } else if (bit == POINTER_S16.bit) {
            return new Type(new Type(DataType.S16), info);
        } else if (bit == POINTER_I32.bit || bit == POINTER_U32.bit) {
            return new Type(new Type(DataType.U32), info);
        } else if (bit == POINTER_S32.bit) {
            return new Type(new Type(DataType.S32), info);
        } else if (bit == POINTER_I64.bit || bit == POINTER_U64.bit) {
            return new Type(new Type(DataType.U64), info);
        } else if (bit == POINTER_S64.bit) {
            return new Type(new Type(DataType.S64), info);
        } else if (bit == POINTER_F32.bit) {
            return new Type(new Type(DataType.F32), info);
        } else if (bit == POINTER_F64.bit) {
            return new Type(new Type(DataType.F64), info);
        } else if (bit == POINTER_FX16.bit) {
            return new Type(new Type(DataType.FX16), info);
        } else if (bit == POINTER_FX32.bit) {
            return new Type(new Type(DataType.FX32), info);
        } else if (bit == POINTER_CODE.bit) {
            return new Type(new Type(DataType.CODE), info);
        } else if (bit == I8.bit || bit == U8.bit) {
            return new Type(DataType.U8);
        } else if (bit == S8.bit) {
            return new Type(DataType.S8);
        } else if (bit == I16.bit || bit == U16.bit) {
            return new Type(DataType.U16);
        } else if (bit == S16.bit) {
            return new Type(DataType.S16);
        } else if (bit == I32.bit || bit == U32.bit) {
            return new Type(DataType.U32);
        } else if (bit == S32.bit) {
            return new Type(DataType.S32);
        } else if (bit == I64.bit || bit == U64.bit) {
            return new Type(DataType.U64);
        } else if (bit == S64.bit) {
            return new Type(DataType.S64);
        } else if (bit == F32.bit) {
            return new Type(DataType.F32);
        } else if (bit == F64.bit) {
            return new Type(DataType.F64);
        } else if (bit == FX16.bit) {
            return new Type(DataType.FX16);
        } else if (bit == FX32.bit) {
            return new Type(DataType.FX32);
        }
        return null;
    }
}
