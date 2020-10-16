package org.graalvm.vm.trcview.ui.data.editor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

@SuppressWarnings("serial")
public class JEditor extends JComponent implements Scrollable, ChangeListener {
    public static final Font TEXT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public static final Font KEYWORD_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public static final Color CURSOR_COLOR = Color.GRAY;
    private static final Color SELECTION_COLOR = new Color(232, 242, 254);
    private static final Color HIGHLIGHT_COLOR = new Color(242, 242, 242);

    private static final int MAX_CACHE_SIZE = 10_000;

    private static final Logger log = Trace.create(JEditor.class);

    private int currentLine;
    private int currentColumn;

    private boolean selection;
    private int selectionLine;
    private int selectionColumn;

    private EditorModel model;

    private FontMetrics fontMetrics;
    private int maxCharWidth;
    private int charHeight;

    private int offsetX = 10;
    private int offsetY = 10;

    private Rectangle clip = null;

    private Map<Integer, Line> lineCache = new HashMap<>();

    private Color selectionColor = SELECTION_COLOR;
    private Color highlightColor = HIGHLIGHT_COLOR;

    private List<ActionListener> actionListeners = new ArrayList<>();

    public JEditor() {
        this(new DefaultEditorModel());
    }

    public JEditor(EditorModel model) {
        fontMetrics = getFontMetrics(TEXT_FONT);
        charHeight = fontMetrics.getHeight();
        Rectangle2D charsize = TEXT_FONT.getMaxCharBounds(fontMetrics.getFontRenderContext());
        maxCharWidth = charsize.getBounds().width;

        setBackground(Color.WHITE);
        setModel(model);

        setFocusable(true);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selection = false;
                setCursorToPoint(e.getX(), e.getY());
                requestFocusInWindow();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doubleClick();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                selection = true;
                setCursorToPoint(e.getX(), e.getY());
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        selection = e.isShiftDown();
                        setCursor(currentLine, currentColumn - 1);
                        scrollToCursor();
                        break;
                    case KeyEvent.VK_RIGHT:
                        selection = e.isShiftDown();
                        setCursor(currentLine, currentColumn + 1);
                        scrollToCursor();
                        break;
                    case KeyEvent.VK_UP:
                        selection = e.isShiftDown();
                        setCursor(currentLine - 1, currentColumn);
                        scrollToCursor();
                        break;
                    case KeyEvent.VK_DOWN:
                        selection = e.isShiftDown();
                        setCursor(currentLine + 1, currentColumn);
                        scrollToCursor();
                        break;
                }
            }
        });

        // swallow arrow keys
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == JEditor.this) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_DOWN:
                            for (KeyListener l : getKeyListeners()) {
                                try {
                                    switch (e.getID()) {
                                        case KeyEvent.KEY_PRESSED:
                                            l.keyPressed(e);
                                            break;
                                        case KeyEvent.KEY_RELEASED:
                                            l.keyReleased(e);
                                            break;
                                        case KeyEvent.KEY_TYPED:
                                            l.keyTyped(e);
                                            break;
                                    }
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                }
                            }
                            e.consume();
                            return true;
                        default:
                            return false;
                    }
                } else {
                    return false;
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(enter, enter);
        getActionMap().put(enter, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enter();
            }
        });
    }

    public void setModel(EditorModel model) {
        if (this.model != null) {
            this.model.removeChangeListener(this);
        }
        this.model = model;
        updateSize();
        currentLine = 0;
        currentColumn = 0;
        model.addChangeListener(this);
    }

    @Override
    public void valueChanged() {
        lineCache.clear();
        updateSize();
        repaint();
    }

    private void updateSize() {
        Line maxline = model.getLongestLine();
        Dimension size;
        int height = charHeight * model.getLineCount() + 2 * offsetY;
        if (maxline == null) {
            size = new Dimension(maxCharWidth * model.getMaximumLineLength() + 2 * offsetX, height);
        } else {
            int width = 0;
            for (Element e : maxline.getElements()) {
                Font fnt = e.getFont();
                FontRenderContext ctx = getFontMetrics(fnt).getFontRenderContext();
                width += fnt.getStringBounds(e.getText(), ctx).getBounds().width;
            }
            size = new Dimension(width + 2 * offsetX, height);
        }
        setMinimumSize(size);
        setPreferredSize(size);
    }

    public void setCursorToPoint(int x, int y) {
        if (clip == null) {
            return;
        }

        int posY = (y - offsetY) / charHeight;

        int posX = 0;
        Line line = getLine(posY);
        if (x <= offsetX) {
            posX = 0;
        } else if (line != null) {
            StringBuilder buf = new StringBuilder();
            for (Element e : line.getElements()) {
                buf.append(e.getText());
            }

            FontRenderContext ctx = fontMetrics.getFontRenderContext();
            String s = buf.toString();
            int lastWidth = 0;
            int lastCX = offsetX;

            for (int i = 1; i <= s.length(); i++) {
                Rectangle2D bounds = TEXT_FONT.getStringBounds(s, 0, i, ctx);
                int width = bounds.getBounds().width;
                int cx = offsetX + (width + lastWidth) / 2;
                lastWidth = width;
                if (x >= lastCX && x <= cx) {
                    // found
                    posX = i - 1;
                    lastCX = cx;
                    break;
                }
                lastCX = cx;
            }

            if (x > lastCX) {
                posX = s.length();
            }
        }

        setCursor(posY, posX);
    }

    public void setCursorLine(int line) {
        if (line < 0) {
            currentLine = 0;
        } else if (line >= model.getLineCount()) {
            currentLine = model.getLineCount() - 1;
            if (currentLine < 0) {
                currentLine = 0;
            }
        } else {
            currentLine = line;
        }

        if (!selection) {
            selectionLine = currentLine;
        }

        setCursorColumn(currentColumn);
    }

    public void setCursorColumn(int column) {
        Line l = getLine(currentLine);
        if (l == null) {
            currentColumn = 0;
        } else if (column < 0) {
            currentColumn = 0;
        } else if (column > l.getLength()) {
            currentColumn = l.getLength();
            if (currentColumn < 0) {
                currentColumn = 0;
            }
        } else {
            currentColumn = column;
        }

        if (!selection) {
            selectionColumn = currentColumn;
        }

        repaint();
    }

    public void setCursor(int line, int column) {
        setCursorLine(line);
        setCursorColumn(column);
    }

    public void scrollToCursor() {
        int x = getX(currentLine, currentColumn);
        int y = getY(currentLine);
        scrollRectToVisible(new Rectangle(x, y, maxCharWidth, charHeight));
    }

    private Line getLine(int y) {
        Line line = lineCache.get(y);
        if (line == null) {
            line = model.getLine(y);
            if (line != null) {
                if (lineCache.size() > MAX_CACHE_SIZE) {
                    lineCache.clear();
                }
                lineCache.put(y, line);
            }
        }
        return line;
    }

    public int getCursorLine() {
        return currentLine;
    }

    public int getCursorColumn() {
        return currentColumn;
    }

    public boolean hasSelection() {
        return selection;
    }

    private int getX(int line, int column) {
        return getX(line, column, TEXT_FONT);
    }

    private int getX(int line, int column, Font fnt) {
        Line ln = getLine(line);
        if (ln == null) {
            return column * maxCharWidth + offsetX;
        } else {
            StringBuilder buf = new StringBuilder();
            for (Element e : ln.getElements()) {
                buf.append(e.getText());
            }
            int col = column;
            if (col > buf.length()) {
                col = buf.length();
            }
            FontRenderContext ctx = getFontMetrics(fnt).getFontRenderContext();
            Rectangle2D bounds = fnt.getStringBounds(buf.toString(), 0, col, ctx);
            return bounds.getBounds().width + offsetX;
        }
    }

    private int getY(int line) {
        return line * charHeight + fontMetrics.getAscent() + offsetY;
    }

    public void setHighlightColor(Color color) {
        highlightColor = color;
        repaint();
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public Element getCurrentElement() {
        Line line = getLine(currentLine);

        if (line == null) {
            return null;
        }

        int i = 0;
        for (Element e : line.getElements()) {
            int next = i + e.getLength();
            if (currentColumn >= i && currentColumn < next) {
                return e;
            }
            i = next;
        }

        return null;
    }

    private void doubleClick() {
        fireActionEvent();
    }

    private void enter() {
        fireActionEvent();
    }

    private void fireActionEvent() {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "enter");
        for (ActionListener l : actionListeners) {
            try {
                l.actionPerformed(evt);
            } catch (Throwable t) {
                log.log(Levels.ERROR, "Uncaught exception: " + t, t);
            }
        }
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        clip = g.getClipBounds();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);

        int posY = (clip.y - offsetY) / charHeight;

        int startY = posY - 1;
        if (startY < 0) {
            startY = 0;
        }

        int endX = clip.x + clip.width;
        int endY = startY + clip.height / charHeight + 2;

        if (endY > model.getLineCount()) {
            endY = model.getLineCount();
        }

        int selectionStartLine;
        int selectionEndLine;
        int selectionStartColumn;
        int selectionEndColumn;

        if (selectionLine == currentLine) {
            selectionStartLine = currentLine;
            selectionEndLine = currentLine;
            if (selectionColumn < currentColumn) {
                selectionStartColumn = selectionColumn;
                selectionEndColumn = currentColumn;
            } else {
                selectionStartColumn = currentColumn;
                selectionEndColumn = selectionColumn;
            }
        } else if (selectionLine < currentLine) {
            selectionStartLine = selectionLine;
            selectionEndLine = currentLine;
            selectionStartColumn = selectionColumn;
            selectionEndColumn = currentColumn;
        } else {
            selectionStartLine = currentLine;
            selectionEndLine = selectionLine;
            selectionStartColumn = currentColumn;
            selectionEndColumn = selectionColumn;
        }

        // render elements
        FontRenderContext ctx = ((Graphics2D) g).getFontRenderContext();
        for (int y = startY; y < endY; y++) {
            Line line = getLine(y);

            // highlight current element
            if (line != null && highlightColor != null && y == currentLine && !selection) {
                int off = 0;
                int posX = 0;
                for (Element element : line.getElements()) {
                    int next = off + element.getLength();
                    String text = element.getText();
                    Font fnt = element.getFont();
                    Rectangle2D bounds = fnt.getStringBounds(text, ctx);
                    int width = bounds.getBounds().width;
                    if (off <= currentColumn && next > currentColumn) {
                        if (!text.trim().isEmpty()) { // don't highlight white space
                            String trimmed = text.trim();
                            int w = fnt.getStringBounds(trimmed, ctx).getBounds().width;
                            int skip = 0;
                            for (int i = 0; i < text.length(); i++) {
                                if (!Character.isWhitespace(text.charAt(i))) {
                                    String space = text.substring(0, i);
                                    skip = fnt.getStringBounds(space, ctx).getBounds().width;
                                    off += i;
                                    next = off + trimmed.length();
                                    break;
                                }
                            }
                            // check again because the element was stripped in between
                            if (off <= currentColumn && next > currentColumn) {
                                g.setColor(highlightColor);
                                g.fillRect(offsetX + posX + skip, offsetY + y * charHeight, w, charHeight);
                            }
                        }
                        break;
                    }
                    off = next;
                    posX += width;
                }
            }

            // draw selection
            if (y >= selectionStartLine && y <= selectionEndLine && selectionColor != null) {
                g.setColor(selectionColor);
                if (y == selectionStartLine) {
                    int sx = getX(selectionStartLine, selectionStartColumn);
                    if (y == selectionEndLine) {
                        int ex = getX(selectionStartLine, selectionEndColumn);
                        g.fillRect(sx, offsetY + y * charHeight, ex - sx - 1, charHeight);
                    } else {
                        g.fillRect(sx, offsetY + y * charHeight, getWidth(), charHeight);
                    }
                } else if (y == selectionEndLine) {
                    int ex = getX(selectionEndLine, selectionEndColumn);
                    g.fillRect(offsetX, offsetY + y * charHeight, ex - offsetX - 1, charHeight);
                } else if (y > selectionStartLine && y < selectionEndLine) {
                    g.fillRect(offsetX, offsetY + y * charHeight, getWidth(), charHeight);
                }
            }

            // draw element text
            int x = offsetX;
            if (line != null) {
                List<Element> elements = line.getElements();
                for (Element element : elements) {
                    x = element.draw(g, x, offsetY + y * charHeight + fontMetrics.getAscent());
                    if (x > endX) {
                        break;
                    }
                }
            }
        }

        // draw cursor crosshair
        int cursorY = getY(currentLine) + 1;
        int cursorX = getX(currentLine, currentColumn) - 1;
        g.setColor(CURSOR_COLOR);
        g.drawLine(0, cursorY, getWidth(), cursorY);
        g.drawLine(cursorX, 0, cursorX, getHeight());
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(300, 300);
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        // stretch to viewport size if viewport is bigger than this component
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getHeight() > getPreferredSize().height;
        }
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        // stretch to viewport size if viewport is bigger than this component
        Container parent = SwingUtilities.getUnwrappedParent(this);
        if (parent instanceof JViewport) {
            return parent.getWidth() > getPreferredSize().width;
        }
        return false;
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        // get the current position
        int currentPosition = 0;
        int maxUnitIncrement;
        if (orientation == SwingConstants.HORIZONTAL) {
            currentPosition = visibleRect.x;
            maxUnitIncrement = maxCharWidth;
        } else {
            currentPosition = visibleRect.y;
            maxUnitIncrement = charHeight;
        }

        // return the number of pixels between currentPosition and the nearest tick mark in the
        // indicated direction
        if (direction < 0) {
            int newPosition = currentPosition - (currentPosition / maxUnitIncrement) * maxUnitIncrement;
            return newPosition == 0 ? maxUnitIncrement : newPosition;
        } else {
            return ((currentPosition / maxUnitIncrement) + 1) * maxUnitIncrement - currentPosition;
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width - maxCharWidth;
        } else {
            return visibleRect.height - charHeight;
        }
    }
}
