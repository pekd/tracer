package org.graalvm.vm.trcview.info;

import org.graalvm.vm.memory.util.Stringify;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.OctFormatter;

public class Formatter {
    private static String bytes(long addr, long step, TraceAnalyzer trc) {
        if (addr == 0) {
            return "NULL";
        }
        try {
            StringBuilder buf = new StringBuilder();
            long ptr = addr;
            for (int i = 0; i < 32; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, step));
                buf.append(HexFormatter.tohex(b, 2));
                buf.append(' ');
            }
            return buf.toString().trim();
        } catch (MemoryNotMappedException e) {
            return "0x" + HexFormatter.tohex(addr);
        }
    }

    public static String format(String type, StepFormat format, long step, TraceAnalyzer trc, long... value) {
        if (type == null) {
            if (format.numberfmt == StepFormat.NUMBERFMT_HEX) {
                return "0x" + HexFormatter.tohex(value[0]) + " [" + value[0] + "]";
            } else if (format.numberfmt == StepFormat.NUMBERFMT_OCT) {
                return OctFormatter.tooct(value[0]) + " [" + value[0] + "]";
            } else {
                return format.formatWord(value[0]) + " [" + value[0] + "]";
            }
        } else {
            StringBuilder buf = new StringBuilder();
            boolean dec = false;
            int i = 0;
            for (char c : type.toCharArray()) {
                long val;
                if (value.length == 0) {
                    val = 0;
                } else if (i >= value.length) {
                    val = value[value.length - 1];
                } else {
                    val = value[i];
                }
                if (dec) {
                    switch (c) {
                        case 'i':
                        case 'n':
                            buf.append(format.formatWord(val));
                            i++;
                            break;
                        case 'd':
                            buf.append(val);
                            i++;
                            break;
                        case 'u':
                            buf.append(Long.toUnsignedString(val));
                            i++;
                            break;
                        case 'x':
                            buf.append(HexFormatter.tohex(val).toLowerCase());
                            i++;
                            break;
                        case 'X':
                            buf.append(HexFormatter.tohex(val).toUpperCase());
                            i++;
                            break;
                        case 'o':
                            buf.append(OctFormatter.tooct(val));
                            i++;
                            break;
                        case 'c':
                            buf.append((char) val);
                            i++;
                            break;
                        case 'C':
                            buf.append(DecoderUtils.encode((int) val & 0xFFFF));
                            i++;
                            break;
                        case 's':
                            buf.append(DecoderUtils.cstr(val, step, trc));
                            i++;
                            break;
                        case 'm':
                            buf.append(bytes(val, step, trc));
                            i++;
                            break;
                        case 'r':
                            buf.append(Stringify.i64force(val));
                            i++;
                            break;
                        case 'p':
                            buf.append(DecoderUtils.ptr(val, format));
                            i++;
                            break;
                        default:
                            buf.append(c);
                            break;
                    }
                    dec = false;
                } else {
                    if (c == '%') {
                        dec = true;
                    } else {
                        buf.append(c);
                    }
                }
            }
            return buf.toString();
        }
    }
}
