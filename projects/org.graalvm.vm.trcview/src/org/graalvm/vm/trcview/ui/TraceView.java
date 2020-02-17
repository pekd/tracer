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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.memory.VirtualMemorySnapshot;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.Watches.Watch;
import org.graalvm.vm.trcview.ui.event.CallListener;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

@SuppressWarnings("serial")
public class TraceView extends JPanel {
    private static final Logger log = Trace.create(TraceView.class);

    private SymbolView symbols;
    private CallStackView stack;
    private StateView state;
    private InstructionView insns;
    private MemoryView mem;
    private MemoryHistoryView memhistory;
    private StraceView strace;
    private Watches watches;

    private ComputedSymbol selectedSymbol;

    private TraceAnalyzer trc;

    private final List<ChangeListener> changeListeners;

    public TraceView(Consumer<String> status, Consumer<Long> position) {
        super(new BorderLayout());
        changeListeners = new ArrayList<>();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("strace", strace = new StraceView(this::jump));
        tabs.addTab("writes", memhistory = new MemoryHistoryView(status));

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setTopComponent(state = new StateView());
        JSplitPane rightBottomSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightBottomSplit.setTopComponent(mem = new MemoryView(status, this::jump));
        rightBottomSplit.setBottomComponent(watches = new Watches(status));
        rightBottomSplit.setResizeWeight(0.5);
        rightSplit.setBottomComponent(rightBottomSplit);
        rightSplit.setResizeWeight(0.5);
        JSplitPane content = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        center.setTopComponent(insns = new InstructionView(status, position));
        center.setBottomComponent(tabs);
        center.setResizeWeight(0.75);
        content.setLeftComponent(center);
        content.setRightComponent(rightSplit);
        content.setResizeWeight(1.0);
        content.setDividerLocation(400);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplit.setTopComponent(symbols = new SymbolView());
        leftSplit.setBottomComponent(stack = new CallStackView());
        leftSplit.setResizeWeight(0.65);
        split.setLeftComponent(leftSplit);
        split.setRightComponent(content);
        add(BorderLayout.CENTER, split);

        symbols.addJumpListener(this::jump);
        symbols.addChangeListener(() -> {
            selectedSymbol = symbols.getSelectedSymbol();
        });
        symbols.addClickListener(() -> {
            selectedSymbol = symbols.getSelectedSymbol();
        });

        insns.addChangeListener(() -> {
            selectedSymbol = null;

            StepEvent step = insns.getSelectedInstruction();
            StepEvent previous = insns.getPreviousInstruction();
            if (step != null) {
                if (previous != null) {
                    state.setState(previous, step);
                } else {
                    state.setState(step);
                }
                mem.setStep(step);
                memhistory.setStep(step);
                strace.setStep(step);
                watches.setStep(step);
            }
            if (insns.getSelectedNode() instanceof BlockNode) {
                BlockNode block = (BlockNode) insns.getSelectedNode();
                StepEvent first = block.getFirstStep();
                if (first != null) {
                    long pc = first.getPC();
                    ComputedSymbol sym = trc.getComputedSymbol(pc);
                    if (sym != null) {
                        selectedSymbol = sym;
                    }
                }
            }
            fireChangeEvent();
        });

        insns.addCallListener(new CallListener() {
            public void call(BlockNode call) {
                BlockNode node = trc.getChildren(call);
                stack.set(node);
                insns.set(node);
                insns.select(node.getFirstNode());
                mem.setStep(node.getFirstStep());
                memhistory.setStep(node.getFirstStep());
                strace.setStep(node.getFirstStep());
                watches.setStep(node.getFirstStep());
            }

            public void ret(Event ret) {
                BlockNode par = trc.getParent(ret);
                BlockNode parent = trc.getParent(par);
                if (parent != null) {
                    parent = trc.getChildren(parent);
                    stack.set(parent);
                    insns.set(parent);
                    insns.select(par);
                    mem.setStep(par.getHead());
                    memhistory.setStep(par.getHead());
                    strace.setStep(par.getHead());
                    watches.setStep(par.getHead());
                }
            }
        });

        stack.addLevelUpListener(this::up);
        stack.addLevelPeekListener(this::peek);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        getActionMap().put(esc, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                BlockNode block = stack.get();
                if (block != null) {
                    up(block);
                }
            }
        });
    }

    public ComputedSymbol getSelectedSymbol() {
        return selectedSymbol;
    }

    public void jump(Node node) {
        BlockNode block = trc.getParent(node);
        if (block == null) {
            block = trc.getRoot();
        }
        block = trc.getChildren(block);
        stack.set(block);
        insns.set(block);
        insns.select(node);
        insns.fireChangeEvent();
    }

    private void up(BlockNode block) {
        BlockNode parent = trc.getParent(block);
        if (parent != null) {
            parent = trc.getChildren(parent);
            stack.set(parent);
            insns.set(parent);
            insns.select(block);
        }
    }

    private void peek(BlockNode block, BlockNode select) {
        insns.set(block);
        if (select != null) {
            insns.select(select);
        } else {
            insns.select(block.getFirstNode());
        }
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        stack.setTraceAnalyzer(trc);
        insns.setTraceAnalyzer(trc);
        state.setTraceAnalyzer(trc);
        mem.setTraceAnalyzer(trc);
        memhistory.setTraceAnalyzer(trc);
        strace.setTraceAnalyzer(trc);
        watches.setTraceAnalyzer(trc);
        symbols.setTraceAnalyzer(trc);

        trc.addSymbolRenameListener((sym) -> insns.repaint());
        trc.addCommentChangeListener(() -> insns.repaint());

        showRoot(trc.getRoot());
    }

    private void showRoot(BlockNode root) {
        stack.set(root);
        insns.set(root);
        insns.select(root.getFirstNode());
        state.setState(root.getFirstStep());
        mem.setStep(root.getFirstStep());
        memhistory.setStep(root.getFirstStep());
        strace.setStep(root.getFirstStep());
        watches.setStep(root.getFirstStep());
    }

    public Node getSelectedNode() {
        return insns.getSelectedNode();
    }

    public StepEvent getSelectedInstruction() {
        return insns.getSelectedInstruction();
    }

    public VirtualMemorySnapshot getMemorySnapshot() {
        StepEvent selected = getSelectedInstruction();
        if (selected != null && trc != null) {
            return new VirtualMemorySnapshot(trc, selected.getStep());
        } else {
            return null;
        }
    }

    public String getMemoryExpression() {
        return mem.getExpression();
    }

    public void setMemoryExpression(String expr) {
        mem.setExpression(expr);
    }

    public List<Watch> getWatches() {
        return watches.getWatches();
    }

    public void setWatches(List<Watch> watches) {
        this.watches.setWatches(watches);
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    protected void fireChangeEvent() {
        for (ChangeListener l : changeListeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.log(Levels.WARNING, "Failed to run change listener: " + t, t);
            }
        }
    }
}
