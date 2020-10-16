package org.graalvm.vm.trcview.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.trcview.ui.data.editor.Line;
import org.graalvm.vm.util.StringUtils;

public class DataLine extends Line {
    private static final Type DEFAULT_TYPE_OCT = new Type(DataType.U8);
    private static final Type DEFAULT_TYPE_HEX = new Type(DataType.U8);

    private final TraceAnalyzer trc;
    private final long step;
    private final long addr;
    private final Type type;
    private List<Element> elements;

    static {
        DEFAULT_TYPE_OCT.setRepresentation(Representation.OCT);
        DEFAULT_TYPE_HEX.setRepresentation(Representation.HEX);
    }

    public DataLine(long addr, Type type, long step, TraceAnalyzer trc) {
        this.addr = addr;
        if (type == null) {
            StepFormat fmt = trc.getArchitecture().getFormat();
            if (fmt.numberfmt == StepFormat.NUMBERFMT_OCT) {
                this.type = DEFAULT_TYPE_OCT;
            } else {
                this.type = DEFAULT_TYPE_HEX;
            }
        } else {
            this.type = type;
        }
        this.step = step;
        this.trc = trc;
    }

    private Element encode(long val, boolean isaddr) {
        String s = DecoderUtils.str(type, val, trc);
        switch (type.getRepresentation()) {
            case CHAR:
            case RAD50:
                return new DefaultElement(s, Element.TYPE_STRING);
            case FX16:
            case FX32:
            case FLOAT:
                // you cannot follow a float value
                return new DefaultElement(s, Element.TYPE_NUMBER);
            default:
                if (isaddr) {
                    return new AddressElement(s, Element.TYPE_NUMBER, val);
                } else {
                    return new NumberElement(s, Element.TYPE_NUMBER, val);
                }
        }
    }

    private long trunc(long val) {
        switch ((int) type.getSize()) {
            case 1:
                return val & 0xFF;
            case 2:
                return val & 0xFFFF;
            case 4:
                return val & 0xFFFFFFFFL;
            case 8:
            default:
                return val;
        }
    }

    private void data(List<Element> result, long val) {
        String comment = null;
        if ((type.getType() == DataType.PTR && type.getRepresentation() == Representation.STRING) || (type.getType() == DataType.STRING)) {
            comment = "; " + DecoderUtils.cstr(trunc(val), step, trc);
        } else if (type.getType() == DataType.U8 || type.getType() == DataType.S8) {
            comment = "; '" + DecoderUtils.encode((int) val & 0xFF) + "'";
        }

        Element data;
        if (type.getType() == DataType.PTR) {
            long address = trunc(val);
            Symbol sym = trc.getSymbol(address);
            if (sym != null && sym.getName() != null) {
                data = new AddressElement(sym.getName(), Element.TYPE_IDENTIFIER, address);
            } else {
                Variable var = trc.getTypedMemory().get(address);
                if (var != null && var.getAddress() == address) {
                    data = new AddressElement(var.getName(), Element.TYPE_IDENTIFIER, address);
                } else {
                    data = encode(val, true);
                }
            }
        } else {
            data = encode(val, false);
        }

        result.add(data);
        if (comment != null) {
            int len = data.getLength();
            int padlen = DataViewModel.DATA_WIDTH - len;
            if (padlen < 1) {
                padlen = 1;
            }
            String pad = StringUtils.repeat(" ", padlen);
            result.add(new DefaultElement(pad, Element.TYPE_PLAIN));
            result.add(new DefaultElement(comment, Element.TYPE_COMMENT));
        }
    }

    private List<Element> createElements() {
        StepFormat fmt = trc.getArchitecture().getFormat();

        List<Element> result = new ArrayList<>();
        String label = "";
        String address = fmt.formatAddress(addr);

        Symbol sym = trc.getSymbol(addr);
        if (sym != null && sym.getValue() == addr) {
            String name = sym.getName();
            if (name != null) {
                label = StringUtils.pad(name, DataViewModel.NAME_WIDTH);
            }
        } else {
            Variable var = trc.getTypedMemory().get(addr);
            if (var != null && var.getAddress() == addr) {
                String name = var.getName();
                label = StringUtils.pad(name, DataViewModel.NAME_WIDTH);
            }
        }

        result.add(new DefaultElement(address, Element.TYPE_COMMENT));
        result.add(new DefaultElement(" ", Element.TYPE_PLAIN));
        result.add(new DefaultElement(StringUtils.pad(label, DataViewModel.NAME_WIDTH), Element.TYPE_IDENTIFIER));
        result.add(new DefaultElement(" ", Element.TYPE_PLAIN));

        try {
            int size = (int) type.getSize();
            long val;
            switch (size) {
                case 1:
                    result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI8(addr, step);
                    data(result, val);
                    break;
                case 2:
                    result.add(new DefaultElement("DCW", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI16(addr, step);
                    data(result, val);
                    break;
                case 4:
                    result.add(new DefaultElement("DCD", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI32(addr, step);
                    data(result, val);
                    break;
                case 8:
                    result.add(new DefaultElement("DCQ", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    val = trc.getI64(addr, step);
                    data(result, val);
                    break;
                default:
                    result.add(new DefaultElement("DC?", Element.TYPE_KEYWORD));
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
