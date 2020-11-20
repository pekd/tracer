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
        super(addr, type, step, trc);
        this.start = start;
        this.length = getLength(type, start);
        omitLabel = start != 0;
    }

    public static long getLength(Type type, long start) {
        long remainder = type.getElements() - start;
        long len;
        if (remainder > MAX_LENGTH) {
            len = MAX_LENGTH;
        } else {
            len = remainder;
        }
        return len * type.getElementSize();
    }

    @Override
    protected void addData(List<Element> result) {
        StringBuilder buf = new StringBuilder();
        String val;
        try {
            long ptr = addr + start * type.getElementSize();
            buf.append('"');
            for (long i = 0; i < length; i++) {
                int b = Byte.toUnsignedInt(trc.getI8(ptr++, step));
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
        result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
        result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
        result.add(new DefaultElement(val, Element.TYPE_STRING));
    }
}
