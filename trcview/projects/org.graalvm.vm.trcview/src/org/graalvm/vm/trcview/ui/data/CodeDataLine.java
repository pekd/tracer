package org.graalvm.vm.trcview.ui.data;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.DynamicTypePropagation;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.util.StringUtils;

public class CodeDataLine extends DataLine {
    private static final Type DEFAULT_TYPE_OCT = new Type(DataType.U8, Representation.OCT);
    private static final Type DEFAULT_TYPE_HEX = new Type(DataType.U8, Representation.DEC);

    private final StepEvent event;

    private static Type getType(Type type, TraceAnalyzer trc) {
        if (type == null) {
            StepFormat fmt = trc.getArchitecture().getFormat();
            if (fmt.numberfmt == StepFormat.NUMBERFMT_OCT) {
                return DEFAULT_TYPE_OCT;
            } else {
                return DEFAULT_TYPE_HEX;
            }
        } else {
            return type;
        }
    }

    public CodeDataLine(long addr, Type type, long step, TraceAnalyzer trc) {
        this(addr, 0, null, -1, type, step, trc);
    }

    public CodeDataLine(long addr, long offset, String name, long index, Type type, long step, TraceAnalyzer trc) {
        super(addr, offset, name, index, getType(type, trc), step, trc);

        DynamicTypePropagation typeRecovery = trc.getTypeRecovery();
        if (typeRecovery != null) {
            Set<StepEvent> steps = typeRecovery.getSemantics().getSteps(addr);
            Iterator<StepEvent> i = steps.iterator();
            if (i.hasNext()) {
                event = i.next();
            } else {
                event = null;
            }
        } else {
            event = null;
        }

        omitUnknownLabel = true;
    }

    private static void addToken(List<Element> result, String text, int type, int width, boolean comma) {
        result.add(new DefaultElement(text, type));
        int len = text.length();
        if (comma) {
            len++;
            result.add(new DefaultElement(",", Element.TYPE_PLAIN));
        }
        if (len < width) {
            result.add(new DefaultElement(StringUtils.repeat(" ", width - len), Element.TYPE_PLAIN));
        }
    }

    @Override
    protected void addData(List<Element> result) {
        if (event == null) {
            result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
            result.add(new DefaultElement("    ??? ", Element.TYPE_PLAIN));
            result.add(new DefaultElement("; code", Element.TYPE_COMMENT));
        } else {
            int tabSize = 7;
            String[] disasm = event.getDisassemblyComponents();
            if (disasm == null) {
                result.add(new DefaultElement("<unreadable>", Element.TYPE_COMMENT));
            } else if (disasm.length == 1) {
                result.add(new DefaultElement(disasm[0], Element.TYPE_KEYWORD));
            } else {
                addToken(result, disasm[0], Element.TYPE_KEYWORD, tabSize, false);
                for (int i = 1; i < disasm.length - 1; i++) {
                    addToken(result, disasm[i], Element.TYPE_PLAIN, tabSize, true);
                }
                result.add(new DefaultElement(disasm[disasm.length - 1], Element.TYPE_PLAIN));
            }
        }
    }
}
