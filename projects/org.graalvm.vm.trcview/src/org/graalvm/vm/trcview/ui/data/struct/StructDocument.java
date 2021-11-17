package org.graalvm.vm.trcview.ui.data.struct;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

import org.graalvm.vm.trcview.analysis.type.Struct;

@SuppressWarnings("serial")
public class StructDocument extends DefaultStyledDocument {
    private final Style text;
    private final Style comment;
    private final Style number;
    private final Style string;
    private final Style keyword;
    private final Style identifier;

    private final int initialCaret;

    private List<StructArea> structs = new ArrayList<>();

    private final static int COLUMN_DATATYPE = 40;

    private static class StructArea {
        private Struct struct;
        private int len;

        public void computeLength() {
            int length = 0;
            len = length;
        }

        public int getLength() {
            return len;
        }
    }

    public StructDocument() {
        text = addStyle("text", null);
        StyleConstants.setFontFamily(text, Font.MONOSPACED);
        StyleConstants.setFontSize(text, 11);
        StyleConstants.setForeground(text, Color.BLACK);

        comment = addStyle("comment", text);
        StyleConstants.setForeground(comment, Color.GRAY);

        number = addStyle("number", text);
        StyleConstants.setForeground(number, Color.RED);

        string = addStyle("string", text);
        StyleConstants.setForeground(string, Color.DARK_GRAY);

        keyword = addStyle("keyword", text);
        StyleConstants.setForeground(keyword, Color.BLUE);
        StyleConstants.setBold(keyword, true);

        identifier = addStyle("identifier", text);
        StyleConstants.setForeground(identifier, Color.BLACK);

        try {
            super.insertString(0, "; structure definitions\n" +
                            "; press n to add a new struct\n" +
                            "; press d to add a new field\n" +
                            "; press u to remove a field\n\n", comment);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        initialCaret = getLength();
    }

    public int getInitialCaret() {
        return initialCaret;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        // swallow
    }

    @Override
    public void remove(int offs, int length) {
        // swallow
    }
}
