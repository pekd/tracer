/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.graalvm.vm.trcview.ui;

import static org.graalvm.vm.trcview.ui.Utils.color;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigInteger;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

@SuppressWarnings("serial")
public class StateView extends JPanel {
    private static final String STYLE = "<style>\n" +
                    "html, body, pre {\n" +
                    "    padding: 0;\n" +
                    "    margin: 0;\n" +
                    "}\n" +
                    "pre {\n" +
                    "    font-family: " + Font.MONOSPACED + ";\n" +
                    "    font-size: 11pt;\n" +
                    "}\n" +
                    ".change {\n" +
                    "    color: red;\n" +
                    "}\n" +
                    ".comment {\n" +
                    "    color: " + color(Color.LIGHT_GRAY) + ";\n" +
                    "}\n" +
                    ".symbol {\n" +
                    "    color: " + color(Color.BLUE) + ";\n" +
                    "}\n" +
                    ".mnemonic {\n" +
                    "    color: " + color(Color.BLUE) + ";\n" +
                    "}\n" +
                    "</style>";
    private static final String TOOLTIP_STYLE = "<style>\n" +
                    "html, body, pre {\n" +
                    "    padding: 0;\n" +
                    "    margin: 0;\n" +
                    "}\n" +
                    "</style>";
    private JTextPane text;

    private StepEvent step;
    private StepEvent previous;

    private TraceAnalyzer trc;
    private Popup tooltipContainer;

    public StateView(MemoryView mem) {
        super(new BorderLayout());
        text = new JTextPane();
        text.setEditorKit(new HTMLEditorKit());
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setBackground(UIManager.getColor("TextField.background"));
        text.setEditable(false);
        text.setContentType("text/html");

        JToolTip tooltip = text.createToolTip();
        PopupFactory popupFactory = PopupFactory.getSharedInstance();

        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                String number = getValue(p);
                if (number != null) {
                    mem.setExpression(toexpr(number));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (tooltipContainer != null) {
                    tooltipContainer.hide();
                    tooltipContainer = null;
                    tooltip.setTipText(null);
                }
            }
        });

        text.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = new Point(e.getX(), e.getY());
                String value = getValue(p);
                if (value != null) {
                    // show
                    String oldText = tooltip.getTipText();
                    tooltip.setTipText(format(value));
                    if (!Objects.equals(oldText, tooltip.getTipText())) {
                        oldText = tooltip.getTipText();
                        if (tooltipContainer != null) {
                            tooltipContainer.hide();
                        }
                        Point loc = text.getToolTipLocation(e);
                        if (loc == null) {
                            loc = new Point(e.getX(), e.getY() + 8);
                        }
                        int x = text.getLocationOnScreen().x + loc.x;
                        int y = text.getLocationOnScreen().y + loc.y;
                        y += 8;
                        tooltipContainer = popupFactory.getPopup(text, tooltip, x, y);
                        tooltipContainer.show();
                    }
                } else {
                    // hide
                    tooltip.setTipText(null);
                    if (tooltipContainer != null) {
                        tooltipContainer.hide();
                        tooltipContainer = null;
                    }
                }
            }
        });

        add(BorderLayout.CENTER, new JScrollPane(text));
    }

    private static String toexpr(String data) {
        String[] parts = data.split(":");
        if (parts.length == 2) {
            String type = parts[0];
            String number = parts[1];
            switch (type) {
                case "oct":
                    return "0" + number;
                case "dec":
                    return number;
                case "hex":
                    return "0x" + number;
                default:
                    return number;
            }
        }
        return null;
    }

    private static String str(BigInteger value) {
        byte[] bytes = value.toByteArray();
        StringBuilder buf = new StringBuilder();
        for (byte b : bytes) {
            buf.append(DecoderUtils.encode(b));
        }
        return buf.toString();
    }

    private static String str(long value) {
        return str(BigInteger.valueOf(value));
    }

    private static String rev(BigInteger value) {
        byte[] bytes = value.toByteArray();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[bytes.length - i - 1];
            buf.append(DecoderUtils.encode(b));
        }
        return buf.toString();
    }

    private static String rev(long value) {
        return rev(BigInteger.valueOf(value));
    }

    private String cstr(long addr) {
        return Utils.html(DecoderUtils.cstr(addr, step.getStep(), trc, 16));
    }

    private String format(long value) {
        String memoryText;
        try {
            long mem = trc.getI64(value, step.getStep());
            memoryText = "Memory (oct): " + Long.toUnsignedString(mem, 8) + "<br>" +
                            "Memory (dec): " + mem + "<br>" +
                            "Memory (hex): " + Long.toUnsignedString(mem, 16) + "<br>" +
                            "Memory (str): " + cstr(value) + "<br>";
        } catch (MemoryNotMappedException e) {
            memoryText = "Memory: <i>not mapped</i><br>";
        }
        String result = "<html><head>" + TOOLTIP_STYLE + "</head><body><pre>" +
                        "Oct: " + Long.toUnsignedString(value, 8) + "<br>" +
                        "Dec: " + value + "<br>" +
                        "Hex: " + Long.toUnsignedString(value, 16) + "<br>" +
                        "Text: \"" + Utils.html(str(value)) + "\"<br>" +
                        "Text (rev): \"" + Utils.html(rev(value)) + "\"<br>" +
                        memoryText +
                        "</pre></body></html>";
        return result;
    }

    private static String format(BigInteger value) {
        String result = "<html><head>" + TOOLTIP_STYLE + "</head><body><pre>" +
                        "Oct: " + value.toString(8) + "<br>" +
                        "Dec: " + value + "<br>" +
                        "Hex: " + value.toString(16) + "<br>" +
                        "Text: \"" + Utils.html(str(value)) + "\"<br>" +
                        "Text (rev): \"" + Utils.html(rev(value)) + "\"<br>" +
                        "</pre></body></html>";
        return result;
    }

    private String format(String data) {
        String[] parts = data.split(":");
        if (parts.length == 2) {
            String type = parts[0];
            String number = parts[1];
            switch (type) {
                case "oct":
                    try {
                        return format(Long.parseUnsignedLong(number, 8));
                    } catch (NumberFormatException e) {
                        return format(new BigInteger(number, 8));
                    }
                case "dec": {
                    try {
                        return format(Long.parseUnsignedLong(number));
                    } catch (NumberFormatException e) {
                        return format(new BigInteger(number));
                    }
                }
                case "hex":
                    try {
                        return format(Long.parseUnsignedLong(number, 16));
                    } catch (NumberFormatException e) {
                        return format(new BigInteger(number, 16));
                    }
            }
        }
        return null;
    }

    private String getValue(Point p) {
        int pos = text.viewToModel(p);
        if (pos >= 0) {
            StyledDocument doc = (StyledDocument) text.getDocument();
            Element elem = doc.getCharacterElement(pos);
            String data = getData(elem);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    private static String getData(Element element) {
        AttributeSet attr = element.getAttributes();
        AttributeSet attrs = (AttributeSet) attr.getAttribute(HTML.Tag.SPAN);
        if (attrs != null) {
            String data = (String) attrs.getAttribute(HTML.Attribute.DATA);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        update();
    }

    public void setState(StepEvent step) {
        this.previous = null;
        this.step = step;
        update();
    }

    public void setState(StepEvent previous, StepEvent current) {
        this.previous = previous;
        this.step = current;
        update();
    }

    private String get() {
        if (previous != null) {
            return StateEncoder.encode(trc, previous, step);
        } else {
            return StateEncoder.encode(trc, step);
        }
    }

    private void update() {
        String content;
        if (step != null) {
            content = get();
        } else {
            content = "";
        }
        String html = "<html><head>" + STYLE + "</head><body><pre>" + content + "</pre></body></html>";
        text.setText(html);
        text.setCaretPosition(0);
    }
}
