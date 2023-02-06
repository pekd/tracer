package org.graalvm.vm.trcview.ui.data;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.TraceCodeReader;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.DynamicTypePropagation;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Operand;
import org.graalvm.vm.trcview.disasm.Token;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.util.StringUtils;

public class CodeDataLine extends DataLine {
    private static final Type DEFAULT_TYPE_OCT = new Type(DataType.U8, Representation.OCT);
    private static final Type DEFAULT_TYPE_HEX = new Type(DataType.U8, Representation.DEC);

    // private final StepEvent event;
    private final AssemblerInstruction disasm;
    private final String comment;

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

        Disassembler disas = trc.getArchitecture().getDisassembler(trc);
        if (disas != null) {
            boolean be = trc.getArchitecture().getFormat().be;
            disasm = disas.disassemble(new TraceCodeReader(trc, addr + offset, be, step));
        } else {
            DynamicTypePropagation typeRecovery = trc.getTypeRecovery();
            StepEvent event = null;
            if (typeRecovery != null) {
                Set<StepEvent> steps = typeRecovery.getSemantics().getSteps(addr + offset);
                Iterator<StepEvent> i = steps.iterator();
                if (i.hasNext()) {
                    event = i.next();
                } else {
                    event = null;
                }
            } else {
                event = null;
            }

            if (event != null) {
                disasm = event.disassemble(trc);
            } else {
                disasm = null;
            }
        }

        String c = trc.getCommentForPC(addr + offset);
        if (c != null) {
            comment = "; " + c;
        } else {
            comment = null;
        }

        omitUnknownLabel = true;
    }

    private static void addToken(List<Element> result, String text, int type, int width, boolean comma, boolean pad) {
        result.add(new DefaultElement(text, type));
        int len = text.length();
        if (comma) {
            len++;
            result.add(new DefaultElement(",", Element.TYPE_PLAIN));
        }
        if (pad && len < width) {
            result.add(new DefaultElement(StringUtils.repeat(" ", width - len), Element.TYPE_PLAIN));
        }
    }

    private static void addToken(List<Element> result, Operand operand, int width, boolean comma, boolean pad) {
        int len = 0;
        for (Token token : operand.getTokens()) {
            String text = token.getText();
            int type = Element.TYPE_PLAIN;
            switch (token.getType()) {
                case NUMBER:
                    type = Element.TYPE_NUMBER;
                    break;
                case LABEL:
                    type = Element.TYPE_IDENTIFIER;
                    break;
                case ADDRESS:
                case OFFSET:
                    type = Element.TYPE_NUMBER;
                    break;
            }
            result.add(new DefaultElement(text, type));
            len += text.length();
        }
        if (comma) {
            len++;
            result.add(new DefaultElement(",", Element.TYPE_PLAIN));
        }
        if (pad && len < width) {
            result.add(new DefaultElement(StringUtils.repeat(" ", width - len), Element.TYPE_PLAIN));
        }
    }

    @Override
    protected void addData(List<Element> result) {
        if (disasm == null) {
            result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
            result.add(new DefaultElement("    ??? ", Element.TYPE_PLAIN));
            result.add(new DefaultElement("; code", Element.TYPE_COMMENT));
        } else {
            int start = 0;
            for (Element e : result) {
                start += e.getLength();
            }

            int tabSize = DataViewModel.TAB_SIZE;
            if (disasm == null) {
                result.add(new DefaultElement("<unreadable>", Element.TYPE_COMMENT));
            } else if (disasm.getOperands().length == 0) {
                result.add(new DefaultElement(disasm.getMnemonic(), Element.TYPE_KEYWORD));
            } else {
                addToken(result, disasm.getMnemonic(), Element.TYPE_KEYWORD, tabSize, false, true);
                Operand[] operands = disasm.getOperands();
                for (int i = 0; i < operands.length - 1; i++) {
                    addToken(result, operands[i], tabSize, true, true);
                }
                addToken(result, operands[operands.length - 1], tabSize, false, false);
            }

            int end = 0;
            for (Element e : result) {
                end += e.getLength();
            }

            int len = end - start;

            if (comment != null) {
                int padlen = DataViewModel.CODE_WIDTH - len;
                if (padlen < 1) {
                    padlen = 1;
                }
                String pad = StringUtils.repeat(" ", padlen);
                result.add(new DefaultElement(pad, Element.TYPE_PLAIN));
                result.add(new DefaultElement(comment, Element.TYPE_COMMENT));
            }
        }
    }
}
