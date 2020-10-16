package org.graalvm.vm.trcview.ui.data.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public abstract class Element {
    public static final int TYPE_PLAIN = 0;
    public static final int TYPE_COMMENT = 1;
    public static final int TYPE_KEYWORD = 2;
    public static final int TYPE_NUMBER = 3;
    public static final int TYPE_STRING = 4;
    public static final int TYPE_IDENTIFIER = 5;

    protected int type;

    public int getType() {
        return type;
    }

    public int getLength() {
        return getText().length();
    }

    public abstract String getText();

    public Font getFont() {
        switch (getType()) {
            default:
            case TYPE_IDENTIFIER:
            case TYPE_PLAIN:
                return JEditor.TEXT_FONT;
            case TYPE_COMMENT:
                return JEditor.TEXT_FONT;
            case TYPE_KEYWORD:
                return JEditor.KEYWORD_FONT;
            case TYPE_NUMBER:
                return JEditor.TEXT_FONT;
            case TYPE_STRING:
                return JEditor.TEXT_FONT;
        }
    }

    public Color getColor() {
        switch (getType()) {
            default:
            case TYPE_IDENTIFIER:
            case TYPE_PLAIN:
                return Color.BLACK;
            case TYPE_COMMENT:
                return Color.GRAY;
            case TYPE_KEYWORD:
                return Color.BLUE;
            case TYPE_NUMBER:
                return Color.RED;
            case TYPE_STRING:
                return Color.LIGHT_GRAY;
        }
    }

    public int draw(Graphics g, int posX, int posY) {
        Font fnt = getFont();

        g.setFont(fnt);
        g.setColor(getColor());

        String text = getText();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        FontRenderContext ctx = g2d.getFontRenderContext();
        Rectangle2D bounds = fnt.getStringBounds(text, ctx);
        g.drawString(text, posX, posY);
        return posX + bounds.getBounds().width;
    }

    @Override
    public String toString() {
        return "Element[\"" + getText() + "\"]";
    }
}
