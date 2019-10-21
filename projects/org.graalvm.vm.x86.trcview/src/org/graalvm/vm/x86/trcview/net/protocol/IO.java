package org.graalvm.vm.x86.trcview.net.protocol;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;
import org.graalvm.vm.x86.node.debug.trace.CpuStateRecord;
import org.graalvm.vm.x86.node.debug.trace.FullCpuStateRecord;
import org.graalvm.vm.x86.node.debug.trace.Record;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.expression.TypeParser;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.io.RecordNode;

public class IO {
    public static final String readString(WordInputStream in) throws IOException {
        int length = in.read16bit() & 0xFFFF;
        if (length == 0xFFFF) {
            return null;
        } else if (length == 0) {
            return "";
        } else {
            byte[] bytes = new byte[length];
            in.read(bytes);
            return new String(bytes);
        }
    }

    public static final void writeString(WordOutputStream out, String s) throws IOException {
        if (s == null) {
            out.write16bit((short) -1);
        } else if (s.length() == 0) {
            out.write16bit((short) 0);
        } else {
            byte[] bytes = s.getBytes();
            out.write16bit((short) bytes.length);
            out.write(bytes);
        }
    }

    public static final String[] readStringArray(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            String[] result = new String[length];
            for (int i = 0; i < result.length; i++) {
                int slen = in.read16bit();
                if (slen == 0) {
                    result[i] = null;
                } else {
                    byte[] bytes = new byte[slen];
                    in.read(bytes);
                    result[i] = new String(bytes);
                }
            }
            return result;
        }
    }

    public static final void writeStringArray(WordOutputStream out, String[] data) throws IOException {
        if (data == null) {
            out.write16bit((short) 0);
        } else {
            out.write16bit((short) data.length);
            for (int i = 0; i < data.length; i++) {
                writeString(out, data[i]);
            }
        }
    }

    public static final byte[] readArray(WordInputStream in) throws IOException {
        int length = in.read32bit();
        if (length == 0) {
            return null;
        } else {
            byte[] data = new byte[length];
            in.read(data);
            return data;
        }
    }

    public static final void writeArray(WordOutputStream out, byte[] data) throws IOException {
        if (data == null) {
            out.write32bit(0);
        } else {
            out.write32bit(data.length);
            out.write(data);
        }
    }

    public static final byte[] readShortArray(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            byte[] data = new byte[length];
            in.read(data);
            return data;
        }
    }

    public static final void writeShortArray(WordOutputStream out, byte[] data) throws IOException {
        if (data == null) {
            out.write16bit((short) 0);
        } else {
            out.write16bit((short) data.length);
            out.write(data);
        }
    }

    public static final byte[][] readShortArray2(WordInputStream in) throws IOException {
        int length = in.read16bit();
        if (length == 0) {
            return null;
        } else {
            byte[][] data = new byte[length][];
            for (int i = 0; i < data.length; i++) {
                data[i] = new byte[in.read16bit()];
                in.read(data[i]);
            }
            return data;
        }
    }

    public static final void writeShortArray2(WordOutputStream out, byte[][] data) throws IOException {
        if (data == null) {
            out.write16bit((short) 0);
        } else {
            out.write16bit((short) data.length);
            for (int i = 0; i < data.length; i++) {
                out.write16bit((short) data[i].length);
                out.write(data[i]);
            }
        }
    }

    public static final Prototype readPrototype(WordInputStream in) throws IOException {
        if (in.read() == 0) {
            return null;
        }
        String rettype = IO.readString(in);
        String args = IO.readString(in);
        try {
            Function fun = new TypeParser(rettype + " f(" + args + ")").parse();
            return fun.getPrototype();
        } catch (ParseException e) {
            throw new IOException("Invalid prototype: " + e.getMessage() + " [prototype = '" + rettype + " f(" + args + ")']", e);
        }
    }

    public static final void writePrototype(WordOutputStream out, Prototype proto) throws IOException {
        if (proto == null) {
            out.write(0);
        } else {
            out.write(1);
            writeString(out, proto.returnType.toString());
            writeString(out, proto.args.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
    }

    public static final ComputedSymbol readComputedSymbol(WordInputStream in) throws IOException {
        int symtype = in.read();
        if (symtype == 0) {
            return null;
        }
        String name = readString(in);
        long address = in.read64bit();
        ComputedSymbol.Type type = in.read() == 1 ? ComputedSymbol.Type.SUBROUTINE : ComputedSymbol.Type.LOCATION;
        ComputedSymbol sym = new ComputedSymbol(name, address, type);
        int visits = in.read32bit();
        for (int i = 0; i < visits; i++) {
            sym.addVisit(readNode(in));
        }
        if (in.read() == 1) {
            Prototype proto = readPrototype(in);
            sym.prototype = proto;
        }
        return sym;
    }

    public static final void writeComputedSymbol(WordOutputStream out, ComputedSymbol sym) throws IOException {
        if (sym == null) {
            out.write(0);
            return;
        }
        out.write(1);
        IO.writeString(out, sym.name);
        out.write64bit(sym.address);
        out.write(sym.type == ComputedSymbol.Type.SUBROUTINE ? 1 : 0);
        out.write32bit(sym.visits.size());
        for (Node visit : sym.visits) {
            writeNode(out, visit);
        }
        out.write(sym.prototype != null ? 1 : 0);
        if (sym.prototype != null) {
            IO.writePrototype(out, sym.prototype);
        }
    }

    private static final StepRecord readStep(WordInputStream in) throws IOException {
        byte[] machinecode = readArray(in);
        CpuStateRecord state = Record.read(in, null);
        return new StepRecord(machinecode, state);
    }

    private static final void writeStep(WordOutputStream out, StepRecord step) throws IOException {
        writeArray(out, step.getMachinecode());
        CpuStateRecord state = new FullCpuStateRecord(step.getState().getState());
        state.write(out);
    }

    public static final Node readNode(WordInputStream in) throws IOException {
        if (in.read() == 0) {
            return null;
        }

        long id = in.read64bit();
        long parent = in.read64bit();
        BlockNode parentNode;
        if (parent == -1) {
            parentNode = null;
        } else if (parent == -2) {
            parentNode = new FakeBlockNode(-1);
        } else {
            parentNode = new FakeBlockNode(parent);
        }
        int type = in.read();
        Node node;
        switch (type) {
            case 0:
                node = new RecordNode(Record.read(in, null));
                node.setParent(parentNode);
                node.setId(id);
                return node;
            case 1:
                node = new RecordNode(readStep(in));
                node.setParent(parentNode);
                node.setId(id);
                return node;
            case 2: {
                StepRecord head = null;
                if (in.read() == 0) {
                    // no head
                } else {
                    head = readStep(in);
                }
                in.read32bit(); // node count
                StepRecord first = readStep(in);
                node = new BlockNode(head, Arrays.asList(new RecordNode(first)));
                node.setParent(parentNode);
                node.setId(id);
                return node;
            }
        }
        return null;
    }

    public static final void writeNode(WordOutputStream out, Node node) throws IOException {
        if (node == null) {
            out.write(0);
            return;
        } else {
            out.write(1);
        }

        out.write64bit(node.getId());
        BlockNode parent = node.getParent();
        if (parent == null) {
            out.write64bit(-1);
        } else if (parent.getHead() == null) {
            out.write64bit(-2);
        } else {
            out.write64bit(parent.getHead().getInstructionCount());
        }

        if (node instanceof RecordNode) {
            RecordNode n = (RecordNode) node;
            Record r = n.getRecord();
            if (r instanceof StepRecord) {
                out.write(1);
                writeStep(out, (StepRecord) r);
            } else {
                out.write(0);
                r.write(out);
            }
        } else {
            BlockNode n = (BlockNode) node;
            out.write(2);
            if (n.getHead() != null) {
                out.write(1);
                writeStep(out, n.getHead());
            } else {
                out.write(0);
            }
            out.write32bit(n.getNodes().size());
            writeStep(out, n.getFirstStep());
        }
    }

    public static class FakeBlockNode extends BlockNode {
        private final long insn;

        public FakeBlockNode(long insn) {
            super(null);
            this.insn = insn;
        }

        public long getInstructionCount() {
            return insn;
        }
    }
}
