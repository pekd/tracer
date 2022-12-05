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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.Subroutine;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.io.Block;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.CallListener;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.StringUtils;
import org.graalvm.vm.util.log.Trace;

@SuppressWarnings("serial")
public class InstructionView extends JPanel {
    private static final Logger log = Trace.create(InstructionView.class);

    public static final Color CALL_FG = Color.BLUE;
    public static final Color RET_FG = Color.RED;
    public static final Color SYSCALL_FG = Color.MAGENTA;
    public static final Color JMP_FG = Color.LIGHT_GRAY;
    public static final Color INDIRECTJMP_FG = new Color(0xAA, 0x55, 0x00);
    public static final Color JCC_FG = new Color(0xFF, 0x80, 0x00);
    public static final Color IRQ_FG = new Color(0x80, 0x40, 0x00);
    public static final Color ENDBR_FG = Color.GRAY;
    public static final Color ERROR_FG = Color.RED;

    public static final Color ROP_BG = new Color(0xFF, 0xE0, 0xE0);
    public static final Color RETRY_BG = new Color(0xE0, 0xFF, 0xE0);

    public static final String STYLE = "html, body, pre {" +
                    "    padding: 0;" +
                    "    margin: 0;" +
                    "}";
    public static final String COMMENT_STYLE = Utils.style(Color.LIGHT_GRAY);

    public static final int COMMENT_COLUMN = 48;

    private boolean reduceIntensity = true;

    private int tabSize = 16;

    private Block instructions;
    private InstructionViewModel model;
    private JList<String> insns;
    private int maxwidth = 0;

    private List<ChangeListener> changeListeners;
    private List<CallListener> callListeners;

    private TraceAnalyzer trc;

    private Consumer<String> status;

    public InstructionView(Consumer<String> status, Consumer<Long> position) {
        super(new BorderLayout());
        this.status = status;
        changeListeners = new ArrayList<>();
        callListeners = new ArrayList<>();

        instructions = new Block() {
            @Override
            public StepEvent getHead() {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public Node get(int i) {
                return null;
            }
        };
        insns = new JList<>(model = new InstructionViewModel());
        insns.setFont(MainWindow.FONT);
        insns.setCellRenderer(new CellRenderer());
        KeyListener[] listeners = insns.getKeyListeners();
        for (KeyListener l : listeners) {
            insns.removeKeyListener(l);
        }
        add(BorderLayout.CENTER, new JScrollPane(insns));

        insns.addListSelectionListener(e -> {
            int selected = insns.getSelectedIndex();
            if (selected == -1) {
                return;
            }

            Node node = instructions.get(selected);
            StepEvent step = Utils.getStep(node);
            if (step == null) {
                log.warning("step is null!");
                return;
            }
            Location loc = Location.getLocation(trc, step);
            StringBuilder buf = new StringBuilder();
            StepFormat fmt = trc.getArchitecture().getFormat();
            if (fmt.numberfmt == StepFormat.NUMBERFMT_HEX) {
                buf.append("PC=0x");
            } else {
                buf.append("PC=");
            }
            buf.append(fmt.formatAddress(loc.getPC()));
            if (loc.getSymbol() != null) {
                buf.append(' ');
                getName(buf, loc, fmt);
            }
            if (loc.getFilename() != null) {
                buf.append(" [");
                buf.append(loc.getFilename());
                if (loc.getOffset() != -1) {
                    buf.append(" @ 0x");
                    buf.append(HexFormatter.tohex(loc.getOffset(), 8));
                }
                buf.append("]");
            }
            status.accept(buf.toString());
            position.accept(step.getStep());
            fireChangeEvent();
        });

        insns.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    trace();
                }
            }
        });

        KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        insns.getInputMap().put(enter, enter);
        insns.getActionMap().put(enter, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                trace();
            }
        });
    }

    private static void getName(StringBuilder buf, Location loc, StepFormat fmt) {
        Symbol sym = loc.getSymbol();
        if (sym != null) {
            buf.append(sym.getName());
            long diff = loc.getPC() - sym.getValue();
            if (diff > 0) {
                buf.append('+');
                buf.append(fmt.formatShortAddress(diff));
            } else if (diff < 0) {
                buf.append('-');
                buf.append(fmt.formatShortAddress(-diff));
            }
        }
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        tabSize = trc.getArchitecture().getTabSize();
    }

    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    protected void fireChangeEvent() {
        for (ChangeListener l : changeListeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.log(Level.WARNING, "Error while running listener: " + t, t);
            }
        }
    }

    public void addCallListener(CallListener listener) {
        callListeners.add(listener);
    }

    public void removeCallListener(CallListener listener) {
        callListeners.remove(listener);
    }

    protected void fireCallEvent(BlockNode node) {
        for (CallListener l : callListeners) {
            try {
                l.call(node);
            } catch (Throwable t) {
                log.log(Level.WARNING, "Error while running listener: " + t, t);
            }
        }
    }

    protected void fireRetEvent(Event node) {
        for (CallListener l : callListeners) {
            try {
                l.ret(node);
            } catch (Throwable t) {
                log.log(Level.WARNING, "Error while running listener: " + t, t);
            }
        }
    }

    private void trace() {
        int selected = insns.getSelectedIndex();
        if (selected == -1) {
            return;
        }

        Node node = instructions.get(selected);
        if (node instanceof BlockNode) {
            fireCallEvent((BlockNode) node);
        } else {
            StepEvent step = (StepEvent) node;
            if (step.getType() == InstructionType.RET || step.getType() == InstructionType.RTI) {
                fireRetEvent((Event) node);
            }
        }
    }

    private static void comment(StringBuilder buf, String disassembly, String comment) {
        buf.insert(0, "<html><head><style>" + STYLE + "</style></head><body><pre>");
        if (disassembly.length() < COMMENT_COLUMN) {
            buf.append(StringUtils.repeat(" ", COMMENT_COLUMN - disassembly.length()));
        } else {
            buf.append(' ');
        }
        buf.append("<span style=\"" + COMMENT_STYLE + "\">; ");
        buf.append(escapeHTML(comment));
        buf.append("</span></pre></body></html>");
    }

    private String format(StepEvent step, StepEvent next) {
        StepFormat fmt = step.getFormat();
        Location loc = Location.getLocation(trc, step);
        StringBuilder buf = new StringBuilder();
        buf.append(fmt.formatAddress(loc.getPC()));
        buf.append(":  ");
        buf.append(loc.getAsm(tabSize));
        String comment = null;
        if (step.getType() == InstructionType.SYSCALL) {
            CpuState ns = next == null ? null : next.getState();
            comment = trc.getArchitecture().getSyscallDecoder().decode(step.getState(), ns, trc);
        }
        if (step.getType() != InstructionType.CALL) {
            String comments = comments(step);
            if (comments != null) {
                if (comment == null) {
                    comment = comments;
                } else {
                    comment += " // " + comments;
                }
            }
        }
        if (comment == null) {
            comment = Autocomment.get(trc, step);
        }
        if (comment != null) {
            comment(buf, loc.getAsm(tabSize), comment);
        }
        return buf.toString();
    }

    private String comments(StepEvent step) {
        String expr = null;
        try {
            expr = trc.evaluateExpression(step.getState());
        } catch (EvaluationException e) {
            status.accept(e.getMessage());
        }
        String comment1 = trc.getCommentForPC(step.getPC());
        String comment2 = trc.getCommentForInsn(step.getStep());
        List<String> parts = new ArrayList<>();
        if (expr != null) {
            parts.add(expr);
        }
        if (comment1 != null) {
            parts.add(comment1);
        }
        if (comment2 != null) {
            parts.add(comment2);
        }
        return parts.isEmpty() ? null : String.join("; ", parts);
    }

    private String format(BlockNode block) {
        InterruptEvent irq = block.getInterrupt();
        StepEvent step = irq.getStep();
        if (step == null) {
            step = Utils.getStep(block);
        }
        StepFormat fmt = step.getFormat();
        Location loc = Location.getLocation(trc, step);
        StringBuilder buf = new StringBuilder();
        buf.append(fmt.formatAddress(loc.getPC()));
        buf.append(":  ");
        buf.append(irq.toString());
        return buf.toString();
    }

    private static String escapeHTML(String s) {
        String result = s.replaceAll("&", "&amp;");
        result = result.replaceAll("<", "&lt;");
        result = result.replaceAll(">", "&gt;");
        return result;
    }

    private static String stripHTML(String s) {
        String result = s.replaceAll("<style>.*</style>", "");
        result = result.replaceAll("&gt;", ">");
        result = result.replaceAll("&lt;", "<");
        result = result.replaceAll("&amp;", "&");
        return result.replaceAll("<.*?>", "");
    }

    public void set(BlockNode block) {
        instructions = block;
        if (instructions.size() > 5000) {
            // compute max size
            maxwidth = 20 + tabSize;
            insns.setPrototypeCellValue(StringUtils.repeat("x", maxwidth));
        } else {
            maxwidth = -1;
            insns.setPrototypeCellValue(null);
        }
        model.changed();
        insns.setSelectedIndex(0);
        insns.repaint();
    }

    public void select(Node node) {
        for (int n = 0; n < instructions.size(); n++) {
            if (instructions.get(n).getId() == node.getId()) {
                insns.setSelectedIndex(n);
                insns.ensureIndexIsVisible(n);
                return;
            }
        }
    }

    public Node getSelectedNode() {
        int selected = insns.getSelectedIndex();
        if (selected == -1) {
            return null;
        }

        return instructions.get(selected);
    }

    public StepEvent getSelectedInstruction() {
        int selected = insns.getSelectedIndex();
        if (selected == -1) {
            return null;
        }

        Node node = instructions.get(selected);
        if (node instanceof BlockNode) {
            return ((BlockNode) node).getHead();
        } else {
            return (StepEvent) node;
        }
    }

    public StepEvent getPreviousInstruction() {
        int selected = insns.getSelectedIndex();
        if (selected == -1) {
            return null;
        }

        selected--;

        if (selected < 0) {
            return null;
        }

        Node node = instructions.get(selected);
        if (node instanceof BlockNode) {
            return ((BlockNode) node).getHead();
        } else {
            return (StepEvent) node;
        }
    }

    private void highlight(StepEvent current, Node prev, Component c) {
        long pc = current.getPC();
        if (prev instanceof BlockNode) {
            BlockNode b = (BlockNode) prev;
            StepEvent step = b.getHead();
            if (step == null) {
                return;
            }
            long npc = step.getPC() + (step.getMachinecode() != null ? step.getMachinecode().length : 0);
            if (pc != npc) {
                if (b.isInterrupt() && pc == step.getPC()) {
                    c.setBackground(RETRY_BG);
                    return;
                } else {
                    c.setBackground(ROP_BG);
                    return;
                }
            }
        }
        Color color = trc.getColor(current.getState());
        if (color != null) {
            if (reduceIntensity) {
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                int resultR = (r / 2) + 128;
                int resultG = (g / 2) + 128;
                int resultB = (b / 2) + 128;
                color = new Color(resultR, resultG, resultB);
            }
            c.setBackground(color);
        }
    }

    private void updatePrototype(String text) {
        if (maxwidth < 1) {
            return;
        }

        String plain = stripHTML(text);
        if (plain.length() > maxwidth) {
            maxwidth = plain.length();
            insns.setPrototypeCellValue(text);
        }
    }

    protected class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Node node = instructions.get(index);
            if (node instanceof BlockNode) {
                BlockNode block = (BlockNode) node;
                c.setForeground(CALL_FG);
                StepEvent head = block.getHead();
                if (index > 0) {
                    Node prev = instructions.get(index - 1);
                    if (!isSelected) {
                        highlight(head, prev, c);
                    }
                }
                if (head != null && head.isSyscall()) {
                    c.setForeground(SYSCALL_FG);
                }
                if (block.isInterrupt()) {
                    c.setForeground(IRQ_FG);
                }
            } else if (node instanceof Event) {
                StepEvent step = (StepEvent) node;
                String mnemonic = step.getMnemonic();
                if (mnemonic == null) {
                    c.setForeground(ERROR_FG);
                } else {
                    switch (step.getType()) {
                        case RET:
                            c.setForeground(RET_FG);
                            break;
                        case RTI:
                            c.setForeground(RET_FG);
                            break;
                        case SYSCALL:
                            c.setForeground(SYSCALL_FG);
                            break;
                        case JMP:
                            c.setForeground(JMP_FG);
                            break;
                        case JMP_INDIRECT:
                            c.setForeground(INDIRECTJMP_FG);
                            break;
                        case JCC:
                            c.setForeground(JCC_FG);
                            break;
                        case OTHER:
                            if (mnemonic.equals("endbr32") || mnemonic.equals("endbr64")) {
                                c.setForeground(ENDBR_FG);
                            }
                            break;
                    }
                }

                if (index > 0) {
                    Node prev = instructions.get(index - 1);
                    if (!isSelected) {
                        highlight(step, prev, c);
                    }
                }
            }
            return c;
        }
    }

    public class InstructionViewModel extends AbstractListModel<String> {
        @Override
        public String getElementAt(int i) {
            Node n = instructions.get(i);
            if (n instanceof Event) {
                StepEvent step = (StepEvent) n;
                if (i + 1 < instructions.size()) {
                    StepEvent next;
                    Node nn = instructions.get(i + 1);
                    if (nn instanceof BlockNode) {
                        next = ((BlockNode) nn).getHead();
                    } else {
                        next = (StepEvent) nn;
                    }
                    String result = format(step, next);
                    updatePrototype(result);
                    return result;
                } else {
                    String result = format(step, null);
                    updatePrototype(result);
                    return result;
                }
            } else if (n instanceof BlockNode) {
                StepEvent step = ((BlockNode) n).getHead();
                StepEvent firstStep = ((BlockNode) n).getFirstStep();
                if (firstStep == null) { // happens if program crashed in call
                    // TODO: provide correct address of call target
                    firstStep = step;
                }
                Location loc = Location.getLocation(trc, firstStep);
                StringBuilder buf = new StringBuilder();
                StepEvent next = null;
                if (((BlockNode) n).isInterrupt()) {
                    buf.append(format((BlockNode) n));
                } else {
                    if (i + 1 < instructions.size()) {
                        Node nn = instructions.get(i + 1);
                        if (nn instanceof BlockNode) {
                            next = ((BlockNode) nn).getHead();
                        } else {
                            next = (StepEvent) nn;
                        }
                        buf.append(format(step, next));
                    } else {
                        buf.append(format(step, null));
                    }
                }
                if (loc.getSymbol() != null) {
                    buf.append(" # <");
                    getName(buf, loc, trc.getArchitecture().getFormat());
                    buf.append(">");
                    if (loc.getFilename() != null) {
                        buf.append(" [");
                        buf.append(loc.getFilename());
                        buf.append("]");
                    }
                } else if (loc.getFilename() != null) {
                    buf.append(" # 0x");
                    buf.append(HexFormatter.tohex(loc.getPC(), 8));
                    buf.append(" [");
                    buf.append(loc.getFilename());
                    buf.append("]");
                }

                Symbol sym = trc.getSymbol(loc.getPC());
                if (sym != null && sym instanceof Subroutine && ((Subroutine) sym).getPrototype() != null) {
                    Subroutine sub = (Subroutine) sym;
                    Function fun = new Function(sub.getName(), sub.getPrototype());
                    CpuState ns = next == null ? null : next.getState();
                    String decoded = trc.getArchitecture().getCallDecoder().decode(fun, step.getState(), ns, trc);
                    String comments = comments(step);
                    if (decoded == null) {
                        decoded = comments;
                    } else if (comments != null) {
                        decoded += " // " + comments;
                    }
                    if (decoded != null) {
                        int length = buf.length() - step.getFormat().formatAddress(step.getPC()).length() - 3;
                        String str = buf.toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;");
                        buf = new StringBuilder();
                        buf.append("<html><head><style>").append(STYLE).append("</style></head><body><pre>");
                        buf.append(str);
                        if (length >= COMMENT_COLUMN) {
                            buf.append(" ");
                        } else {
                            buf.append(StringUtils.repeat(" ", COMMENT_COLUMN - length));
                        }
                        buf.append("<span style=\"" + COMMENT_STYLE + "\">; ");
                        buf.append(escapeHTML(decoded));
                        buf.append("</span></pre></body></html>");
                    }
                } else {
                    String comments = comments(step);
                    if (comments != null) {
                        int length = buf.length() - step.getFormat().formatAddress(step.getPC()).length() - 3;
                        String str = buf.toString().replaceAll("&", "&amp;").replaceAll("<", "&lt;");
                        buf = new StringBuilder();
                        buf.append("<html><head><style>").append(STYLE).append("</style></head><body><pre>");
                        buf.append(str);
                        if (length >= COMMENT_COLUMN) {
                            buf.append(" ");
                        } else {
                            buf.append(StringUtils.repeat(" ", COMMENT_COLUMN - length));
                        }
                        buf.append("<span style=\"" + COMMENT_STYLE + "\">; ");
                        buf.append(escapeHTML(comments));
                        buf.append("</span></pre></body></html>");
                    }
                }
                String result = buf.toString();
                updatePrototype(result);
                return result;
            } else {
                throw new IllegalStateException("invalid node type: " + n.getClass().getSimpleName());
            }
        }

        @Override
        public int getSize() {
            return instructions.size();
        }

        public void changed() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
