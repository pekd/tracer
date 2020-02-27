package org.graalvm.vm.trcview.decode;

import static org.graalvm.vm.util.HexFormatter.tohex;
import static org.graalvm.vm.util.OctFormatter.tooct;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

public class DecoderUtils {
    public static final int STRING_MAXLEN = 50;

    private static String hex(long x) {
        return tohex(x, 1);
    }

    private static String oct(long x) {
        return tooct(x, 1);
    }

    public static String ptr(long x) {
        return ptr(x, false);
    }

    public static String ptr(long x, StepFormat format) {
        return ptr(x, format.numberfmt == StepFormat.NUMBERFMT_OCT);
    }

    public static String ptr(long x, TraceAnalyzer trc) {
        return ptr(x, trc.getArchitecture().getFormat());
    }

    public static String ptr(long x, boolean oct) {
        if (x == 0) {
            return "NULL";
        } else if (oct) {
            return "0" + oct(x);
        } else {
            return "0x" + hex(x);
        }
    }

    public static String encode(int b) {
        switch (b) {
            case 0:
                return "\\0";
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

    public static String encode(byte b) {
        switch (Byte.toUnsignedInt(b)) {
            case 0:
                return "\\0";
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
                if (b < 0x20 || b >= 0x7f) {
                    return "\\x" + HexFormatter.tohex(Byte.toUnsignedInt(b), 2);
                } else {
                    return Character.toString((char) Byte.toUnsignedInt(b));
                }
        }
    }

    public static String str(String s) {
        if (s == null) {
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            buf.append(encode(c));
        }
        return '"' + buf.toString() + '"';
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
            return ptr(addr, trc);
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
            return ptr(addr, trc);
        }
    }
}
