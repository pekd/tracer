package org.graalvm.vm.trcview.net.protocol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.stream.Collectors;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class IO {
    public static final String readString(WordInputStream in) throws IOException {
        int length = in.read16bit() & 0xFFFF;
        if (length == 0xFFFF) {
            return null;
        } else if (length == 0) {
            return "";
        } else {
            byte[] bytes = new byte[length];
            in.read(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static final void writeString(WordOutputStream out, String s) throws IOException {
        if (s == null) {
            out.write16bit((short) -1);
        } else if (s.length() == 0) {
            out.write16bit((short) 0);
        } else {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            out.write16bit((short) bytes.length);
            out.write(bytes);
        }
    }

    public static final String[] readStringArray(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            String[] result = new String[length];
            for (int i = 0; i < result.length; i++) {
                int slen = in.read16bit();
                if (slen == 0) {
                    result[i] = null;
                } else {
                    byte[] bytes = new byte[slen];
                    in.read(bytes);
                    result[i] = new String(bytes, StandardCharsets.UTF_8);
                }
            }
            return result;
        }
    }

    public static final void writeStringArray(WordOutputStream out, String[] data) throws IOException {
        if (data == null) {
            out.write16bit((short) 0);
        } else {
            out.write16bit((short) data.length);
            for (int i = 0; i < data.length; i++) {
                writeString(out, data[i]);
            }
        }
    }

    public static final byte[] readArray(WordInputStream in) throws IOException {
        int length = in.read32bit();
        if (length == 0) {
            return null;
        } else {
            byte[] data = new byte[length];
            in.read(data);
            return data;
        }
    }

    public static final void writeArray(WordOutputStream out, byte[] data) throws IOException {
        if (data == null) {
            out.write32bit(0);
        } else {
            out.write32bit(data.length);
            out.write(data);
        }
    }

    public static final byte[] readShortArray(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            byte[] data = new byte[length];
            in.read(data);
            return data;
        }
    }

    public static final void writeShortArray(WordOutputStream out, byte[] data) throws IOException {
        if (data == null) {
            out.write16bit((short) 0);
        } else {
            out.write16bit((short) data.length);
            out.write(data);
        }
    }

    public static final byte[][] readShortArray2(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            byte[][] data = new byte[length][];
            for (int i = 0; i < data.length; i++) {
                data[i] = new byte[in.read16bit()];
                in.read(data[i]);
            }
            return data;
        }
    }

    public static final void writeShortArray2(WordOutputStream out, byte[][] data) throws IOException {
        if (data == null) {
            out.write16bit((short) 0);
        } else {
            out.write16bit((short) data.length);
            for (int i = 0; i < data.length; i++) {
                out.write16bit((short) data[i].length);
                out.write(data[i]);
            }
        }
    }

    public static final Prototype readPrototype(WordInputStream in) throws IOException {
        if (in.read() == 0) {
            return null;
        }
        String rettype = IO.readString(in);
        String args = IO.readString(in);
        try {
            Function fun = new Parser(rettype + " f(" + args + ")").parsePrototype();
            return fun.getPrototype();
        } catch (ParseException e) {
            throw new IOException("Invalid prototype: " + e.getMessage() + " [prototype = '" + rettype + " f(" + args + ")']", e);
        }
    }

    public static final void writePrototype(WordOutputStream out, Prototype proto) throws IOException {
        if (proto == null) {
            out.write(0);
        } else {
            out.write(1);
            writeString(out, proto.returnType.toString());
            writeString(out, proto.args.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
    }
}
