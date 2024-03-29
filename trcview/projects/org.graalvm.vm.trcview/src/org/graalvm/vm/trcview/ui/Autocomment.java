package org.graalvm.vm.trcview.ui;

import java.util.List;
import java.util.logging.Logger;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Field;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.OctFormatter;
import org.graalvm.vm.util.Stringify;
import org.graalvm.vm.util.Vector128;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class Autocomment {
    private static final Logger log = Trace.create(Autocomment.class);

    private static long trunc(Type type, long val) {
        switch ((int) type.getElementSize()) {
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

    private static String data(TraceAnalyzer trc, Type type, long val) {
        if (type.getType() == DataType.PTR) {
            long address = trunc(type, val);
            if (address == 0) {
                // special handling for NULL, even if 0 is a valid address
                return "NULL";
            } else {
                Symbol sym = trc.getSymbol(address);
                if (sym != null && sym.getName() != null) {
                    if (sym.getValue() != address) {
                        long off = val - sym.getValue();
                        return sym.getName() + trc.getArchitecture().getFormat().formatOffset(off);
                    } else {
                        return sym.getName();
                    }
                } else {
                    Variable var = trc.getTypedMemory().get(address);
                    if (var != null && var.getAddress() == address) {
                        return var.getName(trc.getArchitecture().getFormat());
                    } else {
                        return DecoderUtils.str(type, val, trc);
                    }
                }
            }
        } else {
            return DecoderUtils.str(type, val, trc);
        }
    }

    private static String data(TraceAnalyzer trc, Type type, MemoryEvent access) {
        if (access.hasData() && access.getSize() <= 8) {
            return data(trc, type, access.getValue());
        } else if (access.hasData() && access.getSize() == 16) {
            Vector128 vec = access.getVector();
            return "0x" + vec.hex();
        } else {
            return "???";
        }
    }

    private static String data(MemoryEvent access, StepFormat fmt) {
        if (!access.hasData()) {
            return "???";
        } else if (fmt.numberfmt == StepFormat.NUMBERFMT_OCT) {
            String str;
            String val;
            if (access.getSize() <= 8 && access.getValue() == 0) {
                return "0";
            }
            switch (access.getSize()) {
                case 1:
                    str = Stringify.i8((byte) access.getValue());
                    val = OctFormatter.tooct(access.getValue(), 3);
                    break;
                case 2:
                    str = Stringify.i16((short) access.getValue());
                    val = OctFormatter.tooct(access.getValue(), 6);
                    break;
                case 4:
                    str = Stringify.i32((int) access.getValue());
                    val = OctFormatter.tooct(access.getValue(), 11);
                    break;
                case 8:
                    str = Stringify.i64(access.getValue());
                    val = OctFormatter.tooct(access.getValue(), 22);
                    break;
                case 16:
                    str = Stringify.i128(access.getVector());
                    val = OctFormatter.tooct(access.getVector().getI64(0), 22) + " " + OctFormatter.tooct(access.getVector().getI64(1), 22);
                    break;
                default:
                    return "???";
            }
            if (str != null) {
                return "0" + val + " # \'" + str + "\'";
            } else {
                return "0" + val;
            }
        } else {
            String str;
            String val;
            if (access.getSize() <= 8 && access.getValue() == 0) {
                return "0";
            }
            switch (access.getSize()) {
                case 1:
                    str = Stringify.i8((byte) access.getValue());
                    val = HexFormatter.tohex(access.getValue(), 2);
                    break;
                case 2:
                    str = Stringify.i16((short) access.getValue());
                    val = HexFormatter.tohex(access.getValue(), 4);
                    break;
                case 4:
                    str = Stringify.i32((int) access.getValue());
                    val = HexFormatter.tohex(access.getValue(), 8);
                    break;
                case 8:
                    str = Stringify.i64(access.getValue());
                    val = HexFormatter.tohex(access.getValue(), 16);
                    break;
                case 16:
                    str = Stringify.i128(access.getVector());
                    val = HexFormatter.tohex(access.getVector().getI64(0), 16) + " " + HexFormatter.tohex(access.getVector().getI64(1), 16);
                    break;
                default:
                    return "???";
            }
            if (str != null) {
                return "0x" + val + " # \'" + str + "\'";
            } else {
                return "0x" + val;
            }
        }
    }

    private static String decode(TraceAnalyzer trc, Type type, long off, MemoryEvent access) {
        long offset = off;
        String prefix = "";

        if (type.getElements() > 1) {
            long idx = off / type.getElementSize();
            offset = off % type.getElementSize();
            prefix = "[" + idx + "]";
        }

        if (type.getType() == DataType.STRUCT) {
            Struct struct = type.getStruct();
            if (struct == null) {
                log.log(Levels.ERROR, "Type is " + type.getType() + " but struct is " + struct);
                return prefix + " = ??? (struct error)";
            }
            for (Field field : struct.getFields()) {
                if (field.getOffset() <= offset && field.end() > offset) {
                    // found it
                    return prefix + "." + field.getName() + decode(trc, field.getType(), offset - field.getOffset(), access);
                }
            }
            log.log(Levels.ERROR, "Field not found at offset " + offset + " in struct " + struct);
            return prefix + " = ??? (field not found)";
        } else {
            if (offset != 0) {
                return prefix + " (+" + offset + ") = " + data(trc, type, access);
            } else {
                return prefix + " = " + data(trc, type, access);
            }
        }
    }

    private static String decode(TraceAnalyzer trc, MemoryEvent access, Variable var, StepFormat fmt) {
        Type type = var.getType();
        String name = var.getName(fmt);

        // no explicit variable name?
        if (var.getRawName() == null) {
            // try to use symbol name if available
            Symbol sym = trc.getSymbol(var.getAddress());
            if (sym != null && sym.getValue() == var.getAddress()) {
                name = sym.getName();
            }
        }

        if (name == null || name.length() == 0) {
            return null;
        }

        if (type == null && name != null && var.getAddress() == access.getAddress()) {
            return name + " = " + data(access, fmt);
        } else if (name != null && type != null) {
            long offset = access.getAddress() - var.getAddress();
            if (type.getElements() > 1) {
                // is an array
                long idx = offset / type.getElementSize();
                long off = offset % type.getElementSize();
                if (type.getType() == DataType.STRUCT) {
                    // we got a struct
                    return name + "[" + idx + "]" + decode(trc, type.getElementType(), off, access);
                } else {
                    return name + "[" + idx + "] = " + data(trc, type, access);
                }
            } else if (type.getType() == DataType.STRUCT) {
                return name + decode(trc, type, offset, access);
            } else {
                return name + " = " + data(trc, type, access);
            }
        } else {
            return null;
        }
    }

    private static String decode(TraceAnalyzer trc, MemoryEvent access, TypedMemory mem, StepFormat fmt) {
        long addr = access.getAddress();
        Variable var = mem.get(addr);
        if (var != null) {
            return decode(trc, access, var, fmt);
        } else {
            Symbol sym = trc.getSymbol(addr);
            if (sym != null && sym.getValue() == addr && sym.getName().length() > 0) {
                return sym.getName() + " = " + data(access, fmt);
            }
        }

        return null;
    }

    public static String get(TraceAnalyzer trc, StepEvent step) {
        TypedMemory mem = trc.getTypedMemory();

        List<MemoryEvent> reads = step.getDataReads();
        List<MemoryEvent> writes = step.getDataWrites();

        StepFormat fmt = step.getFormat();

        if (!reads.isEmpty()) {
            for (MemoryEvent read : reads) {
                // process
                String result = decode(trc, read, mem, fmt);
                if (result != null) {
                    return result;
                }
            }
        }

        if (!writes.isEmpty()) {
            for (MemoryEvent write : writes) {
                // process
                String result = decode(trc, write, mem, fmt);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
