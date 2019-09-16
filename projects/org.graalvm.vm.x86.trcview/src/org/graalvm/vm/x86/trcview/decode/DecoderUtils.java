package org.graalvm.vm.x86.trcview.decode;

import static org.graalvm.vm.util.HexFormatter.tohex;

import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;

public class DecoderUtils {
    public static final int STRING_MAXLEN = 50;

    private static String hex(long x) {
        return tohex(x, 1);
    }

    public static String ptr(long x) {
        if (x == 0) {
            return "NULL";
        } else {
            return "0x" + hex(x);
        }
    }

    private static void append(StringBuilder buf, int b) {
        switch (b) {
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            case '\r':
                buf.append("\\r");
                break;
            case '\n':
                buf.append("\\n");
                break;
            case '\t':
                buf.append("\\t");
                break;
            case '\f':
                buf.append("\\f");
                break;
            case '\b':
                buf.append("\\b");
                break;
            default:
                if (b < 0x20) {
                    buf.append(String.format("\\x%02x", b));
                } else {
                    buf.append((char) b);
                }
        }
    }

    public static String cstr(long addr, long insn, MemoryTrace mem) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; true; i++) {
                int b = Byte.toUnsignedInt(mem.getByte(ptr++, insn));
                if (b == 0) {
                    return "\"" + buf + "\"";
                }
                append(buf, b);
                if (i >= STRING_MAXLEN) {
                    return "\"" + buf + "\"...";
                }
            }
        } catch (MemoryNotMappedException e) {
            return "0x" + hex(addr);
        }
    }

    public static String mem(long addr, long length, long insn, MemoryTrace mem) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; i < length; i++) {
                int b = Byte.toUnsignedInt(mem.getByte(ptr++, insn));
                append(buf, b);
                if (i >= STRING_MAXLEN) {
                    return "\"" + buf + "\"...";
                }
            }
            return "\"" + buf + "\"";
        } catch (MemoryNotMappedException e) {
            return "0x" + hex(addr);
        }
    }
}
