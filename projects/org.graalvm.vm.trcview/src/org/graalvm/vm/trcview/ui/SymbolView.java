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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.ChangeListener;
import org.graalvm.vm.trcview.ui.event.JumpListener;
import org.graalvm.vm.util.StringUtils;
import org.graalvm.vm.util.log.Trace;

@SuppressWarnings("serial")
public class SymbolView extends JPanel {
    private static final Logger log = Trace.create(SymbolView.class);

    private JList<String> syms;
    private List<ComputedSymbol> symbols;
    private List<JumpListener> jumpListeners;
    private List<ChangeListener> changeListeners;
    private List<ChangeListener> clickListeners;
    private TraceAnalyzer trc;
    private StepFormat format;
    private int width;

    public SymbolView() {
        super(new BorderLayout());
        jumpListeners = new ArrayList<>();
        changeListeners = new ArrayList<>();
        clickListeners = new ArrayList<>();
        symbols = Collections.emptyList();
        syms = new JList<>(new DefaultListModel<>());
        syms.setFont(MainWindow.FONT);
        syms.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fireClickEvent();
                if (e.getClickCount() == 2) {
                    int i = syms.getSelectedIndex();
                    if (i == -1) {
                        return;
                    }
                    ComputedSymbol sym = symbols.get(i);
                    log.info("jumping to first execution of " + sym.name);
                    fireJumpEvent(trc.getNode(sym.visits.get(0)));
                }
            }
        });
        syms.addListSelectionListener(e -> {
            int selected = syms.getSelectedIndex();
            if (selected == -1) {
                return;
            }
            fireChangeEvent();
        });
        add(BorderLayout.CENTER, new JScrollPane(syms));
    }

    public ComputedSymbol getSelectedSymbol() {
        int selected = syms.getSelectedIndex();
        if (selected != -1) {
            return symbols.get(selected);
        } else {
            return null;
        }
    }

    public void symbolRenamed(ComputedSymbol sym) {
        for (int i = 0; i < symbols.size(); i++) {
            ComputedSymbol s = symbols.get(i);
            if (s.address == sym.address) {
                symbols.set(i, sym);
                ((DefaultListModel<String>) syms.getModel()).set(i, format(s));
                return;
            }
        }
    }

    private final String format(ComputedSymbol sym) {
        int len = 32 - sym.name.length();
        if (len < 1) {
            len = 1;
        }
        String cnt = Integer.toString(sym.visits.size());
        if (cnt.length() < width) {
            cnt = StringUtils.repeat(" ", width - cnt.length()) + cnt;
        }
        return format.formatAddress(sym.address) + " [" + cnt + "] " + sym.name;
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        format = trc.getArchitecture().getFormat();
        trc.addSymbolRenameListener(this::symbolRenamed);
        trc.addSymbolChangeListener(this::update);
        update();
    }

    public void update() {
        List<ComputedSymbol> sym = new ArrayList<>();
        DefaultListModel<String> model = new DefaultListModel<>();
        Collection<ComputedSymbol> subroutines = trc.getSubroutines();
        OptionalInt max = subroutines.stream().mapToInt(s -> s.visits.size()).max();
        if (max.isPresent()) {
            int m = max.getAsInt();
            width = (m == 0 ? 0 : (int) Math.ceil(Math.log10(m)));
            subroutines.stream().sorted((a, b) -> Long.compareUnsigned(a.address, b.address)).forEach(s -> {
                model.addElement(format(s));
                sym.add(s);
            });
        }
        syms.setModel(model);
        symbols = sym;
    }

    public void addJumpListener(JumpListener listener) {
        jumpListeners.add(listener);
    }

    public void removeJumpListener(JumpListener listener) {
        jumpListeners.remove(listener);
    }

    protected void fireJumpEvent(Node node) {
        for (JumpListener l : jumpListeners) {
            try {
                l.jump(node);
            } catch (Throwable t) {
                log.log(Level.WARNING, "Error while running listener: " + t, t);
            }
        }
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

    public void addClickListener(ChangeListener listener) {
        clickListeners.add(listener);
    }

    public void removeClickListener(ChangeListener listener) {
        clickListeners.remove(listener);
    }

    protected void fireClickEvent() {
        for (ChangeListener l : clickListeners) {
            try {
                l.valueChanged();
            } catch (Throwable t) {
                log.log(Level.WARNING, "Error while running listener: " + t, t);
            }
        }
    }
}
