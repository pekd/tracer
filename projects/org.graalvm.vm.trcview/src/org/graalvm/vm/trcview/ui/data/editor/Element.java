package org.graalvm.vm.trcview.ui.data.editor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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

    public int draw(Graphics g, int posX, int posY) {
        switch (getType()) {
            default:
            case TYPE_IDENTIFIER:
            case TYPE_PLAIN:
                g.setFont(JEditor.TEXT_FONT);
                g.setColor(Color.BLACK);
                break;
            case TYPE_COMMENT:
                g.setFont(JEditor.TEXT_FONT);
                g.setColor(Color.GRAY);
                break;
            case TYPE_KEYWORD:
                g.setFont(JEditor.KEYWORD_FONT);
                g.setColor(Color.BLUE);
                break;
            case TYPE_NUMBER:
                g.setFont(JEditor.TEXT_FONT);
                g.setColor(Color.RED);
                break;
            case TYPE_STRING:
                g.setFont(JEditor.TEXT_FONT);
                g.setColor(Color.LIGHT_GRAY);
                break;
        }

        String text = getText();
        Font fnt = g.getFont();
        FontRenderContext ctx = ((Graphics2D) g).getFontRenderContext();
        Rectangle2D bounds = fnt.getStringBounds(text, ctx);
        g.drawString(text, posX, posY);
        return posX + bounds.getBounds().width;
    }
}
