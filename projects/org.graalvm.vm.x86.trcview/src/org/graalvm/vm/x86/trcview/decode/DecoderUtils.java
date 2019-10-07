package org.graalvm.vm.x86.trcview.decode;

import static org.graalvm.vm.util.HexFormatter.tohex;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;

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

    public static String encode(int b) {
        switch (b) {
            case '"':
                return "\\\"";
            case '\\':
                return "\\\\";
            case '\r':
                return "\\r";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            case '\f':
                return "\\f";
            case '\b':
                return "\\b";
            default:
                if (b < 0x20) {
                    return "\\x" + HexFormatter.tohex(b, 2);
                } else {
                    return Character.toString((char) b);
                }
        }
    }

    public static String cstr(long addr, long insn, TraceAnalyzer trc) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; true; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, insn));
                if (b == 0) {
                    return "\"" + buf + "\"";
                }
                buf.append(encode(b));
                if (i >= STRING_MAXLEN) {
                    return "\"" + buf + "\"...";
                }
            }
        } catch (MemoryNotMappedException e) {
            return "0x" + hex(addr);
        }
    }

    public static String mem(long addr, long length, long insn, TraceAnalyzer trc) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; i < length; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, insn));
                buf.append(encode(b));
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
