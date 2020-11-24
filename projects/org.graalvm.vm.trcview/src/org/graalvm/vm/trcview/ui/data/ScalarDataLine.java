package org.graalvm.vm.trcview.ui.data;

import java.util.List;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.util.StringUtils;

public class ScalarDataLine extends DataLine {
    private static final Type DEFAULT_TYPE_OCT = new Type(DataType.U8, Representation.OCT);
    private static final Type DEFAULT_TYPE_HEX = new Type(DataType.U8, Representation.DEC);
    private static final int DEREF_STR_MAXLEN = 16;

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

    public ScalarDataLine(long addr, Type type, long step, TraceAnalyzer trc) {
        super(addr, getType(type, trc), step, trc);
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

    private void data(List<Element> result, long val) {
        String comment = null;
        if ((type.getType() == DataType.PTR && type.getRepresentation() == Representation.STRING) || (type.getType() == DataType.STRING)) {
            comment = "; " + DecoderUtils.cstr(trunc(val), step, trc);
        } else if (type.getType() == DataType.PTR) {
            // perform magic to decode the data
            // step 1: obtain type of address
            long ptr = trunc(val);
            TypedMemory mem = trc.getTypedMemory();
            Variable var = mem.get(ptr);
            if (var != null && var.getType() != null) {
                // there is type information
                Type dsttype = var.getType();
                if (dsttype.isStringData()) {
                    // decode as string with limited length
                    comment = "; " + DecoderUtils.cstr(ptr, step, trc, DEREF_STR_MAXLEN);
                } else {
                    // decode as value; if unknown type, we don't create a comment
                    try {
                        switch (dsttype.getType()) {
                            case U8:
                            case S8:
                                comment = "; " + DecoderUtils.str(dsttype, trc.getI8(ptr, step), trc);
                                break;
                            case U16:
                            case S16:
                                comment = "; " + DecoderUtils.str(dsttype, trc.getI16(ptr, step), trc);
                                break;
                            case U32:
                            case S32:
                            case F32:
                                comment = "; " + DecoderUtils.str(dsttype, trc.getI32(ptr, step), trc);
                                break;
                            case U64:
                            case S64:
                            case F64:
                                comment = "; " + DecoderUtils.str(dsttype, trc.getI64(ptr, step), trc);
                                break;
                        }
                    } catch (MemoryNotMappedException e) {
                        comment = "; cannot dereference memory";
                    }
                }
            }
        } else if (type.getType() == DataType.U8 || type.getType() == DataType.S8) {
            comment = "; '" + DecoderUtils.encode((int) val & 0xFF) + "'";
        }

        Element data;
        if (type.getType() == DataType.PTR) {
            long address = trunc(val);
            if (address == 0) {
                // special handling for NULL, even if 0 is a valid address
                data = new DefaultElement("NULL", Element.TYPE_NUMBER);
            } else {
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

    @Override
    protected void addData(List<Element> result) {
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
    }
}
