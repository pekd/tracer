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

import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;

import javax.swing.JOptionPane;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.SymbolName;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class Utils {
    public static String color(Color color) {
        return "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

    public static String style(Color color) {
        return "color: " + color(color) + ";";
    }

    public static String tab(String s, int tabsz) {
        int pos = 0;
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '\t') {
                do {
                    pos++;
                    pos %= tabsz;
                    buf.append(' ');
                } while (pos != 0);
            } else if (c == '\n') {
                pos = 0;
                buf.append(c);
            } else {
                pos++;
                buf.append(c);
            }
        }
        return buf.toString();
    }

    public static String html(char c) {
        switch (c) {
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '"':
                return "&quot;";
            default:
                return Character.toString(c);
        }
    }

    public static String html(String text) {
        StringBuilder buf = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            buf.append(html(c));
        }
        return buf.toString();
    }

    public static void rename(ComputedSymbol selected, TraceAnalyzer trc, Component parent) {
        String input = JOptionPane.showInputDialog("Enter name:", selected.name);
        if (input != null) {
            if (input.trim().length() > 0) {
                String name = input.trim();
                for (ComputedSymbol sym : trc.getSymbols()) {
                    if (sym != selected && sym.name.equals(name)) {
                        JOptionPane.showMessageDialog(parent, "Error: symbol " + name + " already exists", "Rename symbol...", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                trc.renameSymbol(selected, name);
            } else {
                // reset symbol name to default name
                SymbolName names = new SymbolName(trc.getArchitecture().getFormat());
                String name = null;
                switch (selected.type) {
                    case SUBROUTINE:
                        name = names.sub(selected.address);
                        break;
                    case LOCATION:
                        name = names.loc(selected.address);
                        break;
                    default:
                    case DATA:
                    case UNKNOWN:
                        name = names.unk(selected.address);
                        break;
                }
                trc.renameSymbol(selected, name);
            }
        }
    }

    public static void setFunctionType(ComputedSymbol selected, TraceAnalyzer trc, Component parent) {
        String prototype;
        if (selected.prototype != null) {
            prototype = new Function(selected.name, selected.prototype).toString();
        } else {
            prototype = "void " + selected.name + "()";
        }
        String input = JOptionPane.showInputDialog("Enter prototype:", prototype);
        if (input != null && input.trim().length() > 0) {
            try {
                Parser parser = new Parser(input.trim(), trc.getTypeDatabase());
                Function fun = parser.parsePrototype();
                String name = fun.getName();
                for (ComputedSymbol sym : trc.getSymbols()) {
                    if (sym != selected && sym.name.equals(name)) {
                        JOptionPane.showMessageDialog(parent, "Error: symbol " + name + " already exists", "Set function type...", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                if (!fun.getName().equals(selected.name)) {
                    trc.renameSymbol(selected, fun.getName());
                }
                trc.setPrototype(selected, fun.getPrototype());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage(), "Set function type...", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (input != null) {
            trc.setPrototype(selected, null);
        }
    }

    public static void setCommentPC(long pc, TraceAnalyzer trc) {
        String comment = trc.getCommentForPC(pc);
        if (comment != null) {
            String input = JOptionPane.showInputDialog("Enter comment:", comment);
            if (input != null && input.trim().length() > 0) {
                trc.setCommentForPC(pc, input.trim());
            } else if (input != null) {
                trc.setCommentForPC(pc, null);
            }
        } else {
            String input = JOptionPane.showInputDialog("Enter comment:");
            if (input != null && input.trim().length() > 0) {
                trc.setCommentForPC(pc, input.trim());
            }
        }
    }

    public static void setExpression(long pc, TraceAnalyzer trc, Component parent) {
        String expr = trc.getExpression(pc);
        if (expr != null) {
            String input = JOptionPane.showInputDialog("Enter expression:", expr);
            if (input != null && input.trim().length() > 0) {
                try {
                    trc.setExpression(pc, input.trim());
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage(), "Set expression...", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (input != null) {
                try {
                    trc.setExpression(pc, null);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage(), "Set expression...", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } else {
            String input = JOptionPane.showInputDialog("Enter expression:");
            if (input != null && input.trim().length() > 0) {
                try {
                    trc.setExpression(pc, input.trim());
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(parent, "Error: " + ex.getMessage(), "Set expression...", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
    }
}
