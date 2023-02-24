package org.graalvm.vm.trcview.ui.data;

import java.util.List;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;

public class StringDataLine extends DataLine {
    public static final long MAX_LENGTH = 64;

    private final long start;
    private final long length;

    public StringDataLine(long addr, long start, Type type, long step, TraceAnalyzer trc) {
        this(addr, 0, null, -1, start, type, step, trc);
    }

    public StringDataLine(long addr, long offset, String name, long index, long start, Type type, long step, TraceAnalyzer trc) {
        super(addr, offset, name, index, type, step, trc);
        this.start = start;
        this.length = getLength(type, start);
        omitLabel = start != 0;
    }

    private static long getLength(Type type, long start) {
        long remainder = type.getElements() - start;
        long len;
        if (remainder > MAX_LENGTH) {
            len = MAX_LENGTH;
        } else {
            len = remainder;
        }
        return len;
    }

    @Override
    protected void addData(List<Element> result) {
        StringBuilder buf = new StringBuilder();
        String val;
        long sz = type.getElementSize();
        try {
            long ptr = addr + offset + start * sz;
            buf.append('"');
            for (long i = 0; i < length; i++) {
                int b;
                if (sz == 2) {
                    b = Short.toUnsignedInt(trc.getI16(ptr, step));
                } else {
                    b = Byte.toUnsignedInt(trc.getI8(ptr, step));
                }
                ptr += sz;
                buf.append(DecoderUtils.encode(b));
            }
            buf.append('"');
            val = buf.toString();
        } catch (MemoryNotMappedException e) {
            if (buf.length() > 1) {
                buf.append("\", ??");
                val = buf.toString();
            } else {
                val = buf.substring(1);
            }
        }
        if (sz == 2) {
            result.add(new DefaultElement("DCW", Element.TYPE_KEYWORD));
        } else {
            result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
        }

        long ptr = addr + offset + start * sz;
        result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
        result.add(new DataElement(val, Element.TYPE_STRING, ptr));
    }
}
