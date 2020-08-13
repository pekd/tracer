package org.graalvm.vm.trcview.libtrc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

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

    private Field pc;

    private String format;
    private List<StateField> layout;

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

    private static int getType(Field field) {
        Class<?> type = field.getType();
        if (type == byte.class) {
            return TYPE_I8;
        } else if (type == short.class) {
            return TYPE_I16;
        } else if (type == int.class) {
            return TYPE_I32;
        } else if (type == long.class) {
            return TYPE_I64;
        } else if (type == float.class) {
            return TYPE_F32;
        } else if (type == double.class) {
            return TYPE_F64;
        } else {
            throw new IllegalArgumentException("invalid field type");
        }
    }

    private static StateField getLayoutField(Field field, int offset) {
        String name = field.getName();

        int fmt = StateField.FORMAT_HEX;
        Register[] annotations = field.getAnnotationsByType(Register.class);
        if (annotations.length == 1) {
            fmt = annotations[0].value();
        }

        return new StateField(name, getType(field), fmt, offset);
    }

    private static boolean checkType(Class<?> type) {
        return type == byte.class || type == short.class || type == int.class || type == long.class || type == float.class || type == double.class;
    }

    public StateSerializer(Class<T> clazz) {
        // collect all relevant fields
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Register.class)) {
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
        layout = new ArrayList<>();
        int off = 0;
        for (Field field : stateFields) {
            layout.add(getLayoutField(field, off));
            off += getSize(field);
        }

        // get program counter offset and size
        for (Field field : stateFields) {
            if (field.isAnnotationPresent(ProgramCounter.class)) {
                pc = field;
                break;
            }
        }
        if (pc == null) {
            log.log(Levels.WARNING, "No program counter defined in " + clazz.getSimpleName());
        }

        // compute total size
        size = 0;
        for (Field field : stateFields) {
            size += getSize(field);
        }

        log.log(Levels.INFO, "State has " + stateFields.size() + " fields with " + size + " bytes");
    }

    public List<StateField> getLayout() {
        return layout;
    }

    public String getFormat() {
        return format;
    }

    public int getSize() {
        return size;
    }

    public long getPC(T state) throws IllegalArgumentException, IllegalAccessException {
        if (pc == null) {
            throw new IllegalStateException("no pc defined");
        }
        Class<?> type = pc.getType();
        if (type == byte.class) {
            return Byte.toUnsignedLong(pc.getByte(state));
        } else if (type == short.class) {
            return Short.toUnsignedLong(pc.getShort(state));
        } else if (type == int.class) {
            return Integer.toUnsignedLong(pc.getInt(state));
        } else if (type == long.class) {
            return pc.getLong(state);
        } else if (type == float.class) {
            return Integer.toUnsignedLong(Float.floatToRawIntBits(pc.getFloat(state)));
        } else if (type == double.class) {
            return Double.doubleToRawLongBits(pc.getDouble(state));
        } else {
            throw new IllegalArgumentException("invalid field type");
        }
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
