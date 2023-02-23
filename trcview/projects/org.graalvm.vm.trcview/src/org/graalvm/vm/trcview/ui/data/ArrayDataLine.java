package org.graalvm.vm.trcview.ui.data;

import java.util.List;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;

public class ArrayDataLine extends DataLine {
    public static final int MAX_LENGTH = 16;

    private final long start;
    private final long length;

    public ArrayDataLine(long addr, long start, Type type, long step, TraceAnalyzer trc) {
        this(addr, 0, null, -1, start, type, step, trc);
    }

    public ArrayDataLine(long addr, long offset, String name, long index, long start, Type type, long step, TraceAnalyzer trc) {
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

    private Element encode(long val, long ptr) {
        boolean isaddr = type.getType() == DataType.PTR;
        if (isaddr) {
            long address = trunc(val);
            if (address == 0) {
                // special handling for NULL, even if 0 is a valid address
                return new DataElement("NULL", Element.TYPE_NUMBER, ptr);
            } else {
                Symbol sym = trc.getSymbol(address);
                if (sym != null && sym.getName() != null) {
                    return new AddressElement(sym.getName(), Element.TYPE_IDENTIFIER, ptr, address);
                } else {
                    Variable var = trc.getTypedMemory().get(address);
                    if (var != null && var.getAddress() == address) {
                        return new AddressElement(var.getName(trc.getArchitecture().getFormat()), Element.TYPE_IDENTIFIER, ptr, address);
                    }
                }
            }
        }

        String s = DecoderUtils.str(type, val, trc);
        switch (type.getRepresentation()) {
            case CHAR:
            case RAD50:
                return new DataElement(s, Element.TYPE_STRING, ptr);
            case FX16:
            case FX32:
            case FLOAT:
                // you cannot follow a float value
                return new DataElement(s, Element.TYPE_NUMBER, ptr);
            default:
                if (isaddr) {
                    return new AddressElement(s, Element.TYPE_NUMBER, ptr, val);
                } else {
                    return new NumberElement(s, Element.TYPE_NUMBER, ptr, val);
                }
        }
    }

    @Override
    protected void addData(List<Element> result) {
        try {
            int size = (int) type.getElementSize();
            long ptr = addr + offset + start * type.getElementSize();
            boolean comma = false;
            switch (size) {
                case 1:
                    result.add(new DefaultElement("DCB", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    for (long i = 0; i < length; i++) {
                        long p = ptr + i * type.getElementSize();
                        byte val = trc.getI8(ptr + i * type.getElementSize(), step);
                        if (comma) {
                            result.add(new DefaultElement(", ", Element.TYPE_PLAIN));
                        }
                        result.add(encode(val, p));
                        comma = true;
                    }
                    break;
                case 2:
                    result.add(new DefaultElement("DCW", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    for (long i = 0; i < length; i++) {
                        long p = ptr + i * type.getElementSize();
                        short val = trc.getI16(p, step);
                        if (comma) {
                            result.add(new DefaultElement(", ", Element.TYPE_PLAIN));
                        }
                        result.add(encode(val, p));
                        comma = true;
                    }
                    break;
                case 4:
                    result.add(new DefaultElement("DCD", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    for (long i = 0; i < length; i++) {
                        long p = ptr + i * type.getElementSize();
                        int val = trc.getI32(ptr + i * type.getElementSize(), step);
                        if (comma) {
                            result.add(new DefaultElement(", ", Element.TYPE_PLAIN));
                        }
                        result.add(encode(val, p));
                        comma = true;
                    }
                    break;
                case 8:
                    result.add(new DefaultElement("DCQ", Element.TYPE_KEYWORD));
                    result.add(new DefaultElement("    ", Element.TYPE_PLAIN));
                    for (long i = 0; i < length; i++) {
                        long p = ptr + i * type.getElementSize();
                        long val = trc.getI64(ptr + i * type.getElementSize(), step);
                        if (comma) {
                            result.add(new DefaultElement(", ", Element.TYPE_PLAIN));
                        }
                        result.add(encode(val, p));
                        comma = true;
                    }
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
