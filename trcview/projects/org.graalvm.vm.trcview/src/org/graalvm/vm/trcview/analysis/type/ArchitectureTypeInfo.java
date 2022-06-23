package org.graalvm.vm.trcview.analysis.type;

public class ArchitectureTypeInfo {
    public static final ArchitectureTypeInfo LP32 = new ArchitectureTypeInfo(4, 2, 2, 4);
    public static final ArchitectureTypeInfo ILP32 = new ArchitectureTypeInfo(4, 2, 4, 4);
    public static final ArchitectureTypeInfo LLP64 = new ArchitectureTypeInfo(8, 2, 4, 4);
    public static final ArchitectureTypeInfo LP64 = new ArchitectureTypeInfo(8, 2, 4, 8);
    public static final ArchitectureTypeInfo ILP64 = new ArchitectureTypeInfo(8, 2, 8, 8);
    public static final ArchitectureTypeInfo SILP64 = new ArchitectureTypeInfo(8, 8, 8, 8);

    private final int pointerSize;
    private final int shortSize;
    private final int intSize;
    private final int longSize;

    public ArchitectureTypeInfo(int pointerSize, int shortSize, int intSize, int longSize) {
        if (shortSize > intSize) {
            throw new IllegalArgumentException("sizeof(short) must be less than or equal to sizeof(int)");
        }
        if (intSize > longSize) {
            throw new IllegalArgumentException("sizeof(int) must be less than or equal to sizeof(long)");
        }

        this.pointerSize = pointerSize;
        this.shortSize = shortSize;
        this.intSize = intSize;
        this.longSize = longSize;
    }

    public int getPointerSize() {
        return pointerSize;
    }

    public int getShortSize() {
        return shortSize;
    }

    public int getIntSize() {
        return intSize;
    }

    public int getLongSize() {
        return longSize;
    }

    private static DataType getType(int size, boolean isUnsigned) {
        if (isUnsigned) {
            switch (size) {
                case 1:
                    return DataType.U8;
                case 2:
                    return DataType.U16;
                case 4:
                    return DataType.U32;
                case 8:
                    return DataType.U64;
                default:
                    throw new IllegalArgumentException("invalid size " + size);
            }
        } else {
            switch (size) {
                case 1:
                    return DataType.S8;
                case 2:
                    return DataType.S16;
                case 4:
                    return DataType.S32;
                case 8:
                    return DataType.S64;
                default:
                    throw new IllegalArgumentException("invalid size " + size);
            }
        }
    }

    public DataType getShortType(boolean isUnsigned) {
        return getType(shortSize, isUnsigned);
    }

    public DataType getIntType(boolean isUnsigned) {
        return getType(intSize, isUnsigned);
    }

    public DataType getLongType(boolean isUnsigned) {
        return getType(longSize, isUnsigned);
    }
}
