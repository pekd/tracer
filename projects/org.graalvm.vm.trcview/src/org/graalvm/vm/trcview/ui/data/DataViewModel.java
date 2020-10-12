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
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.DefaultLine;
import org.graalvm.vm.trcview.ui.data.editor.EditorModel;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.trcview.ui.data.editor.Line;
import org.graalvm.vm.trcview.ui.event.ChangeListener;

public class DataViewModel extends EditorModel implements ChangeListener, SymbolRenameListener {
    public static final int NAME_WIDTH = 40;
    private static final Element HEADER_SEPARATOR = new DefaultElement("================================================================================", Element.TYPE_COMMENT);

    private TraceAnalyzer trc;
    private long step;

    private int maxlen = 80;

    private List<MemorySegment> segments = Collections.emptyList();
    private long lineCount;

    private NavigableMap<Long, TextSegment> memorySegments = new TreeMap<>();
    private NavigableMap<Long, TextSegment> lineSegments = new TreeMap<>();

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
            TextSegment s = new TextSegment(seg, line);
            memorySegments.put(s.address, s);
            lineSegments.put(s.firstLine, s);
            line += seg.getSize() + s.metainfo;
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
            throw new IllegalArgumentException("unknown line");
        } else {
            return s.getValue().getLineByAddress(addr);
        }
    }

    @Override
    public Line getLine(int line) {
        Entry<Long, TextSegment> tse = lineSegments.floorEntry((long) line);
        if (tse == null) {
            return null;
        }

        TextSegment ts = tse.getValue();

        Line l = ts.getLine(line);
        if (l != null) {
            int len = l.getLength();
            if (len > maxlen) {
                maxlen = len;
            }
        }

        return l;
    }

    @Override
    public int getMaximumLineLength() {
        return maxlen;
    }

    private class TextSegment {
        public final long address;
        public long firstLine;
        public long content;

        public final int metainfo;

        public final List<Line> header;
        public final List<Line> footer;

        public TextSegment(MemorySegment segment, long line) {
            this.address = segment.getStart();
            this.firstLine = line;

            content = segment.getSize();

            StepFormat fmt = trc.getArchitecture().getFormat();

            Element addrelement = new DefaultElement(fmt.formatAddress(segment.getStart()) + " ", Element.TYPE_COMMENT);
            header = new ArrayList<>();
            header.add(new DefaultLine(addrelement, HEADER_SEPARATOR));
            header.add(new DefaultLine(addrelement, new DefaultElement("SEGMENT [name=" + segment.getName() + ", protection=" + segment.getProtection() + "]", Element.TYPE_COMMENT)));
            header.add(new DefaultLine(addrelement, HEADER_SEPARATOR));

            Element endaddrelement = new DefaultElement(fmt.formatAddress(segment.getEnd()) + " ", Element.TYPE_COMMENT);
            footer = new ArrayList<>();
            footer.add(new DefaultLine(endaddrelement));

            metainfo = header.size() + footer.size();
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
            return new DataLine(addr, 1, step, trc);
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
                return address + delta - header.size();
            }
        }

        public long getLineByAddress(long addr) {
            long delta = addr - address;
            return firstLine + header.size() + delta;
        }
    }
}
