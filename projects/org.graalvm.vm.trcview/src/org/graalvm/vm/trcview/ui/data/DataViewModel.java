package org.graalvm.vm.trcview.ui.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.SymbolRenameListener;
import org.graalvm.vm.trcview.analysis.memory.MemorySegment;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Field;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.DefaultLine;
import org.graalvm.vm.trcview.ui.data.editor.EditorModel;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.trcview.ui.data.editor.Line;
import org.graalvm.vm.trcview.ui.event.ChangeListener;

public class DataViewModel extends EditorModel implements ChangeListener, SymbolRenameListener {
    public static final int NAME_WIDTH = 40;
    public static final int DATA_WIDTH = 32;
    private static final Element HEADER_SEPARATOR = new DefaultElement("================================================================================", Element.TYPE_COMMENT);

    private TraceAnalyzer trc;
    private long step;

    private int maxlen = 80;
    private Line maxline = null;

    private List<MemorySegment> segments = Collections.emptyList();
    private long lineCount;

    private NavigableMap<Long, TextSegment> memorySegments = new TreeMap<>(); // addr to segment
    private NavigableMap<Long, TextSegment> lineSegments = new TreeMap<>(); // line to segment

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        if (this.trc != null) {
            this.trc.removeCommentChangeListener(this);
            this.trc.removeSymbolChangeListener(this);
            this.trc.removeSymbolRenameListener(this);
        }

        this.trc = trc;
        trc.addCommentChangeListener(this);
        trc.addSymbolChangeListener(this);
        trc.addSymbolRenameListener(this);

        fireChangeEvent();
    }

    @Override
    public void valueChanged() {
        fireChangeEvent();
    }

    @Override
    public void symbolRenamed(ComputedSymbol sym) {
        fireChangeEvent();
    }

    public void setStep(long step) {
        this.step = step;
        segments = trc.getMemorySegments(step);
        memorySegments.clear();
        lineSegments.clear();
        long line = 0;
        for (MemorySegment seg : segments) {
            TextSegment s = new TextSegment(seg, line, trc.getTypedMemory());
            memorySegments.put(s.address, s);
            lineSegments.put(s.firstLine, s);
            line += s.getLineCount();
        }
        lineCount = line;
        fireChangeEvent();
    }

    public void update() {
        long line = 0;
        lineSegments.clear();
        for (TextSegment s : memorySegments.values()) {
            s.recompute(line);
            lineSegments.put(s.firstLine, s);
            line += s.getLineCount();
        }
        lineCount = line;
        fireChangeEvent();
    }

    public long getBytes() {
        long result = 0;
        for (MemorySegment seg : segments) {
            result += seg.getSize();
        }
        return result;
    }

    @Override
    public int getLineCount() {
        return (int) lineCount;
    }

    public long getAddressByLine(long line) {
        Entry<Long, TextSegment> s = lineSegments.floorEntry(line);
        if (s == null) {
            throw new IllegalArgumentException("unknown line");
        } else {
            return s.getValue().getAddress(line);
        }
    }

    public long getLineByAddress(long addr) {
        Entry<Long, TextSegment> s = memorySegments.floorEntry(addr);
        if (s == null) {
            throw new IllegalArgumentException("unknown address");
        } else if (!s.getValue().contains(addr)) {
            throw new IllegalArgumentException("unknown address");
        } else {
            return s.getValue().getLineByAddress(addr);
        }
    }

    @Override
    public Line getLine(int line) {
        if (line > getLineCount()) {
            return null;
        }

        Entry<Long, TextSegment> tse = lineSegments.floorEntry((long) line);
        if (tse == null) {
            return null;
        }

        TextSegment ts = tse.getValue();

        Line l = ts.getLine(line);
        if (l != null) {
            int len = l.getLength();
            if (len > maxlen) {
                maxline = l;
                maxlen = len;
            }
        }

        return l;
    }

    @Override
    public int getMaximumLineLength() {
        return maxlen;
    }

    @Override
    public Line getLongestLine() {
        return maxline;
    }

    private class Var {
        public final Variable var;
        public final Field field;
        public final String parent;
        public final long offset;
        public final long delta;
        public long line;

        public Var(Variable var, long line) {
            this.var = var;
            this.line = line;
            this.parent = null;
            this.field = null;
            this.offset = 0;
            this.delta = 0;
        }

        public Var(Variable var, long line, String parent, Field field, long offset) {
            this.var = var;
            this.line = line;
            this.parent = parent;
            this.field = field;
            this.offset = offset;
            this.delta = 0;
        }

        public Var(Variable var, long line, String parent, Field field, long offset, long delta) {
            this.var = var;
            this.line = line;
            this.parent = parent;
            this.field = field;
            this.offset = offset;
            this.delta = delta;
        }

        public boolean contains(long ln) {
            return line == ln;
        }
    }

    private class TextSegment {
        public final long address;
        public long firstLine;
        public long content;

        public final MemorySegment segment;
        public final TypedMemory mem;

        public final int metainfo;

        public final List<Line> header;
        public final List<Line> footer;

        public final NavigableMap<Long, Var> vars; // address to var
        public final NavigableMap<Long, Var> invvars; // line to var

        public TextSegment(MemorySegment segment, long line, TypedMemory mem) {
            this.address = segment.getStart();
            this.firstLine = line;
            this.segment = segment;
            this.mem = mem;

            content = segment.getSize();

            StepFormat fmt = trc.getArchitecture().getFormat();

            String name = segment.getName() == null ? "" : " \"" + segment.getName() + "\"";

            Element addrelement = new DefaultElement(fmt.formatAddress(segment.getStart()) + " ", Element.TYPE_COMMENT);
            header = new ArrayList<>();
            header.add(new DefaultLine(addrelement, HEADER_SEPARATOR));
            header.add(new DefaultLine(addrelement, new DefaultElement("SEGMENT" + name + " protection=\"" + segment.getProtection() + "\"", Element.TYPE_COMMENT)));
            header.add(new DefaultLine(addrelement, HEADER_SEPARATOR));

            Element endaddrelement = new DefaultElement(fmt.formatAddress(segment.getEnd()) + " ", Element.TYPE_COMMENT);
            footer = new ArrayList<>();
            footer.add(new DefaultLine(endaddrelement, new DefaultElement("END OF SEGMENT" + name, Element.TYPE_COMMENT)));
            footer.add(new DefaultLine(endaddrelement));

            metainfo = header.size() + footer.size();

            vars = new TreeMap<>();
            invvars = new TreeMap<>();
            recompute(line);
        }

        private long add(long line, Variable var, Struct struct) {
            return add(line, var, struct, null, 0);
        }

        private long add(long line, Variable var, Struct struct, String parent, long offset) {
            List<Field> fields = new ArrayList<>(struct.getFields());
            Collections.sort(fields, (a, b) -> Long.compareUnsigned(a.getOffset(), b.getOffset()));
            long ptr = var.getAddress();
            long ln = line;
            for (Field field : fields) {
                Type type = field.getType();
                if (type.getType() == DataType.STRUCT) {
                    String p;
                    if (parent == null) {
                        p = field.getName();
                    } else {
                        p = parent + "." + field.getName();
                    }
                    if (type.getElements() > 1) {
                        for (long i = 0; i < type.getElements(); i++) {
                            String n = p + "[" + i + "]";
                            ln = add(ln, var, type.getStruct(), n, offset + field.getOffset() + i * type.getElementSize());
                        }
                    } else {
                        ln = add(ln, var, type.getStruct(), p, offset + field.getOffset());
                    }
                    ptr += field.getSize();
                } else {
                    if (type.getElements() > 1) {
                        long rem = type.getSize() / type.getElementSize();
                        long lineSize = type.isStringData() ? StringDataLine.MAX_LENGTH : ArrayDataLine.MAX_LENGTH;
                        long delta = 0;
                        long idx = 0;
                        while (rem > 0) {
                            Var v = new Var(var, ln, parent, field, offset + field.getOffset(), idx);
                            assert delta == idx * type.getElementSize();
                            assert v.var.getAddress() + v.offset + v.delta * type.getElementSize() == ptr + offset + delta;
                            vars.put(ptr + offset + delta, v);
                            invvars.put(ln, v);
                            if (rem > lineSize) {
                                rem -= lineSize;
                                idx += lineSize;
                                delta += lineSize * type.getElementSize();
                                ln++;
                            } else {
                                idx += rem;
                                delta += rem * type.getElementSize();
                                rem = 0;
                            }
                        }
                    } else {
                        Var v = new Var(var, ln, parent, field, offset + field.getOffset());
                        vars.put(ptr + offset, v);
                        invvars.put(ln, v);
                    }
                    ptr += field.getSize();
                    ln++;
                }
            }
            return ln;
        }

        public void recompute(long line) {
            firstLine = line;

            vars.clear();
            invvars.clear();

            long lastAddr = address;
            long lastLine = line + header.size();
            for (Variable var : mem.getTypes(segment.getStart(), segment.getEnd())) {
                long off = var.getAddress() - lastAddr;
                long ln = lastLine + off;
                Type type = var.getType();
                if (type != null && type.getType() == DataType.STRUCT) {
                    Struct struct = type.getStruct();
                    lastLine = add(ln, var, struct);
                } else if (type != null && type.getElements() > 1) {
                    long ptr = var.getAddress();
                    long rem = var.getSize() / type.getElementSize();
                    long lineSize = type.isStringData() ? StringDataLine.MAX_LENGTH : ArrayDataLine.MAX_LENGTH;
                    while (rem > 0) {
                        Var v = new Var(var, ln);
                        vars.put(ptr, v);
                        invvars.put(ln, v);
                        if (rem > lineSize) {
                            rem -= lineSize;
                            ptr += lineSize * type.getElementSize();
                        } else {
                            ptr += rem * type.getElementSize();
                            rem = 0;
                        }
                        ln++;
                    }
                    lastLine = ln;
                } else {
                    Var v = new Var(var, ln);
                    vars.put(var.getAddress(), v);
                    invvars.put(ln, v);
                    lastLine = ln + 1;
                }
                lastAddr = var.getAddress() + var.getSize();
            }

            content = getLineByAddress(segment.getEnd()) - getLineByAddress(segment.getStart()) + 1;
        }

        public long getLineCount() {
            return metainfo + content;
        }

        public boolean isHeader(long line) {
            return line - firstLine < header.size();
        }

        public Line getHeader(long line) {
            return header.get((int) (line - firstLine));
        }

        public boolean isFooter(long line) {
            return line - firstLine - header.size() >= content;
        }

        public Line getFooter(long line) {
            return footer.get((int) (line - firstLine - header.size() - content));
        }

        public Line getData(long line) {
            long addr = getAddress(line);
            Var var = vars.get(addr);
            if (var != null) {
                Type type = var.var.getType();
                if (type != null && type.getType() == DataType.STRUCT) {
                    Var v = invvars.get(line);
                    String name;
                    if (v.parent != null) {
                        name = v.parent + "." + v.field.getName();
                    } else {
                        name = v.field.getName();
                    }
                    if (v.field.getType().getElements() > 1) {
                        if (v.field.getType().isStringData()) {
                            return new StringDataLine(addr, v.offset, name, v.delta, v.field.getType(), step, trc);
                        } else {
                            return new ArrayDataLine(addr, v.offset, name, v.delta, v.field.getType(), step, trc);
                        }
                    } else {
                        return new ScalarDataLine(addr, v.offset, name, v.field.getType(), step, trc);
                    }
                } else if (type != null && type.getElements() > 1) {
                    if (type.isStringData()) {
                        long delta = line - var.line;
                        long offset = delta * StringDataLine.MAX_LENGTH;
                        return new StringDataLine(addr, offset, type, step, trc);
                    } else {
                        long delta = line - var.line;
                        long offset = delta * ArrayDataLine.MAX_LENGTH;
                        return new ArrayDataLine(addr, offset, type, step, trc);
                    }
                } else {
                    return new ScalarDataLine(addr, type, step, trc);
                }
            } else {
                return new ScalarDataLine(addr, null, step, trc);
            }
        }

        public Line getLine(long line) {
            if (isHeader(line)) {
                return getHeader(line);
            } else if (isFooter(line)) {
                return getFooter(line);
            } else {
                return getData(line);
            }
        }

        public long getAddress(long line) {
            long delta = line - firstLine;
            if (delta < header.size()) {
                return address;
            } else {
                Entry<Long, Var> var = invvars.floorEntry(line);
                if (var == null) {
                    return address + delta - header.size();
                } else if (var.getValue().contains(line)) {
                    return var.getValue().var.getAddress();
                } else {
                    Var v = var.getValue();
                    delta = line - v.line;
                    return v.var.getAddress() + v.var.getSize() - 1 + delta;
                }
            }
        }

        public long getLineByAddress(long addr) {
            Entry<Long, Var> var = vars.floorEntry(addr);
            if (var == null) {
                long delta = addr - address;
                return firstLine + header.size() + delta;
            } else if (var.getValue().var.contains(addr)) {
                return var.getValue().line;
            } else {
                Var v = var.getValue();
                long off = addr - (v.var.getAddress() + v.var.getSize()) + 1;
                return v.line + off;
            }
        }

        public boolean contains(long addr) {
            return addr >= segment.getStart() && addr <= segment.getEnd();
        }
    }
}
