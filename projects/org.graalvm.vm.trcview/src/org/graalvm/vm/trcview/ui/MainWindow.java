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
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.graalvm.vm.posix.elf.ElfStrings;
import org.graalvm.vm.trcview.analysis.Analysis;
import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.memory.VirtualMemorySnapshot;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.DerivedStepEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.TraceFileReader;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.TypeParser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.EventNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.io.TextSerializer;
import org.graalvm.vm.trcview.net.Client;
import org.graalvm.vm.trcview.net.Local;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.Watches.Watch;
import org.graalvm.vm.trcview.ui.plugin.UIPluginLoader;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.util.ui.MessageBox;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "TRCView";
    private static final String ABOUT_TEXT = "<html><body><i>TRCView - Interactive Execution Trace Analyzer</i><br/><br/>" +
                    "Supported architectures:<br/>" +
                    "<ul>%s</ul>" +
                    "Architecture support can be extended with plugins.</body></html>";

    private static final Logger log = Trace.create(MainWindow.class);

    public static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private JLabel status;
    private JLabel position;
    private TraceView view;

    private JMenuItem open;
    private JMenuItem loadPrototypes;
    private JMenuItem loadSymbols;
    private JMenuItem saveSymbols;
    private JMenuItem loadSession;
    private JMenuItem saveSession;
    private JMenuItem refresh;
    private JMenuItem renameSymbol;
    private JMenuItem setFunctionType;
    private JMenuItem setCommentInsn;
    private JMenuItem setCommentPC;
    private JMenuItem setExpression;
    private JMenuItem gotoPC;
    private JMenuItem gotoInsn;
    private JMenuItem gotoNext;
    private JMenuItem exportMemory;

    private TraceAnalyzer trc;

    private UIPluginLoader pluginLoader;

    public MainWindow() {
        this(null);
    }

    public MainWindow(MainWindow master) {
        super(WINDOW_TITLE);

        FileDialog load = new FileDialog(this, "Open...", FileDialog.LOAD);
        FileDialog loadSyms = new FileDialog(this, "Load symbols...", FileDialog.LOAD);
        FileDialog saveSyms = new FileDialog(this, "Save symbols...", FileDialog.SAVE);
        FileDialog loadSess = new FileDialog(this, "Load session...", FileDialog.LOAD);
        FileDialog saveSess = new FileDialog(this, "Save session...", FileDialog.SAVE);
        ExportMemoryDialog exportMemoryDialog = new ExportMemoryDialog(this);

        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, view = new TraceView(this::setStatus, this::setPosition));
        JPanel south = new JPanel(new BorderLayout());
        south.add(BorderLayout.CENTER, status = new JLabel("Ready"));
        south.add(BorderLayout.EAST, position = new JLabel(""));
        add(BorderLayout.SOUTH, south);

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        open = new JMenuItem("Open...");
        open.setMnemonic('O');
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        open.addActionListener(e -> {
            load.setVisible(true);
            if (load.getFile() == null) {
                return;
            }
            String filename = load.getDirectory() + load.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        load(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        loadSession = new JMenuItem("Load session...");
        loadSession.setMnemonic('l');
        loadSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        loadSession.addActionListener(e -> {
            loadSess.setVisible(true);
            if (loadSess.getFile() == null) {
                return;
            }
            String filename = loadSess.getDirectory() + loadSess.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        loadSession(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        loadSession.setEnabled(false);
        saveSession = new JMenuItem("Save session...");
        saveSession.setMnemonic('s');
        saveSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        saveSession.addActionListener(e -> {
            saveSess.setVisible(true);
            if (saveSess.getFile() == null) {
                return;
            }

            String filename = saveSess.getDirectory() + saveSess.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        saveSession(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        saveSession.setEnabled(false);
        loadPrototypes = new JMenuItem("Load prototypes...");
        loadPrototypes.setMnemonic('p');
        loadPrototypes.addActionListener(e -> {
            loadSyms.setVisible(true);
            if (loadSyms.getFile() == null) {
                return;
            }
            String filename = loadSyms.getDirectory() + loadSyms.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        loadPrototypes(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        loadPrototypes.setEnabled(false);
        loadSymbols = new JMenuItem("Load symbols...");
        loadSymbols.setMnemonic('m');
        loadSymbols.addActionListener(e -> {
            loadSyms.setVisible(true);
            if (loadSyms.getFile() == null) {
                return;
            }

            String filename = loadSyms.getDirectory() + loadSyms.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        loadSymbols(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        loadSymbols.setEnabled(false);
        saveSymbols = new JMenuItem("Save symbols...");
        saveSymbols.setMnemonic('y');
        saveSymbols.addActionListener(e -> {
            saveSyms.setVisible(true);
            if (saveSyms.getFile() == null) {
                return;
            }
            String filename = saveSyms.getDirectory() + saveSyms.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        saveSymbols(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        saveSymbols.setEnabled(false);
        refresh = new JMenuItem("Refresh");
        refresh.setMnemonic('r');
        refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        refresh.addActionListener(e -> {
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        trc.refresh();
                    } catch (Throwable ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        refresh.setEnabled(false);
        JMenuItem exit = new JMenuItem("Exit");
        exit.setMnemonic('x');
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));
        exit.addActionListener(e ->

        exit());

        if (master == null) {
            fileMenu.add(open);
            fileMenu.addSeparator();
            fileMenu.add(loadSession);
            fileMenu.add(saveSession);
            fileMenu.addSeparator();
            fileMenu.add(loadSymbols);
            fileMenu.add(saveSymbols);
            fileMenu.add(loadPrototypes);
            fileMenu.addSeparator();
            fileMenu.add(refresh);
            fileMenu.addSeparator();
            fileMenu.add(exit);
            fileMenu.setMnemonic('F');
        } else {
            JMenuItem close = new JMenuItem("Close");
            close.addActionListener(e -> this.dispose());
            close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
            fileMenu.add(close);

            // synchronize master view to slave view
            view.addChangeListener(() -> {
                Node node = view.getSelectedNode();
                if (node instanceof EventNode) {
                    Event event = ((EventNode) node).getEvent();
                    if (event instanceof DerivedStepEvent) {
                        DerivedStepEvent step = (DerivedStepEvent) event;
                        long id = step.getParentStep();
                        master.jump(id);
                    }
                } else if (node instanceof BlockNode) {
                    StepEvent event = ((BlockNode) node).getHead();
                    if (event != null && event instanceof DerivedStepEvent) {
                        DerivedStepEvent step = (DerivedStepEvent) event;
                        long id = step.getParentStep();
                        master.jump(id);
                    }
                }
            });
        }

        menu.add(fileMenu);

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('e');
        renameSymbol = new JMenuItem("Rename symbol...");
        renameSymbol.setMnemonic('n');
        renameSymbol.setAccelerator(KeyStroke.getKeyStroke('n'));
        renameSymbol.addActionListener(e -> {
            ComputedSymbol selected = view.getSelectedSymbol();
            if (selected == null) {
                return;
            }
            String input = JOptionPane.showInputDialog("Enter name:", selected.name);
            if (input != null && input.trim().length() > 0) {
                String name = input.trim();
                for (ComputedSymbol sym : trc.getSymbols()) {
                    if (sym != selected && sym.name.equals(name)) {
                        JOptionPane.showMessageDialog(this, "Error: symbol " + name + " already exists", "Rename symbol...", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                trc.renameSymbol(selected, name);
            }
        });
        renameSymbol.setEnabled(false);
        editMenu.add(renameSymbol);
        setFunctionType = new JMenuItem("Set function type...");
        setFunctionType.setMnemonic('y');
        setFunctionType.setAccelerator(KeyStroke.getKeyStroke('y'));
        setFunctionType.addActionListener(e -> {
            ComputedSymbol selected = view.getSelectedSymbol();
            if (selected == null) {
                return;
            }
            String prototype;
            if (selected.prototype != null) {
                prototype = new Function(selected.name, selected.prototype).toString();
            } else {
                prototype = "void " + selected.name + "()";
            }
            String input = JOptionPane.showInputDialog("Enter prototype:", prototype);
            if (input != null && input.trim().length() > 0) {
                try {
                    TypeParser parser = new TypeParser(input.trim());
                    Function fun = parser.parse();
                    String name = fun.getName();
                    for (ComputedSymbol sym : trc.getSymbols()) {
                        if (sym != selected && sym.name.equals(name)) {
                            JOptionPane.showMessageDialog(this, "Error: symbol " + name + " already exists", "Set function type...", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    if (!fun.getName().equals(selected.name)) {
                        trc.renameSymbol(selected, fun.getName());
                    }
                    trc.setPrototype(selected, fun.getPrototype());
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Set function type...", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else if (input != null) {
                trc.setPrototype(selected, null);
            }
        });
        setFunctionType.setEnabled(false);
        editMenu.add(setFunctionType);
        setCommentInsn = new JMenuItem("Set comment (instruction)...");
        setCommentInsn.setMnemonic('i');
        setCommentInsn.setAccelerator(KeyStroke.getKeyStroke('/'));
        setCommentInsn.addActionListener(e -> {
            StepEvent insn = view.getSelectedInstruction();
            if (insn == null) {
                return;
            }
            String comment = trc.getCommentForInsn(insn.getStep());
            if (comment != null) {
                String input = JOptionPane.showInputDialog("Enter comment:", comment);
                if (input != null && input.trim().length() > 0) {
                    trc.setCommentForInsn(insn.getStep(), input.trim());
                } else if (input != null) {
                    trc.setCommentForInsn(insn.getStep(), null);
                }
            } else {
                String input = JOptionPane.showInputDialog("Enter comment:");
                if (input != null && input.trim().length() > 0) {
                    trc.setCommentForInsn(insn.getStep(), input.trim());
                }
            }
        });
        setCommentInsn.setEnabled(false);
        editMenu.add(setCommentInsn);
        setCommentPC = new JMenuItem("Set comment (PC)...");
        setCommentPC.setMnemonic('p');
        setCommentPC.setAccelerator(KeyStroke.getKeyStroke(';'));
        setCommentPC.addActionListener(e -> {
            StepEvent insn = view.getSelectedInstruction();
            if (insn == null) {
                return;
            }
            String comment = trc.getCommentForPC(insn.getPC());
            if (comment != null) {
                String input = JOptionPane.showInputDialog("Enter comment:", comment);
                if (input != null && input.trim().length() > 0) {
                    trc.setCommentForPC(insn.getPC(), input.trim());
                } else if (input != null) {
                    trc.setCommentForPC(insn.getPC(), null);
                }
            } else {
                String input = JOptionPane.showInputDialog("Enter comment:");
                if (input != null && input.trim().length() > 0) {
                    trc.setCommentForPC(insn.getPC(), input.trim());
                }
            }
        });
        setCommentPC.setEnabled(false);
        editMenu.add(setCommentPC);
        setExpression = new JMenuItem("Set expression...");
        setExpression.setAccelerator(KeyStroke.getKeyStroke('@'));
        setExpression.addActionListener(e -> {
            StepEvent insn = view.getSelectedInstruction();
            if (insn == null) {
                return;
            }
            String expr = trc.getExpression(insn.getPC());
            if (expr != null) {
                String input = JOptionPane.showInputDialog("Enter expression:", expr);
                if (input != null && input.trim().length() > 0) {
                    try {
                        trc.setExpression(insn.getPC(), input.trim());
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Set expression...", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else if (input != null) {
                    try {
                        trc.setExpression(insn.getPC(), null);
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Set expression...", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } else {
                String input = JOptionPane.showInputDialog("Enter expression:");
                if (input != null && input.trim().length() > 0) {
                    try {
                        trc.setExpression(insn.getPC(), input.trim());
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Set expression...", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
        });
        setExpression.setEnabled(false);
        editMenu.add(setExpression);
        menu.add(editMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('v');
        gotoPC = new JMenuItem("Goto PC...");
        gotoPC.setMnemonic('g');
        gotoPC.setAccelerator(KeyStroke.getKeyStroke('g'));
        gotoPC.addActionListener(e -> {
            StepEvent step = view.getSelectedInstruction();
            String input;
            if (step != null) {
                long loc = step.getPC();
                input = JOptionPane.showInputDialog("Enter address:", HexFormatter.tohex(loc));
                if (input != null && input.trim().length() > 0) {
                    try {
                        long pc = Long.parseLong(input.trim(), 16);
                        Node n = trc.getNextPC(view.getSelectedNode(), pc);
                        if (n != null) {
                            log.info("Jumping to next occurence of PC=0x" + HexFormatter.tohex(pc));
                            view.jump(n);
                        } else {
                            JOptionPane.showMessageDialog(this, "Error: cannot find a next instruction at 0x" + HexFormatter.tohex(pc), "Goto...", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Error: invalid number", "Goto PC...", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                Optional<ComputedSymbol> first = trc.getSymbols().stream().sorted((a, b) -> Long.compareUnsigned(a.address, b.address)).findFirst();
                if (first.isPresent()) {
                    input = JOptionPane.showInputDialog("Enter address:", HexFormatter.tohex(first.get().address));
                } else {
                    input = JOptionPane.showInputDialog("Enter address:", "0");
                }
                if (input != null && input.trim().length() > 0) {
                    try {
                        long pc = Long.parseLong(input.trim(), 16);
                        Node n = trc.getNextPC(trc.getRoot(), pc);
                        if (n != null) {
                            log.info("Jumping to next occurence of PC=0x" + HexFormatter.tohex(pc));
                            view.jump(n);
                        } else {
                            JOptionPane.showMessageDialog(this, "Error: cannot find a next instruction at 0x" + HexFormatter.tohex(pc), "Goto...", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Error: invalid number", "Goto PC...", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        gotoPC.setEnabled(false);
        viewMenu.add(gotoPC);
        gotoInsn = new JMenuItem("Goto instruction...");
        gotoInsn.setMnemonic('i');
        gotoInsn.setAccelerator(KeyStroke.getKeyStroke('i'));
        gotoInsn.addActionListener(e -> {
            StepEvent step = view.getSelectedInstruction();
            long start = 0;
            if (step != null) {
                start = step.getStep();
            }
            String input = JOptionPane.showInputDialog("Enter instruction number:", Long.toUnsignedString(start));
            if (input != null && input.trim().length() > 0) {
                try {
                    long insn = Long.parseUnsignedLong(input.trim());
                    if (!jump(insn)) {
                        JOptionPane.showMessageDialog(this, "Error: cannot find instruction " + insn, "Goto...", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Error: invalid number", "Goto instruction...", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        gotoInsn.setEnabled(false);
        viewMenu.add(gotoInsn);
        gotoNext = new JMenuItem("Goto next");
        gotoNext.setMnemonic('n');
        gotoNext.setAccelerator(KeyStroke.getKeyStroke('c'));
        gotoNext.addActionListener(e -> {
            StepEvent step = view.getSelectedInstruction();
            if (step != null) {
                long pc = step.getPC();
                Node n = trc.getNextPC(view.getSelectedNode(), pc);
                if (n != null) {
                    log.info("Jumping to next occurence of PC=0x" + HexFormatter.tohex(pc));
                    view.jump(n);
                } else {
                    JOptionPane.showMessageDialog(this, "Error: cannot find a next instruction at 0x" + HexFormatter.tohex(pc), "Goto next", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        gotoNext.setEnabled(false);
        viewMenu.add(gotoNext);
        menu.add(viewMenu);

        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('t');

        exportMemory = new JMenuItem("Export memory region");
        exportMemory.setMnemonic('e');
        exportMemory.addActionListener(e -> {
            VirtualMemorySnapshot mem = view.getMemorySnapshot();
            if (mem == null) {
                return;
            }

            SwingUtilities.invokeLater(exportMemoryDialog::focus);
            exportMemoryDialog.setMemory(mem);
            exportMemoryDialog.setVisible(true);
        });
        exportMemory.setEnabled(false);
        toolsMenu.add(exportMemory);
        menu.add(toolsMenu);

        JMenu pluginMenu = new JMenu("Plugins");
        pluginLoader = new UIPluginLoader(this, pluginMenu, view);

        if (pluginMenu.getItemCount() > 0) {
            menu.add(pluginMenu);
        }

        JMenu helpMenu = new JMenu("Help");
        JMenuItem about = new JMenuItem("About...");
        about.setMnemonic('A');
        about.addActionListener(e -> {
            List<Architecture> archs = Architecture.getArchitectures();
            Collections.sort(archs, (a, b) -> ElfStrings.getElfMachine(a.getId()).compareTo(ElfStrings.getElfMachine(b.getId())));
            StringBuilder list = new StringBuilder();
            for (Architecture arch : archs) {
                String shortName = arch.getName();
                String longName = ElfStrings.getElfMachine(arch.getId());
                String description = arch.getDescription();
                String name = longName + " [" + shortName + "]";
                if (description != null) {
                    name += ": " + description;
                }
                list.append("<li>" + name.replaceAll("&", "&amp;").replaceAll("<", "&lt;") + "</li>");
            }
            String aboutText = String.format(ABOUT_TEXT, list);
            JOptionPane.showMessageDialog(this, aboutText, "About...", JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(about);
        helpMenu.setMnemonic('H');
        menu.add(helpMenu);

        setJMenuBar(menu);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 600);
    }

    public boolean jump(long insn) {
        StepEvent selected = view.getSelectedInstruction();
        if (selected != null && selected.getStep() == insn) {
            return true;
        }
        Node n = trc.getInstruction(insn);
        if (n != null) {
            log.log(Levels.DEBUG, "Jumping to instruction " + insn);
            view.jump(n);
            return true;
        } else {
            return false;
        }
    }

    public void setStatus(String text) {
        status.setText(text);
    }

    public void setPosition(long pos) {
        if (pos < 0) {
            position.setText("--");
        } else {
            double ratio = (double) pos / (double) trc.getInstructionCount();
            position.setText(Math.round(ratio * 100.0) + "%");
        }
    }

    public void load(File file) throws IOException {
        log.info("Loading file " + file + "...");
        open.setEnabled(false);
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            long size = file.length();
            load(new TraceFileReader(in), size, file.toString());
        } catch (Throwable t) {
            log.log(Level.INFO, "Loading failed: " + t, t);
            setStatus("Loading failed: " + t);
            throw t;
        } finally {
            open.setEnabled(true);
        }
        log.info("File loaded");
    }

    public void load(TraceReader reader, long size, String file) throws IOException {
        log.info("Loading file " + file + "...");
        open.setEnabled(false);
        try {
            String text = "Loading " + file;
            setStatus(text);
            setPosition(-1);
            List<Analyzer> analyzers = pluginLoader.getAnalyzers(reader.getArchitecture());
            Analysis analysis = new Analysis(reader.getArchitecture(), analyzers);
            analysis.start();
            BlockNode root = BlockNode.read(reader, analysis, pos -> setStatus(text + " (" + (pos * 100L / size) + "%)"));
            analysis.finish(root);
            if (root == null || root.getFirstStep() == null) {
                setStatus("Loading failed");
                return;
            }
            setStatus("Trace loaded");
            setTitle(file + " - " + WINDOW_TITLE);
            EventQueue.invokeLater(() -> {
                trc = new Local(reader.getArchitecture(), root, analysis);
                view.setTraceAnalyzer(trc);
                loadPrototypes.setEnabled(true);
                loadSymbols.setEnabled(true);
                saveSymbols.setEnabled(true);
                loadSession.setEnabled(true);
                saveSession.setEnabled(true);
                refresh.setEnabled(false);
                renameSymbol.setEnabled(true);
                setFunctionType.setEnabled(true);
                setCommentInsn.setEnabled(true);
                setCommentPC.setEnabled(true);
                setExpression.setEnabled(true);
                gotoPC.setEnabled(true);
                gotoInsn.setEnabled(true);
                gotoNext.setEnabled(true);
                exportMemory.setEnabled(true);
                pluginLoader.traceLoaded(trc);
            });
        } catch (Throwable t) {
            log.log(Level.INFO, "Loading failed: " + t, t);
            setStatus("Loading failed: " + t);
            throw t;
        } finally {
            open.setEnabled(true);
        }
    }

    public void loadPrototypes(File file) throws IOException {
        log.info("Loading prototype file " + file + "...");
        setStatus("Loading prototype file " + file + "...");
        boolean ok = true;
        loadPrototypes.setEnabled(false);
        Map<String, List<ComputedSymbol>> syms = trc.getNamedSymbols();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            int lineno = 0;
            while ((line = in.readLine()) != null) {
                lineno++;
                if (line.contains("//")) {
                    line = line.substring(0, line.indexOf("//"));
                }
                line = line.trim();
                if (line.endsWith(";")) {
                    line = line.substring(0, line.length() - 1);
                }
                if (line.length() > 0) {
                    try {
                        TypeParser parser = new TypeParser(line);
                        Function fun = parser.parse();
                        List<ComputedSymbol> sym = syms.get(fun.getName());
                        if (sym != null) {
                            for (ComputedSymbol sy : sym) {
                                trc.setPrototype(sy, fun.getPrototype());
                            }
                        }
                    } catch (ParseException e) {
                        log.info("Parse error in line " + lineno + ": " + e.getMessage());
                        setStatus("Parse error in line " + lineno + ": " + e.getMessage());
                        ok = false;
                    }
                }
            }
            log.info("Prototypes loaded from file " + file);
            if (ok) {
                setStatus("Prototypes loaded from file " + file);
            }
        } catch (Throwable t) {
            log.log(Level.WARNING, "Loading failed: " + t, t);
            setStatus("Loading failed: " + t);
            throw t;
        } finally {
            loadPrototypes.setEnabled(true);
        }
        log.info("Prototype file loaded");
    }

    public void loadSymbols(File file) throws IOException {
        log.info("Loading symbol file " + file + "...");
        setStatus("Loading symbol file " + file + "...");
        loadSymbols.setEnabled(false);
        try {
            loadSession(file, false);
        } finally {
            loadSymbols.setEnabled(true);
        }
    }

    public void saveSymbols(File file) throws IOException {
        log.info("Saving symbols to file " + file + "...");
        setStatus("Saving symbols to file " + file + "...");
        saveSymbols.setEnabled(false);
        try {
            saveSession(file, false);
        } finally {
            saveSymbols.setEnabled(true);
        }
    }

    private boolean loadSession(File file, boolean everything) throws IOException {
        boolean reanalyze = false;
        boolean ok = true;
        List<Watch> watches = new ArrayList<>();
        long insn = -1;
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            int lineno = 0;
            while ((line = in.readLine()) != null) {
                int comment = line.indexOf('#');
                if (comment > 0) {
                    line = line.substring(0, comment).trim();
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                lineno++;
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String address = line.substring(0, idx);
                    String[] data = TextSerializer.tokenize(line.substring(idx + 1));
                    if (everything && address.equals("WATCH")) {
                        // watch point
                        if (data.length != 3) {
                            log.info("Syntax error in line " + lineno + ": invalid watch point");
                            setStatus("Syntax error in line " + lineno + ": invalid watch point");
                            ok = false;
                            continue;
                        }
                        String name = data[0];
                        String format = data[1];
                        String str = data[2];
                        Expression expr = null;
                        try {
                            expr = new Parser(str).parse();
                        } catch (ParseException e) {
                            log.info("Syntax error in line " + lineno + ": " + e.getMessage());
                            setStatus("Syntax error in line " + lineno + ": " + e.getMessage());
                            ok = false;
                            continue;
                        }
                        watches.add(new Watch(name, format, str, expr));
                    } else if (everything && address.equals("INSN")) {
                        // watch point
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid location");
                            setStatus("Syntax error in line " + lineno + ": invalid location");
                            ok = false;
                            continue;
                        }
                        try {
                            insn = Long.parseUnsignedLong(data[0]);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid instruction");
                            setStatus("Syntax error in line " + lineno + ": invalid instruction");
                            ok = false;
                            continue;
                        }
                    } else if (everything && address.startsWith("COMMENTINSN:")) {
                        // comment for insn#
                        long step;
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid comment");
                            setStatus("Syntax error in line " + lineno + ": invalid comment");
                            ok = false;
                            continue;
                        }
                        try {
                            step = Long.parseUnsignedLong(address.substring(12), 16);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid instruction");
                            setStatus("Syntax error in line " + lineno + ": invalid instruction");
                            ok = false;
                            continue;
                        }
                        trc.setCommentForInsn(step, data[0]);
                    } else if (everything && address.startsWith("COMMENTPC:")) {
                        // comment for PC
                        long pc;
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid comment");
                            setStatus("Syntax error in line " + lineno + ": invalid comment");
                            ok = false;
                            continue;
                        }
                        try {
                            pc = Long.parseUnsignedLong(address.substring(10), 16);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid program counter");
                            setStatus("Syntax error in line " + lineno + ": invalid program counter");
                            ok = false;
                            continue;
                        }
                        trc.setCommentForPC(pc, data[0]);
                    } else if (everything && address.startsWith("EXPR:")) {
                        // expression
                        long pc;
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid expression");
                            setStatus("Syntax error in line " + lineno + ": invalid expression");
                            ok = false;
                            continue;
                        }
                        try {
                            pc = Long.parseUnsignedLong(address.substring(5), 16);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid program counter");
                            setStatus("Syntax error in line " + lineno + ": invalid program counter");
                            ok = false;
                            continue;
                        }
                        try {
                            trc.setExpression(pc, data[0]);
                        } catch (ParseException e) {
                            log.info("Syntax error in line " + lineno + ": " + e.getMessage());
                            setStatus("Syntax error in line " + lineno + ": " + e.getMessage());
                            ok = false;
                            continue;
                        }
                    } else {
                        // symbol
                        long pc;
                        try {
                            pc = Long.parseUnsignedLong(address, 16);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid address");
                            setStatus("Syntax error in line " + lineno + ": invalid address");
                            ok = false;
                            continue;
                        }
                        if (data.length == 1) {
                            ComputedSymbol sym = trc.getComputedSymbol(pc);
                            if (sym == null) {
                                trc.addSubroutine(pc, data[0], null);
                                reanalyze = true;
                            } else {
                                trc.renameSymbol(sym, data[0]);
                            }
                        } else if (data.length == 3) {
                            String proto = data[1] + " f(" + data[2] + ")";
                            Function fun;
                            try {
                                fun = new TypeParser(proto).parse();
                            } catch (ParseException e) {
                                log.info("Syntax error in line " + lineno + ": " + e.getMessage());
                                setStatus("Syntax error in line " + lineno + ": " + e.getMessage());
                                ok = false;
                                continue;
                            }
                            ComputedSymbol sym = trc.getComputedSymbol(pc);
                            if (sym == null) {
                                trc.addSubroutine(pc, data[0], fun.getPrototype());
                                reanalyze = true;
                            } else {
                                trc.renameSymbol(sym, data[0]);
                                trc.setPrototype(sym, fun.getPrototype());
                            }
                        } else {
                            log.info("Syntax error in line " + lineno + ": invalid name");
                            setStatus("Syntax error in line " + lineno + ": invalid name");
                            ok = false;
                        }
                    }
                } else {
                    log.info("Syntax error in line " + lineno + ": missing '='");
                    setStatus("Syntax error in line " + lineno + ": missing '='");
                    ok = false;
                }
            }
            if (reanalyze) {
                reanalyze = false;
                log.info("Reanalyzing visits...");
                setStatus("Reanalyzing visits...");
                trc.reanalyze();
            }
            String filetype = "Session file";
            if (!everything) {
                filetype = "Symbol file";
            } else {
                SwingUtilities.invokeLater(() -> view.setWatches(watches));
            }
            log.info(filetype + " " + file + " loaded successfully");
            if (ok) {
                setStatus(filetype + " " + file + " loaded successfully");
            }
            return ok;
        } catch (Throwable t) {
            log.log(Level.WARNING, "Loading failed: " + t, t);
            setStatus("Loading failed: " + t);
            throw t;
        } finally {
            if (reanalyze) {
                log.info("Reanalyzing symbol visits...");
                trc.reanalyze();
            }

            if (insn != -1) {
                Node n = trc.getInstruction(insn);
                if (n != null) {
                    log.info("Jumping to instruction " + insn);
                    SwingUtilities.invokeLater(() -> view.jump(n));
                } else {
                    log.info("Cannot find instruction");
                    setStatus("Cannot find instruction");
                }
            }
        }
    }

    public void loadSession(File file) throws IOException {
        log.info("Loading session file " + file + "...");
        setStatus("Loading session file " + file + "...");
        loadSession.setEnabled(false);
        try {
            loadSession(file, true);
        } finally {
            loadSession.setEnabled(true);
        }
    }

    public void saveSession(File file) throws IOException {
        log.info("Saving session to file " + file + "...");
        setStatus("Saving session to file " + file + "...");
        saveSession.setEnabled(false);
        try {
            saveSession(file, true);
        } finally {
            saveSession.setEnabled(true);
        }
    }

    public void saveSession(File file, boolean everything) throws IOException {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            Set<ComputedSymbol> symbols = trc.getSubroutines();
            symbols.stream().sorted((a, b) -> Long.compareUnsigned(a.address, b.address)).forEach(sym -> {
                if (sym.prototype != null) {
                    out.printf("%x=%s\n", sym.address,
                                    TextSerializer.encode(sym.name, sym.prototype.returnType.toString(), sym.prototype.args.stream().map(Object::toString).collect(Collectors.joining(", "))));
                } else {
                    out.printf("%x=%s\n", sym.address, TextSerializer.encode(sym.name));
                }
            });
            if (everything) {
                List<Watch> watches = view.getWatches();
                for (Watch watch : watches) {
                    out.printf("WATCH=%s\n", TextSerializer.encode(watch.name, watch.type, watch.str));
                }
                Map<Long, String> commentsInsn = trc.getCommentsForInsns();
                for (Entry<Long, String> comment : commentsInsn.entrySet()) {
                    out.printf("COMMENTINSN:%x=%s\n", comment.getKey(), TextSerializer.encode(comment.getValue()));
                }
                Map<Long, String> commentsPC = trc.getCommentsForPCs();
                for (Entry<Long, String> comment : commentsPC.entrySet()) {
                    out.printf("COMMENTPC:%x=%s\n", comment.getKey(), TextSerializer.encode(comment.getValue()));
                }
                Map<Long, String> expressions = trc.getExpressions();
                for (Entry<Long, String> expr : expressions.entrySet()) {
                    out.printf("EXPR:%x=%s\n", expr.getKey(), TextSerializer.encode(expr.getValue()));
                }
                StepEvent step = view.getSelectedInstruction();
                if (step != null) {
                    out.printf("INSN=%d\n", step.getStep());
                }
            }
            log.info("Session file " + file);
            setStatus("Session saved to file " + file);
        } catch (Throwable t) {
            log.log(Level.WARNING, "Saving failed: " + t, t);
            setStatus("Saving failed: " + t);
            throw t;
        }
    }

    public void connect(String host, int port) throws IOException {
        log.info("Connecting to " + host + " on port " + port + "...");
        setStatus("Connecting to " + host + " on port " + port + "...");
        try {
            trc = new Client(host, port);
            log.info("Connected to " + host + " on port " + port);
            setStatus("Connected to " + host + " on port " + port);
            setTitle(host + ":" + port + " - " + WINDOW_TITLE);
            EventQueue.invokeLater(() -> {
                view.setTraceAnalyzer(trc);
                loadPrototypes.setEnabled(true);
                loadSymbols.setEnabled(true);
                saveSymbols.setEnabled(true);
                refresh.setEnabled(true);
                renameSymbol.setEnabled(true);
                setFunctionType.setEnabled(true);
                setCommentInsn.setEnabled(true);
                setCommentPC.setEnabled(true);
                setExpression.setEnabled(true);
                gotoPC.setEnabled(true);
                gotoInsn.setEnabled(true);
                gotoNext.setEnabled(true);
                exportMemory.setEnabled(true);
                pluginLoader.traceLoaded(trc);
            });
        } catch (Throwable t) {
            setStatus("Failed to connect to " + host + " on port " + port);
            log.info("Failed to connect to " + host + " on port " + port);
            throw t;
        }
    }

    private void exit() {
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        Trace.setup();
        MainWindow w = new MainWindow();
        w.setVisible(true);
        if (args.length == 1) {
            try {
                w.load(new File(args[0]));
            } catch (Throwable t) {
                System.out.println("Failed to load the file specified by the argument");
            }
        } else if (args.length == 2) {
            try {
                w.connect(args[0], Integer.parseInt(args[1]));
            } catch (Throwable t) {
                System.out.println("Failed to connect to the specified server");
            }
        }
    }
}
