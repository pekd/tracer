package org.graalvm.vm.trcview.ui.data.editor;

import java.util.ArrayList;
import java.util.List;

public class DefaultEditorModel extends EditorModel {
    private List<Line> lines = new ArrayList<>();

    public void addLine(Line line) {
        lines.add(line);
    }

    public void removeLine(int line) {
        lines.remove(line);
    }

    @Override
    public int getLineCount() {
        return lines.size();
    }

    @Override
    public int getMaximumLineLength() {
        return 80;
    }

    @Override
    public Line getLongestLine() {
        return null;
    }

    @Override
    public Line getLine(int line) {
        if (line >= getLineCount()) {
            return null;
        }
        return lines.get(line);
    }
}
