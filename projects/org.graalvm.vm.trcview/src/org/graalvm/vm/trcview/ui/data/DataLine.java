package org.graalvm.vm.trcview.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.trcview.ui.data.editor.Line;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.StringUtils;

public class DataLine extends Line {
    private final TraceAnalyzer trc;
    private final long step;
    private final long addr;
    private final int size;
    private List<Element> elements;

    public DataLine(long addr, int size, long step, TraceAnalyzer trc) {
        this.addr = addr;
        this.size = size;
        this.step = step;
        this.trc = trc;
    }

    private List<Element> createElements() {
        StepFormat fmt = trc.getArchitecture().getFormat();

        List<Element> result = new ArrayList<>();
        String label = StringUtils.pad("unk_" + fmt.formatShortAddress(addr), 40);
        String address = fmt.formatAddress(addr);

        result.add(new DefaultElement(address, Element.TYPE_COMMENT));
        result.add(new DefaultElement(" ", Element.TYPE_PLAIN));
        result.add(new DefaultElement(label, Element.TYPE_IDENTIFIER));
        result.add(new DefaultElement(" ", Element.TYPE_PLAIN));

        long val;
        try {
            switch (size) {
                case 1:
                    result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI8(addr, step);
                    result.add(new DefaultElement("0x" + HexFormatter.tohex(val & 0xFF), Element.TYPE_NUMBER));
                    break;
                case 2:
                    result.add(new DefaultElement("DCW", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI16(addr, step);
                    result.add(new DefaultElement("0x" + HexFormatter.tohex(val & 0xFFFF), Element.TYPE_NUMBER));
                    break;
                case 4:
                    result.add(new DefaultElement("DCD", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI32(addr, step);
                    result.add(new DefaultElement("0x" + HexFormatter.tohex(val & 0xFFFFFFFFL), Element.TYPE_NUMBER));
                    break;
                case 8:
                    result.add(new DefaultElement("DCQ", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI64(addr, step);
                    result.add(new DefaultElement("0x" + HexFormatter.tohex(val), Element.TYPE_NUMBER));
                    break;
                default:
                    result.add(new DefaultElement("DC", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ??? ", Element.TYPE_PLAIN));
                    result.add(new DefaultElement("; unknown size " + size, Element.TYPE_COMMENT));
                    break;
            }
        } catch (MemoryNotMappedException e) {
            result.add(new DefaultElement("??? ", Element.TYPE_PLAIN));
            result.add(new DefaultElement("; memory not mapped", Element.TYPE_COMMENT));
        }

        return result;
    }

    @Override
    public List<Element> getElements() {
        if (elements == null) {
            elements = createElements();
        }
        return elements;
    }
}
