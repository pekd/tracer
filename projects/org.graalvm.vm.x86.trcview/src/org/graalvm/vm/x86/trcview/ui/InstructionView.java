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
package org.graalvm.vm.x86.trcview.ui;

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
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.StringUtils;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.x86.isa.AMD64InstructionQuickInfo;
import org.graalvm.vm.x86.isa.AMD64InstructionQuickInfo.InstructionType;
import org.graalvm.vm.x86.isa.CpuState;
import org.graalvm.vm.x86.isa.instruction.Jmp.JmpIndirect;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.trcview.analysis.Subroutine;
import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.decode.CallDecoder;
import org.graalvm.vm.x86.trcview.decode.SyscallDecoder;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.io.RecordNode;
import org.graalvm.vm.x86.trcview.net.TraceAnalyzer;
import org.graalvm.vm.x86.trcview.ui.event.CallListener;
import org.graalvm.vm.x86.trcview.ui.event.ChangeListener;

@SuppressWarnings("serial")
public class InstructionView extends JPanel {
    private static final Logger log = Trace.create(InstructionView.class);

    public static final Color CALL_FG = Color.BLUE;
    public static final Color RET_FG = Color.RED;
    public static final Color SYSCALL_FG = Color.MAGENTA;
    public static final Color JMP_FG = Color.LIGHT_GRAY;
    public static final Color INDIRECTJMP_FG = new Color(0xAA, 0x55, 0x00);
    public static final Color JCC_FG = new Color(0xFF, 0x80, 0x00);
    public static final Color ENDBR_FG = Color.GRAY;
    public static final Color ERROR_FG = Color.RED;

    public static final Color ROP_BG = new Color(0xFF, 0xE0, 0xE0);

    public static final String STYLE = "html, body, pre {" +
                    "    padding: 0;" +
                    "    margin: 0;" +
                    "}";
    public static final String COMMENT_STYLE = Utils.style(Color.LIGHT_GRAY);

    public static final int COMMENT_COLUMN = 48;

    private List<Node> instructions;
    private InstructionViewModel model;
    private JList<String> insns;

    private List<ChangeListener> changeListeners;
    private List<CallListener> callListeners;

    private TraceAnalyzer trc;

    public InstructionView(Consumer<String> status, Consumer<Long> position) {
        super(new BorderLayout());
        changeListeners = new ArrayList<>();
        callListeners = new ArrayList<>();

        instructions = new ArrayList<>();
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
            StepRecord step;
            if (node instanceof BlockNode) {
                step = ((BlockNode) node).getHead();
            } else {
                step = (StepRecord) ((RecordNode) node).getRecord();
            }
            Location loc = Location.getLocation(trc, step);
            StringBuilder buf = new StringBuilder();
            buf.append("PC=0x");
            buf.append(HexFormatter.tohex(loc.getPC(), 16));
            if (loc.getSymbol() != null) {
                buf.append(" ");
                buf.append(loc.getSymbol());
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
            position.accept(step.getInstructionCount());
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

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
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

    protected void fireRetEvent(RecordNode node) {
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
            StepRecord step = (StepRecord) ((RecordNode) node).getRecord();
            if (step.getMnemonic() != null && step.getMnemonic().equals("ret")) {
                fireRetEvent((RecordNode) node);
            }
        }
    }

    private static void comment(StringBuilder buf, String disassembly, String comment) {
        buf.insert(0, "<html><head><style>" + STYLE + "</style></head><body><pre>");
        buf.append(StringUtils.repeat(" ", COMMENT_COLUMN - disassembly.length()));
        buf.append("<span style=\"" + COMMENT_STYLE + "\">; ");
        buf.append(comment);
        buf.append("</span></pre></body></html>");
    }

    private String format(StepRecord step, StepRecord next) {
        Location loc = Location.getLocation(trc, step);
        StringBuilder buf = new StringBuilder();
        buf.append("0x");
        buf.append(HexFormatter.tohex(loc.getPC(), 16));
        buf.append(": ");
        buf.append(Utils.tab(loc.getAsm(), 12));
        String mnemonic = loc.getMnemonic();
        if (mnemonic != null && mnemonic.equals("syscall")) {
            CpuState ns = next == null ? null : next.getState().getState();
            String decoded = SyscallDecoder.decode(step.getState().getState(), ns, trc);
            if (decoded != null) {
                comment(buf, loc.getAsm(), decoded);
            }
        }
        return buf.toString();
    }

    private static String stripHTML(String s) {
        String result = s.replaceAll("<style>.*</style>", "");
        result = result.replaceAll("&gt;", ">");
        result = result.replaceAll("&lt;", "<");
        result = result.replaceAll("&amp;", "&");
        return result.replaceAll("<.*?>", "");
    }

    public void set(BlockNode block) {
        instructions = new ArrayList<>();
        for (Node n : block.getNodes()) {
            if (n instanceof BlockNode) {
                instructions.add(n);
            } else if (n instanceof RecordNode && ((RecordNode) n).getRecord() instanceof StepRecord) {
                instructions.add(n);
            }
        }
        int max = 0;
        if (instructions.size() > 10000) {
            // compute max size
            max = 20 + 12;
            for (int i = 0; i < instructions.size(); i++) {
                Node n = instructions.get(i);
                if (n instanceof BlockNode) {
                    Location loc = Location.getLocation(trc, ((BlockNode) n).getFirstStep());
                    int len = 0;
                    if (loc.getSymbol() != null) {
                        len += 5 + loc.getSymbol().length();
                        if (loc.getFilename() != null) {
                            len += 3 + loc.getFilename().length();
                        }
                    } else if (loc.getFilename() != null) {
                        len += 8 + HexFormatter.tohex(loc.getPC(), 8).length();
                        len += loc.getFilename().length();
                    }
                    max = Math.max(max, 20 + 12 + 18 + len); // pc = 20, insn = 12, arg = 18
                } else if (n instanceof RecordNode && ((RecordNode) n).getRecord() instanceof StepRecord) {
                    StepRecord step = (StepRecord) ((RecordNode) n).getRecord();
                    InstructionType type = AMD64InstructionQuickInfo.getType(step.getMachinecode());
                    switch (type) {
                        case SYSCALL: {
                            CpuState ns = null;
                            if (i + 1 < instructions.size()) {
                                StepRecord next;
                                Node nn = instructions.get(i + 1);
                                if (nn instanceof BlockNode) {
                                    next = ((BlockNode) nn).getHead();
                                } else {
                                    next = (StepRecord) ((RecordNode) nn).getRecord();
                                }
                                ns = next == null ? null : next.getState().getState();
                            }
                            String decoded = SyscallDecoder.decode(step.getState().getState(), ns, trc);
                            max = Math.max(max, 20 + COMMENT_COLUMN + 2 + decoded.length());
                            break;
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < model.getSize(); i++) {
                String s = stripHTML(model.getElementAt(i));
                max = Math.max(max, s.length());
            }
        }
        insns.setPrototypeCellValue(StringUtils.repeat("x", max + 10)); // +10 is a hack
        model.changed();
        // insns.setModel(model);
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

    public StepRecord getSelectedInstruction() {
        int selected = insns.getSelectedIndex();
        if (selected == -1) {
            return null;
        }

        Node node = instructions.get(selected);
        if (node instanceof BlockNode) {
            return ((BlockNode) node).getHead();
        } else {
            return (StepRecord) ((RecordNode) node).getRecord();
        }
    }

    public StepRecord getPreviousInstruction() {
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
            return (StepRecord) ((RecordNode) node).getRecord();
        }
    }

    protected class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            Node node = instructions.get(index);
            if (node instanceof BlockNode) {
                if (index > 0) {
                    BlockNode block = (BlockNode) node;
                    Node prev = instructions.get(index - 1);
                    StepRecord head = block.getHead();
                    long pc = head.getPC();
                    if (prev instanceof BlockNode) {
                        BlockNode b = (BlockNode) prev;
                        StepRecord step = b.getHead();
                        long npc = step.getPC() + (step.getMachinecode() != null ? step.getMachinecode().length : 0);
                        if (!isSelected && pc != npc) {
                            c.setBackground(ROP_BG);
                        }
                    }
                }
                c.setForeground(CALL_FG);
            } else if (node instanceof RecordNode) {
                StepRecord step = (StepRecord) ((RecordNode) node).getRecord();
                String mnemonic = step.getMnemonic();
                if (mnemonic == null) {
                    c.setForeground(ERROR_FG);
                } else {
                    switch (AMD64InstructionQuickInfo.getType(step.getMachinecode())) {
                        case RET:
                            c.setForeground(RET_FG);
                            break;
                        case SYSCALL:
                            c.setForeground(SYSCALL_FG);
                            break;
                        case JMP:
                            if (step.getInstruction() instanceof JmpIndirect) {
                                c.setForeground(INDIRECTJMP_FG);
                            } else {
                                c.setForeground(JMP_FG);
                            }
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
                    long pc = step.getPC();
                    if (prev instanceof BlockNode) {
                        BlockNode b = (BlockNode) prev;
                        StepRecord s = b.getHead();
                        long npc = s.getPC() + (s.getMachinecode() != null ? s.getMachinecode().length : 0);
                        if (!isSelected && pc != npc) {
                            c.setBackground(ROP_BG);
                        }
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
            if (n instanceof RecordNode) {
                StepRecord step = (StepRecord) ((RecordNode) n).getRecord();
                if (i + 1 < instructions.size()) {
                    StepRecord next;
                    Node nn = instructions.get(i + 1);
                    if (nn instanceof BlockNode) {
                        next = ((BlockNode) nn).getHead();
                    } else {
                        next = (StepRecord) ((RecordNode) nn).getRecord();
                    }
                    return format(step, next);
                } else {
                    return format(step, null);
                }
            } else if (n instanceof BlockNode) {
                StepRecord step = ((BlockNode) n).getHead();
                Location loc = Location.getLocation(trc, ((BlockNode) n).getFirstStep());
                StringBuilder buf = new StringBuilder();
                StepRecord next = null;
                if (i + 1 < instructions.size()) {
                    Node nn = instructions.get(i + 1);
                    if (nn instanceof BlockNode) {
                        next = ((BlockNode) nn).getHead();
                    } else {
                        next = (StepRecord) ((RecordNode) nn).getRecord();
                    }
                    buf.append(format(step, next));
                } else {
                    buf.append(format(step, null));
                }
                if (loc.getSymbol() != null) {
                    buf.append(" # <");
                    buf.append(loc.getSymbol());
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
                    CpuState ns = next == null ? null : next.getState().getState();
                    String decoded = CallDecoder.decode(fun, step.getState().getState(), ns, trc);
                    if (decoded != null) {
                        int length = buf.length();
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
                        buf.append(decoded);
                        buf.append("</span></pre></body></html>");
                    }
                }
                return buf.toString();
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
