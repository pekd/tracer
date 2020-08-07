package org.graalvm.vm.trcview.libtrc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class StateSerializer<T> {
    private static final Logger log = Trace.create(StateSerializer.class);

    public static final byte TYPE_I8 = 0;
    public static final byte TYPE_I16 = 1;
    public static final byte TYPE_I32 = 2;
    public static final byte TYPE_I64 = 3;
    public static final byte TYPE_F32 = 4;
    public static final byte TYPE_F64 = 5;

    private List<Field> stateFields = new ArrayList<>();

    private int pcOffset;
    private int pcSize;

    private String layout;
    private String format;

    private int size;

    private static int getSize(Field field) {
        Class<?> type = field.getType();
        if (type == byte.class) {
            return 1;
        } else if (type == short.class) {
            return 2;
        } else if (type == int.class) {
            return 4;
        } else if (type == long.class) {
            return 8;
        } else if (type == float.class) {
            return 4;
        } else if (type == double.class) {
            return 8;
        } else {
            throw new IllegalArgumentException("invalid field type");
        }
    }

    private static String getLayoutField(Field field) {
        String name = field.getName();
        Class<?> type = field.getType();
        if (type == byte.class) {
            return "u8 " + name + ";";
        } else if (type == short.class) {
            return "u16 " + name + ";";
        } else if (type == int.class || type == float.class) {
            return "u32 " + name + ";";
        } else if (type == long.class || type == double.class) {
            return "u64 " + name + ";";
        } else {
            throw new IllegalArgumentException("invalid field type");
        }
    }

    private static boolean checkType(Class<?> type) {
        return type == byte.class || type == short.class || type == int.class || type == long.class || type == float.class || type == double.class;
    }

    public StateSerializer(Class<T> clazz) {
        // collect all relevant fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Register.class) || field.isAnnotationPresent(ProgramCounter.class)) {
                if (checkType(field.getType())) {
                    stateFields.add(field);
                } else {
                    log.log(Levels.WARNING, "Invalid type " + field.getType().getSimpleName() + " in field " + field.getName());
                }
            }
        }

        // get state format
        StateFormat[] fmt = clazz.getAnnotationsByType(StateFormat.class);
        if (fmt.length == 0) {
            log.log(Levels.WARNING, "No StateFormat annotation found");
        } else if (fmt.length > 1) {
            log.log(Levels.WARNING, "More than one StateFormat annotation found");
        } else {
            format = fmt[0].value();
        }

        // sort to improve alignment
        Collections.sort(stateFields, (a, b) -> getSize(b) - getSize(a));
        layout = stateFields.stream().map(StateSerializer::getLayoutField).collect(Collectors.joining(" "));

        // get program counter offset and size
        int off = 0;
        for (Field field : stateFields) {
            if (field.isAnnotationPresent(ProgramCounter.class)) {
                pcOffset = off;
                pcSize = getSize(field);
                break;
            } else {
                off += getSize(field);
            }
        }
        if (pcOffset == 0 && pcSize == 0) {
            log.log(Levels.WARNING, "No program counter defined in " + clazz.getSimpleName());
        }

        // compute total size
        size = 0;
        for (Field field : stateFields) {
            size += getSize(field);
        }

        log.log(Levels.INFO, "State has " + stateFields.size() + " fields with " + size + " bytes");
    }

    public String getLayout() {
        return layout;
    }

    public String getFormat() {
        return format;
    }

    public int getSize() {
        return size;
    }

    public int getPCOffset() {
        return pcOffset;
    }

    public int getPCSize() {
        return pcSize;
    }

    public byte[] serialize(T state) throws IllegalArgumentException, IllegalAccessException {
        byte[] result = new byte[size];
        int off = 0;
        for (Field field : stateFields) {
            Class<?> type = field.getType();
            if (type == byte.class) {
                byte val = field.getByte(state);
                result[off++] = val;
            } else if (type == short.class) {
                short val = field.getShort(state);
                result[off++] = (byte) (val >> 8);
                result[off++] = (byte) val;
            } else if (type == int.class) {
                int val = field.getInt(state);
                result[off++] = (byte) (val >> 24);
                result[off++] = (byte) (val >> 16);
                result[off++] = (byte) (val >> 8);
                result[off++] = (byte) val;
            } else if (type == long.class) {
                long val = field.getLong(state);
                result[off++] = (byte) (val >> 56);
                result[off++] = (byte) (val >> 48);
                result[off++] = (byte) (val >> 40);
                result[off++] = (byte) (val >> 32);
                result[off++] = (byte) (val >> 24);
                result[off++] = (byte) (val >> 16);
                result[off++] = (byte) (val >> 8);
                result[off++] = (byte) val;
            } else if (type == float.class) {
                int val = Float.floatToRawIntBits(field.getFloat(state));
                result[off++] = (byte) (val >> 24);
                result[off++] = (byte) (val >> 16);
                result[off++] = (byte) (val >> 8);
                result[off++] = (byte) val;
            } else if (type == double.class) {
                long val = Double.doubleToRawLongBits(field.getDouble(state));
                result[off++] = (byte) (val >> 56);
                result[off++] = (byte) (val >> 48);
                result[off++] = (byte) (val >> 40);
                result[off++] = (byte) (val >> 32);
                result[off++] = (byte) (val >> 24);
                result[off++] = (byte) (val >> 16);
                result[off++] = (byte) (val >> 8);
                result[off++] = (byte) val;
            }
        }
        assert off == size;
        return result;
    }
}
