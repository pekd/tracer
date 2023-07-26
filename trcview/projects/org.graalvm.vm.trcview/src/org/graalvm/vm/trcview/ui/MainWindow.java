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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JCheckBoxMenuItem;
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
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Field;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.NameAlreadyUsedException;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.UserDefinedType;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.io.DerivedStepEvent;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.arch.io.TraceFileReader;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.arch.vm.VMArchitecture;
import org.graalvm.vm.trcview.arch.vm.analysis.VMAnalyzer;
import org.graalvm.vm.trcview.arch.vm.analysis.VMTransformer;
import org.graalvm.vm.trcview.data.TypedMemory;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.decode.ABI;
import org.graalvm.vm.trcview.decode.DecoderUtils;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.io.ABISerializer;
import org.graalvm.vm.trcview.io.BlockNode;
import org.graalvm.vm.trcview.io.Node;
import org.graalvm.vm.trcview.io.TextSerializer;
import org.graalvm.vm.trcview.io.TraceParser;
import org.graalvm.vm.trcview.net.Local;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.TraceView.ThreadID;
import org.graalvm.vm.trcview.ui.Watches.Watch;
import org.graalvm.vm.trcview.ui.call.ABIEditor;
import org.graalvm.vm.trcview.ui.data.DataDialog;
import org.graalvm.vm.trcview.ui.data.DatatypeDialog;
import org.graalvm.vm.trcview.ui.data.TypeRecoveryDialog;
import org.graalvm.vm.trcview.ui.device.DeviceDialog;
import org.graalvm.vm.trcview.ui.event.TraceListenable;
import org.graalvm.vm.trcview.ui.event.TraceListener;
import org.graalvm.vm.trcview.ui.help.HelpBrowser;
import org.graalvm.vm.trcview.ui.plugin.UIPluginLoader;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.graalvm.vm.util.ui.MessageBox;

@SuppressWarnings("serial")
public class MainWindow extends JFrame implements TraceListenable {
    private static final String WINDOW_TITLE = "TRCView";
    private static final String ABOUT_TEXT = "<html><body><i>TRCView - Interactive Execution Trace Analyzer</i><br/><br/>" +
                    "Supported architectures:<br/>" +
                    "<ul>%s</ul>" +
                    "Architecture support can be extended with plugins.</body></html>";

    private static final Logger log = Trace.create(MainWindow.class);

    private static final int MENU_SHORTCUT_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

    public static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private JLabel status;
    private JLabel position;
    private TraceView view;

    private JMenuItem open;
    private JMenuItem loadPrototypes;
    private JMenuItem loadMap;
    private JMenuItem loadIdaMap;
    private JMenuItem generateIDC;
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
    private JMenuItem setColor;
    private JMenuItem gotoPC;
    private JMenuItem gotoInsn;
    private JMenuItem gotoNext;
    private JMenuItem exportMemory;
    private JCheckBoxMenuItem typeRecovery;
    private JCheckBoxMenuItem codeAnalysis;
    private JCheckBoxMenuItem useSymbols;
    private JCheckBoxMenuItem autoComment;
    private JMenu subviewMenu;

    private TraceAnalyzer trc;

    private UIPluginLoader pluginLoader;

    private List<TraceListener> traceListeners;

    private List<Constructor<? extends VMTransformer>> vmTransformers;

    public MainWindow() {
        this(null);
    }

    public MainWindow(MainWindow master) {
        super(WINDOW_TITLE);

        traceListeners = new ArrayList<>();
        vmTransformers = new ArrayList<>();

        FileDialog load = new FileDialog(this, "Open...", FileDialog.LOAD);
        FileDialog loadSyms = new FileDialog(this, "Load symbols...", FileDialog.LOAD);
        FileDialog saveSyms = new FileDialog(this, "Save symbols...", FileDialog.SAVE);
        FileDialog genIDC = new FileDialog(this, "Generate IDC...", FileDialog.SAVE);
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
        fileMenu.setMnemonic('F');

        open = new JMenuItem("Open...");
        open.setMnemonic('O');
        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_SHORTCUT_MASK));
        open.addActionListener(e -> {
            load.setVisible(true);
            if (load.getFile() == null) {
                return;
            }
            String filename = load.getDirectory() + load.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
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
        loadSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MENU_SHORTCUT_MASK));
        loadSession.addActionListener(e -> {
            loadSess.setVisible(true);
            if (loadSess.getFile() == null) {
                return;
            }
            String filename = loadSess.getDirectory() + loadSess.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {

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
        saveSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_SHORTCUT_MASK));
        saveSession.addActionListener(e -> {
            saveSess.setVisible(true);
            if (saveSess.getFile() == null) {
                return;
            }

            String filename = saveSess.getDirectory() + saveSess.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
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
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
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
        loadMap = new JMenuItem("Load map (binutils format)...");
        loadMap.setMnemonic('m');
        loadMap.addActionListener(e -> {
            loadSyms.setVisible(true);
            if (loadSyms.getFile() == null) {
                return;
            }
            String filename = loadSyms.getDirectory() + loadSyms.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        loadMap(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        loadMap.setEnabled(false);
        loadIdaMap = new JMenuItem("Load map (IDA format)...");
        loadIdaMap.setMnemonic('i');
        loadIdaMap.addActionListener(e -> {
            loadSyms.setVisible(true);
            if (loadSyms.getFile() == null) {
                return;
            }
            String filename = loadSyms.getDirectory() + loadSyms.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        loadIdaMap(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        loadIdaMap.setEnabled(false);
        generateIDC = new JMenuItem("Generate IDC...");
        generateIDC.setMnemonic('i');
        generateIDC.addActionListener(e -> {
            genIDC.setVisible(true);
            if (genIDC.getFile() == null) {
                return;
            }
            String filename = genIDC.getDirectory() + genIDC.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        generateIDC(new File(filename));
                    } catch (IOException ex) {
                        MessageBox.showError(MainWindow.this, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        generateIDC.setEnabled(false);
        loadSymbols = new JMenuItem("Load symbols...");
        loadSymbols.setMnemonic('m');
        loadSymbols.addActionListener(e -> {
            loadSyms.setVisible(true);
            if (loadSyms.getFile() == null) {
                return;
            }

            String filename = loadSyms.getDirectory() + loadSyms.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
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
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
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
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
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
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_SHORTCUT_MASK));
        exit.addActionListener(e -> exit());

        if (master == null) {
            fileMenu.add(open);
            // fileMenu.add(openDatabase);
            fileMenu.addSeparator();
            fileMenu.add(loadSession);
            fileMenu.add(saveSession);
            fileMenu.addSeparator();
            fileMenu.add(loadSymbols);
            fileMenu.add(saveSymbols);
            fileMenu.addSeparator();
            fileMenu.add(loadPrototypes);
            fileMenu.add(loadMap);
            fileMenu.add(loadIdaMap);
            fileMenu.addSeparator();
            fileMenu.add(generateIDC);
            fileMenu.addSeparator();
            fileMenu.add(refresh);
            fileMenu.addSeparator();
            fileMenu.add(exit);
        } else {
            JMenuItem close = new JMenuItem("Close");
            close.addActionListener(e -> this.dispose());
            close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, MENU_SHORTCUT_MASK));

            fileMenu.add(loadSession);
            fileMenu.add(saveSession);
            fileMenu.addSeparator();
            fileMenu.add(loadSymbols);
            fileMenu.add(saveSymbols);
            fileMenu.addSeparator();
            fileMenu.add(loadPrototypes);
            fileMenu.add(loadMap);
            fileMenu.add(loadIdaMap);
            fileMenu.addSeparator();
            fileMenu.add(generateIDC);
            fileMenu.addSeparator();
            fileMenu.add(close);

            // synchronize master view to slave view
            view.addChangeListener(() -> {
                Node node = view.getSelectedNode();
                if (node instanceof Event) {
                    Event event = (Event) node;
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
            Utils.rename(selected, trc, this);
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
            Utils.setFunctionType(selected, trc, this);
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
            Utils.setCommentPC(insn.getPC(), trc);
        });
        setCommentPC.setEnabled(false);
        editMenu.add(setCommentPC);
        setExpression = new JMenuItem("Set expression...");
        setExpression.setMnemonic('e');
        setExpression.setAccelerator(KeyStroke.getKeyStroke('@'));
        setExpression.addActionListener(e -> {
            StepEvent insn = view.getSelectedInstruction();
            if (insn == null) {
                return;
            }
            Utils.setExpression(insn.getPC(), trc, this);
        });
        setExpression.setEnabled(false);
        editMenu.add(setExpression);
        setColor = new JMenuItem("Set color...");
        setColor.setMnemonic('c');
        setColor.setAccelerator(KeyStroke.getKeyStroke('h'));
        setColor.addActionListener(e -> {
            StepEvent insn = view.getSelectedInstruction();
            if (insn == null) {
                return;
            }
            ColorPicker picker = new ColorPicker(this);
            picker.setVisible(true);
            trc.setColor(insn.getPC(), picker.getColor());
        });
        setColor.setEnabled(false);
        editMenu.add(setColor);
        menu.add(editMenu);

        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('v');

        subviewMenu = new JMenu("Open subview");
        subviewMenu.setMnemonic('s');
        JMenuItem deviceWindow = new JMenuItem("Devices");
        deviceWindow.setMnemonic('D');
        deviceWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK));
        deviceWindow.addActionListener(e -> {
            DeviceDialog dlg = new DeviceDialog(this, trc, view::jump, view, this);
            dlg.setVisible(true);
        });
        JMenuItem datatypeWindow = new JMenuItem("Datatypes");
        datatypeWindow.setMnemonic('a');
        datatypeWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.SHIFT_DOWN_MASK));
        datatypeWindow.addActionListener(e -> {
            DatatypeDialog dlg = new DatatypeDialog(this, trc);
            dlg.setVisible(true);
        });
        JMenuItem abiWindow = new JMenuItem("ABI");
        abiWindow.setMnemonic('i');
        abiWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_DOWN_MASK));
        abiWindow.addActionListener(e -> {
            ABIEditor dlg = new ABIEditor(this, trc);
            dlg.setVisible(true);
        });
        JMenuItem dataWindow = new JMenuItem("Data");
        dataWindow.setMnemonic('i');
        dataWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_DOWN_MASK));
        dataWindow.addActionListener(e -> {
            DataDialog dlg = new DataDialog(this, trc, view, view, view::jump, this);
            dlg.setVisible(true);
        });
        JMenuItem typeRecoveryWindow = new JMenuItem("Type Recovery");
        typeRecoveryWindow.setMnemonic('r');
        typeRecoveryWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK));
        typeRecoveryWindow.addActionListener(e -> {
            TypeRecoveryDialog dlg = new TypeRecoveryDialog(this, trc, view, this);
            dlg.setVisible(true);
        });
        subviewMenu.add(datatypeWindow);
        subviewMenu.add(abiWindow);
        subviewMenu.add(dataWindow);
        subviewMenu.add(deviceWindow);
        subviewMenu.add(typeRecoveryWindow);
        subviewMenu.setEnabled(false);
        viewMenu.add(subviewMenu);

        gotoPC = new JMenuItem("Goto PC...");
        gotoPC.setMnemonic('g');
        gotoPC.setAccelerator(KeyStroke.getKeyStroke('g'));
        gotoPC.addActionListener(e -> {
            StepEvent step = view.getSelectedInstruction();
            String input;

            StepFormat fmt = trc.getArchitecture().getFormat();
            int base = 10;
            if (fmt.numberfmt == StepFormat.NUMBERFMT_OCT) {
                base = 8;
            } else if (fmt.numberfmt == StepFormat.NUMBERFMT_HEX) {
                base = 16;
            }

            if (step != null) {
                long loc = step.getPC();
                input = JOptionPane.showInputDialog("Enter address:", fmt.formatAddress(loc));
                if (input != null && input.trim().length() > 0) {
                    try {
                        long pc = Long.parseLong(input.trim(), base);
                        Node n = trc.getNextPC(view.getSelectedNode(), pc);
                        if (n != null) {
                            log.info("Jumping to next occurence of PC=" + fmt.formatAddress(pc));
                            view.jump(n);
                        } else {
                            JOptionPane.showMessageDialog(this, "Error: cannot find a next instruction at " + fmt.formatAddress(pc), "Goto...", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Error: invalid number", "Goto PC...", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                Optional<ComputedSymbol> first = trc.getSymbols().stream().sorted((a, b) -> Long.compareUnsigned(a.address, b.address)).findFirst();
                if (first.isPresent()) {
                    input = JOptionPane.showInputDialog("Enter address:", fmt.formatAddress(first.get().address));
                } else {
                    input = JOptionPane.showInputDialog("Enter address:", "0");
                }
                if (input != null && input.trim().length() > 0) {
                    try {
                        long pc = Long.parseLong(input.trim(), base);
                        Node n = trc.getNextPC(trc.getRoot(), pc);
                        if (n != null) {
                            log.info("Jumping to next occurence of PC=" + fmt.formatAddress(pc));
                            view.jump(n);
                        } else {
                            JOptionPane.showMessageDialog(this, "Error: cannot find a next instruction at " + fmt.formatAddress(pc), "Goto...", JOptionPane.ERROR_MESSAGE);
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

        typeRecovery = new JCheckBoxMenuItem("Perform type recovery");
        typeRecovery.setMnemonic('t');
        typeRecovery.setSelected(false);
        toolsMenu.add(typeRecovery);

        codeAnalysis = new JCheckBoxMenuItem("Perform code vs data analysis");
        codeAnalysis.setMnemonic('c');
        codeAnalysis.setSelected(true);
        toolsMenu.add(codeAnalysis);

        useSymbols = new JCheckBoxMenuItem("Use symbols/labels");
        useSymbols.setMnemonic('s');
        useSymbols.setSelected(false);
        useSymbols.addItemListener(e -> {
            if (trc != null) {
                trc.setSymbolize(useSymbols.isSelected());
                view.update();
            }
        });
        useSymbols.setSelected(true);
        toolsMenu.add(useSymbols);

        autoComment = new JCheckBoxMenuItem("Enable autocomment");
        autoComment.setMnemonic('a');
        autoComment.setSelected(false);
        autoComment.addItemListener(e -> view.setAutocomment(autoComment.isSelected()));
        autoComment.setSelected(true);
        toolsMenu.add(autoComment);

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
        helpMenu.setMnemonic('H');

        JMenuItem help = new JMenuItem("Help");
        help.setMnemonic('H');
        help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        help.addActionListener(e -> {
            HelpBrowser dlg = new HelpBrowser(this);
            dlg.setVisible(true);
        });
        helpMenu.add(help);

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

        menu.add(helpMenu);

        setJMenuBar(menu);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 600);
        setLocationRelativeTo(null);
    }

    public synchronized void addTraceListener(TraceListener l) {
        traceListeners.add(l);
    }

    public synchronized void removeTraceListener(TraceListener l) {
        traceListeners.remove(l);
    }

    protected synchronized void setTraceAnalyzer(TraceAnalyzer trc) {
        for (TraceListener l : traceListeners) {
            try {
                l.setTraceAnalyzer(trc);
            } catch (Throwable t) {
                log.log(Levels.ERROR, "Error setting trace", t);
            }
        }
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
        long start = System.currentTimeMillis();
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
        long end = System.currentTimeMillis();
        long time = end - start;
        log.info("File loaded [" + time + " ms]");
    }

    private void setTrace(TraceAnalyzer trc) {
        this.trc = trc;
        trc.setSymbolize(useSymbols.isSelected());
        view.setTraceAnalyzer(trc);
        loadPrototypes.setEnabled(true);
        loadMap.setEnabled(true);
        loadIdaMap.setEnabled(true);
        generateIDC.setEnabled(true);
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
        setColor.setEnabled(true);
        gotoPC.setEnabled(true);
        gotoInsn.setEnabled(true);
        gotoNext.setEnabled(true);
        exportMemory.setEnabled(true);
        subviewMenu.setEnabled(true);
        pluginLoader.traceLoaded(trc);

        setTraceAnalyzer(trc);
    }

    public void load(TraceReader reader, long size, String file) throws IOException {
        log.info("Loading file " + file + "...");
        open.setEnabled(false);
        try {
            String text = "Loading " + file;
            setStatus(text);
            setPosition(-1);
            List<Analyzer> analyzers = pluginLoader.getAnalyzers(reader.getArchitecture());
            Analyzer analyzer = reader.getAnalyzer();
            if (analyzer != null) {
                analyzers.add(analyzer);
                for (Constructor<? extends VMTransformer> clazz : vmTransformers) {
                    try {
                        VMTransformer transformer = clazz.newInstance();
                        if (transformer.isApplicable(reader.getArchitecture())) {
                            analyzers.add(new VMAnalyzer(transformer));
                        }
                    } catch (Throwable t) {
                        log.log(Levels.ERROR, "Failed to instantiate VM transformer: " + t.getMessage(), t);
                        throw new IOException(t);
                    }
                }
            }
            Analysis analysis = new Analysis(reader.getArchitecture(), analyzers, typeRecovery.isSelected(), codeAnalysis.isSelected());
            analysis.start();
            Map<Integer, BlockNode> threads = TraceParser.parse(reader, analysis, pos -> setStatus(text + " (" + (pos * 100L / size) + "%)"));
            BlockNode root = null;
            for (BlockNode block : threads.values()) {
                if (root == null) {
                    root = block;
                } else if (block.getStep() < root.getStep()) {
                    root = block;
                }
            }
            analysis.finish(root);
            if (root == null || root.getFirstStep() == null) {
                setStatus("Loading failed");
                return;
            }

            setStatus("Trace loaded");
            setTitle(file + " - " + WINDOW_TITLE);

            final BlockNode rootNode = root;
            EventQueue.invokeLater(() -> {
                TraceAnalyzer t = new Local(reader.getArchitecture(), rootNode, threads, analysis);
                setTrace(t);
                // setTrace(new LocalDatabase(reader.getArchitecture(), root, analysis));
                for (Analyzer a : analyzers) {
                    if (a instanceof VMAnalyzer) {
                        VMAnalyzer va = (VMAnalyzer) a;
                        va.traceLoaded(this, t);
                    }
                }
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
                        Parser parser = new Parser(line, trc.getTypeDatabase());
                        Function fun = parser.parsePrototype();
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

    public void loadMap(File file) throws IOException {
        log.info("Loading map file " + file + "...");
        setStatus("Loading map file " + file + "...");
        loadMap.setEnabled(false);
        boolean reanalyze = false;
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            int lineno = 0;
            Set<Long> globals = new HashSet<>();
            while ((line = in.readLine()) != null) {
                lineno++;
                line = line.trim();
                String[] parts = line.split(" ");
                if (parts.length != 3) { // ignore
                    continue;
                }
                long addr;
                try {
                    addr = Long.parseUnsignedLong(parts[0], 16);
                } catch (NumberFormatException e) {
                    log.info("Parse error in line " + lineno + ": invalid address");
                    continue;
                }
                if (parts[1].length() != 1) {
                    log.info("Parse error in line " + lineno + ": invalid type");
                    continue;
                }
                String name = parts[2].trim();
                char type = parts[1].charAt(0);
                switch (type) {
                    case 'a':
                    case 'A':
                    case 't':
                    case 'T': {
                        if (!globals.contains(addr) || Character.isUpperCase(type)) {
                            ComputedSymbol sym = trc.getComputedSymbol(addr);
                            if (sym == null) {
                                trc.addSubroutine(addr, name, null);
                                reanalyze = true;
                            } else {
                                trc.renameSymbol(sym, name);
                            }
                            if (Character.isUpperCase(type)) {
                                globals.add(addr);
                            }
                        }
                        break;
                    }
                }
            }
            setStatus("Map file loaded");
            view.updateThreadNames();
        } catch (Throwable t) {
            log.log(Level.WARNING, "Loading failed: " + t, t);
            setStatus("Loading failed: " + t);
            throw t;
        } finally {
            loadMap.setEnabled(true);
            if (reanalyze) {
                trc.reanalyze();
            }
        }
        log.info("Map file loaded");
    }

    public void loadIdaMap(File file) throws IOException {
        log.info("Loading map file " + file + "...");
        setStatus("Loading map file " + file + "...");
        loadIdaMap.setEnabled(false);
        boolean reanalyze = false;
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            int lineno = 0;
            Set<Long> globals = new HashSet<>();
            while ((line = in.readLine()) != null) {
                lineno++;
                line = line.trim();
                String[] parts = line.split("\\s+");
                if (parts.length != 2) { // ignore
                    continue;
                }
                String[] loc = parts[0].split(":");
                if (loc.length != 2) {
                    log.info("Parse error in line " + lineno + ": invalid address");
                    continue;
                }
                long addr;
                try {
                    addr = Long.parseUnsignedLong(loc[1], 16);
                } catch (NumberFormatException e) {
                    log.info("Parse error in line " + lineno + ": invalid address");
                    continue;
                }
                String name = parts[1].trim();
                if (!globals.contains(addr)) {
                    ComputedSymbol sym = trc.getComputedSymbol(addr);
                    if (sym == null) {
                        trc.addSubroutine(addr, name, null);
                        reanalyze = true;
                    } else {
                        trc.renameSymbol(sym, name);
                    }
                    globals.add(addr);
                }
            }
            setStatus("Map file loaded");
            view.updateThreadNames();
        } catch (Throwable t) {
            log.log(Level.WARNING, "Loading failed: " + t, t);
            setStatus("Loading failed: " + t);
            throw t;
        } finally {
            loadIdaMap.setEnabled(true);
            if (reanalyze) {
                trc.reanalyze();
            }
        }
        log.info("Map file loaded");
    }

    public void generateIDC(File file) throws IOException {
        log.info("Generating IDC script " + file + "...");
        setStatus("Generating IDC script " + file + "...");
        generateIDC.setEnabled(false);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            out.println("// This file was generated by TRCView");
            out.println();
            out.println("#include <idc.idc>\n\n" +
                            "static main(void) {");
            out.println("\t// functions");
            Set<ComputedSymbol> symbols = trc.getSubroutines();
            symbols.stream().sorted((a, b) -> Long.compareUnsigned(a.address, b.address)).forEach(sym -> {
                out.printf("\tadd_func(0x%x);\n", sym.address);
                if (!sym.name.startsWith("sub_") && !sym.name.startsWith("j_sub_")) {
                    out.printf("\tset_name(0x%x, %s, SN_NOWARN);\n", sym.address, DecoderUtils.str(sym.name));
                }
                if (sym.prototype != null) {
                    out.printf("\tSetType(0x%x, \"%s __fastcall %s(%s);\");\n", sym.address, sym.prototype.returnType.toCType(), sym.name,
                                    sym.prototype.args.stream().map(x -> x.toCType()).collect(Collectors.joining(", ")));
                }
            });
            out.println();

            out.println("\t// comments");
            Map<Long, String> commentsPC = trc.getCommentsForPCs();
            commentsPC.entrySet().stream().sorted((x, y) -> Long.compareUnsigned(x.getKey(), y.getKey())).forEach(comment -> {
                out.printf("\tset_cmt(0x%x, %s, 0);\n", comment.getKey(), DecoderUtils.str(comment.getValue()));
            });

            out.println();
            out.println("\t// data types");
            UserTypeDatabase types = trc.getTypeDatabase();
            if (!types.getTypes().isEmpty()) {
                out.println("\tauto id;");
                out.println("\tbegin_type_updating(UTP_STRUCT);");
            }
            for (UserDefinedType type : types.getTypes()) {
                String name = type.getName();
                if (type instanceof Struct) {
                    Struct struct = (Struct) type;
                    out.printf("\tid = add_struc(-1, %s, 0);\n", DecoderUtils.str(name));
                    for (Field field : struct.getFields()) {
                        String fieldname = field.getName();
                        long offset = field.getOffset();
                        long size = field.getSize();
                        List<String> flagConstants = new ArrayList<>();
                        String typeid = "-1";
                        switch (field.getType().getType()) {
                            case S8:
                            case U8:
                                flagConstants.add("FF_BYTE");
                                break;
                            case S16:
                            case U16:
                            case FX16:
                                flagConstants.add("FF_WORD");
                                break;
                            case S32:
                            case U32:
                            case FX32:
                                flagConstants.add("FF_DWORD");
                                break;
                            case S64:
                            case U64:
                                flagConstants.add("FF_QWORD");
                                break;
                            case F32:
                                flagConstants.add("FF_FLOAT");
                                break;
                            case F64:
                                flagConstants.add("FF_DOUBLE");
                                break;
                            case STRUCT:
                                flagConstants.add("FF_STRUCT");
                                typeid = "get_struc_id(" + DecoderUtils.str(field.getType().getStruct().getName()) + ")";
                                break;
                        }
                        switch (field.getType().getRepresentation()) {
                            case CHAR:
                                flagConstants.add("FF_0CHAR");
                                break;
                            case DEC:
                                flagConstants.add("FF_0NUMD");
                                break;
                            case HEX:
                            case FX16:
                            case FX32:
                                flagConstants.add("FF_0NUMH");
                                break;
                            case OCT:
                                flagConstants.add("FF_0NUMO");
                                break;
                            case FLOAT:
                                flagConstants.add("FF_0FLT");
                                break;
                        }
                        flagConstants.add("FF_DATA");
                        String flags = String.join("|", flagConstants);
                        out.printf("\tadd_struc_member(id, %s, 0x%x, %s, %s, %s);\n", DecoderUtils.str(fieldname), offset, flags, typeid, size);
                    }
                }
            }
            if (!types.getTypes().isEmpty()) {
                out.println("\tend_type_updating(UTP_STRUCT);");

                out.println();
                out.println("\t// struct field types");
                for (UserDefinedType type : types.getTypes()) {
                    String name = type.getName();
                    if (type instanceof Struct) {
                        out.printf("\tid = get_struc_id(%s);\n", DecoderUtils.str(name));
                        Struct struct = (Struct) type;
                        for (Field field : struct.getFields()) {
                            if (field.getType() != null && field.getType().getType() != DataType.CODE) {
                                out.printf("\tSetType(get_member_id(id, 0x%x), %s);\n", field.getOffset(), DecoderUtils.str(field.getType().toCType()));
                            }
                        }
                    }
                }
            }

            out.println();
            out.println("\t// variables");
            TypedMemory mem = trc.getTypedMemory();
            for (Variable var : mem.getAllTypes()) {
                String name = var.getRawName();
                if (name != null) {
                    out.printf("\tset_name(0x%x, %s, SN_NOWARN);\n", var.getAddress(), DecoderUtils.str(name));
                }

                if (var.getType() != null && var.getType().getType() != DataType.CODE) {
                    out.printf("\tSetType(0x%x, %s);\n", var.getAddress(), DecoderUtils.str(var.getType().toCType()));
                } else if (var.getType() != null && var.getType().getType() == DataType.CODE) {
                    out.printf("\tcreate_insn(0x%x);\n", var.getAddress());
                }
            }

            out.println("}");

            log.info("Finished generating IDC script " + file);
            setStatus("Finished generating IDC script " + file);
        } catch (Throwable t) {
            log.log(Level.WARNING, "Generating IDC script failed: " + t, t);
            setStatus("Generating IDC script failed: " + t);
            throw t;
        } finally {
            generateIDC.setEnabled(true);
        }
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
        String memory = null;
        String memhistory = null;
        Map<Integer, String> threadNames = new HashMap<>();
        Map<Integer, Long> threadInstructions = new HashMap<>();
        Map<String, String> types = new HashMap<>();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;
            int lineno = 0;
            while ((line = in.readLine()) != null) {
                int comment = line.indexOf('#');
                if (comment > 0) {
                    // TODO: handle comments properly!
                    String sub = line.substring(0, comment).trim();
                    if (sub.length() == 0) {
                        continue;
                    }
                    // line = line.substring(0, comment).trim();
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                lineno++;
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String address = line.substring(0, idx);
                    String[] data;
                    try {
                        data = TextSerializer.tokenize(line.substring(idx + 1));
                    } catch (IOException e) {
                        log.info("Syntax error in line " + lineno + ": " + e.getMessage());
                        setStatus("Syntax error in line " + lineno + ": " + e.getMessage());
                        ok = false;
                        continue;
                    }
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
                            expr = new Parser(str).parseExpression();
                        } catch (ParseException e) {
                            log.info("Syntax error in line " + lineno + ": " + e.getMessage());
                            setStatus("Syntax error in line " + lineno + ": " + e.getMessage());
                            ok = false;
                            continue;
                        }
                        watches.add(new Watch(name, format, str, expr));
                    } else if (everything && address.equals("MEMORY")) {
                        // memory expression
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid memory address");
                            setStatus("Syntax error in line " + lineno + ": invalid memory address");
                            ok = false;
                            continue;
                        }
                        memory = data[0];
                    } else if (everything && address.equals("MEMHISTORY")) {
                        // memory history expression
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid memory address");
                            setStatus("Syntax error in line " + lineno + ": invalid memory address");
                            ok = false;
                            continue;
                        }
                        memhistory = data[0];
                    } else if (everything && address.equals("INSN")) {
                        // selected instruction
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
                    } else if (everything && address.startsWith("COLOR:")) {
                        // color for PC
                        long pc;
                        if (data.length != 1 || data[0].length() != 6) {
                            log.info("Syntax error in line " + lineno + ": invalid color");
                            setStatus("Syntax error in line " + lineno + ": invalid color");
                            ok = false;
                            continue;
                        }
                        try {
                            pc = Long.parseUnsignedLong(address.substring(6), 16);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid program counter");
                            setStatus("Syntax error in line " + lineno + ": invalid program counter");
                            ok = false;
                            continue;
                        }
                        try {
                            int r = Integer.parseInt(data[0].substring(0, 2), 16);
                            int g = Integer.parseInt(data[0].substring(2, 4), 16);
                            int b = Integer.parseInt(data[0].substring(4, 6), 16);
                            Color color = new Color(r, g, b);
                            trc.setColor(pc, color);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid color");
                            setStatus("Syntax error in line " + lineno + ": invalid color");
                            ok = false;
                            continue;
                        }
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
                    } else if (everything && address.startsWith("THREAD:")) {
                        // thread
                        int tid;
                        if (data.length != 1 && data.length != 2) {
                            log.info("Syntax error in line " + lineno + ": invalid thread name");
                            setStatus("Syntax error in line " + lineno + ": invalid thread name");
                            ok = false;
                            continue;
                        }
                        try {
                            tid = Integer.parseInt(address.substring(7), 16);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid thread id");
                            setStatus("Syntax error in line " + lineno + ": invalid thread id");
                            ok = false;
                            continue;
                        }
                        if (data.length == 2) {
                            threadInstructions.put(tid, Long.parseUnsignedLong(data[1]));
                        }
                        threadNames.put(tid, data[0]);
                    } else if (everything && address.equals("ABI")) {
                        // ABI definition
                        try {
                            ABI abi = trc.getABI();
                            if (abi instanceof GenericABI) {
                                ABISerializer.load((GenericABI) abi, line.substring(idx + 1), trc.getTypeDatabase());
                            } else {
                                log.info("Cannot change non-generic ABI");
                                setStatus("Cannot change non-generic ABI");
                                ok = false;
                                continue;
                            }
                        } catch (IOException | ParseException e) {
                            log.info("Syntax error in line " + lineno + ": " + e.getMessage());
                            setStatus("Syntax error in line " + lineno + ": " + e.getMessage());
                            ok = false;
                            continue;
                        }
                    } else if (everything && address.startsWith("TYPE")) {
                        // data type
                        if (data.length != 1) {
                            log.info("Syntax error in line " + lineno + ": invalid type");
                            setStatus("Syntax error in line " + lineno + ": invalid type");
                            ok = false;
                            continue;
                        }
                        String name = address.substring(5);
                        String value = data[0];
                        types.put(name, value);
                    } else if (everything && address.startsWith("VAR:")) {
                        // memory variable
                        if (data.length != 2) {
                            log.info("Syntax error in line " + lineno + ": invalid variable definition");
                            setStatus("Syntax error in line " + lineno + ": invalid variable definition");
                            ok = false;
                            continue;
                        }
                        // flush all pending types
                        defineTypes(types);
                        try {
                            TypedMemory mem = trc.getTypedMemory();
                            long addr = Long.parseUnsignedLong(address.substring(4), 16);
                            String name = data[0];
                            if (data[1].startsWith("/*")) {
                                // unparseable type, ignore it
                                continue;
                            }
                            mem.set(addr, new Parser(data[1], trc.getTypeDatabase()).parseType(), name);
                        } catch (NumberFormatException e) {
                            log.info("Syntax error in line " + lineno + ": invalid address");
                            setStatus("Syntax error in line " + lineno + ": invalid address");
                            ok = false;
                            continue;
                        } catch (ParseException e) {
                            log.info("Syntax error in line " + lineno + ": invalid type");
                            setStatus("Syntax error in line " + lineno + ": invalid type");
                            ok = false;
                            continue;
                        }
                    } else {
                        // symbol
                        // first, flush all pending types
                        defineTypes(types);
                        // then, parse the symbol
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
                                fun = new Parser(proto, trc.getTypeDatabase()).parsePrototype();
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
            defineTypes(types);
            if (reanalyze) {
                reanalyze = false;
                log.info("Reanalyzing visits...");
                setStatus("Reanalyzing visits...");
                trc.reanalyze();
            }
            view.updateThreadNames();
            String filetype = "Session file";
            if (!everything) {
                filetype = "Symbol file";
            } else {
                final String memexpr = memory;
                final String memhistoryexpr = memhistory;
                SwingUtilities.invokeLater(() -> {
                    view.setWatches(watches);
                    if (memexpr != null) {
                        view.setMemoryExpression(memexpr);
                    }
                    if (memhistoryexpr != null) {
                        view.setMemoryHistoryExpression(memhistoryexpr);
                    }
                    view.setThreadNames(threadNames);
                    view.setThreadSteps(threadInstructions);
                });
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

    private void defineTypes(Map<String, String> types) {
        if (!types.isEmpty()) {
            UserTypeDatabase db = trc.getTypeDatabase();
            // create all structs
            for (Entry<String, String> entry : types.entrySet()) {
                if (entry.getValue().startsWith("struct")) {
                    try {
                        db.add(new Struct(entry.getKey()), true);
                    } catch (NameAlreadyUsedException e) {
                        log.info("Data type " + entry.getKey() + " already defined");
                        setStatus("Data type " + entry.getKey() + " already defined");
                    }
                }
            }
            // add the struct bodies
            for (Entry<String, String> entry : types.entrySet()) {
                if (entry.getValue().startsWith("struct")) {
                    Parser p = new Parser(entry.getValue(), db);
                    try {
                        Struct s = p.parseStruct();
                        Struct defined = (Struct) db.get(entry.getKey());
                        defined.set(s);
                    } catch (ParseException e) {
                        log.info("Syntax error in data type definition of " + entry.getKey());
                        setStatus("Syntax error in data type definition of " + entry.getKey());
                    }
                }
            }
            types.clear();
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
            if (everything) {
                Collection<UserDefinedType> types = trc.getTypeDatabase().getTypes();
                for (UserDefinedType type : types) {
                    out.printf("TYPE:%s=%s\n", type.getName(), TextSerializer.encode(type.toString()));
                }
            }
            Set<ComputedSymbol> symbols = trc.getSubroutines();
            symbols.stream().sorted((a, b) -> Long.compareUnsigned(a.address, b.address)).forEach(sym -> {
                if (sym.prototype != null) {
                    out.printf("%x=%s\n", sym.address,
                                    TextSerializer.encode(sym.name, sym.prototype.returnType.toString(), String.join(", ", sym.prototype.getArgumentsAsString())));
                } else {
                    out.printf("%x=%s\n", sym.address, TextSerializer.encode(sym.name));
                }
            });
            if (everything) {
                List<Variable> vars = trc.getTypedMemory().getTypes();
                for (Variable var : vars) {
                    String t = var.getType() == null ? null : var.getType().toString();
                    out.printf("VAR:%x=%s\n", var.getAddress(), TextSerializer.encode(var.getRawName(), t));
                }
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
                Map<Long, Color> colors = trc.getColors();
                for (Entry<Long, Color> color : colors.entrySet()) {
                    Color c = color.getValue();
                    out.printf("COLOR:%x=\"%02x%02x%02x\"\n", color.getKey(), c.getRed(), c.getGreen(), c.getBlue());
                }
                String memexpr = view.getMemoryExpression();
                out.printf("MEMORY=%s\n", TextSerializer.encode(memexpr));
                String memhistexpr = view.getMemoryHistoryExpression();
                out.printf("MEMHISTORY=%s\n", TextSerializer.encode(memhistexpr));
                StepEvent step = view.getSelectedInstruction();
                if (step != null) {
                    out.printf("INSN=%d\n", step.getStep());
                }
                for (ThreadID thread : view.getThreads()) {
                    Node node = view.getThreadNode(thread.id);
                    long id;
                    if (node instanceof StepEvent) {
                        id = ((StepEvent) node).getStep();
                    } else if (node instanceof BlockNode) {
                        id = ((BlockNode) node).getStep();
                    } else {
                        throw new AssertionError("node is not a step event nor a block");
                    }
                    out.printf("THREAD:%s=%s\n", thread.id, TextSerializer.encode(thread.name, Long.toUnsignedString(id)));
                }
                if (trc.getABI() instanceof GenericABI) {
                    GenericABI abi = (GenericABI) trc.getABI();
                    out.printf("ABI=%s\n", ABISerializer.store(abi));
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

    private void exit() {
        dispose();
        System.exit(0);
    }

    private void loadAnalyzer(String name)
                    throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
                    ClassCastException {
        log.info("Loading analyzer \"" + name + "\"");
        @SuppressWarnings("unchecked")
        Class<? extends VMTransformer> clazz = (Class<? extends VMTransformer>) Class.forName(name);
        VMTransformer transformer = clazz.getConstructor().newInstance();
        VMArchitecture arch = transformer.getArchitecture();
        log.info("Analyzer for architecture " + arch.getName() + " [" + arch.getDescription() + "] loaded");
        vmTransformers.add(clazz.getConstructor());
    }

    private void parseArgs(String[] args) {
        String tracefile = null;
        String sessionfile = null;
        int i;
        loop: for (i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-a":
                    if (i + 1 < args.length) {
                        String analyzer = args[i + 1];
                        try {
                            loadAnalyzer(analyzer);
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                                        | SecurityException | ClassCastException e) {
                            System.out.println("Failed to load the analyzer class \"" + analyzer + "\"");
                        }
                        i++;
                    } else {
                        System.out.println("Syntax error: missing argument for option -a");
                        return;
                    }
                    break;
                case "-f":
                    if (i + 1 < args.length) {
                        tracefile = args[i + 1];
                        i++;
                    } else {
                        System.out.println("Syntax error: missing argument for option -f");
                        return;
                    }
                    break;
                case "-l":
                    if (i + 1 < args.length) {
                        sessionfile = args[i + 1];
                        i++;
                    } else {
                        System.out.println("Syntax error: missing argument for option -l");
                        return;
                    }
                    break;
                case "--":
                default:
                    break loop;
            }
        }

        if (i < args.length) {
            tracefile = args[i];
        }

        if (tracefile != null) {
            try {
                load(new File(tracefile));
            } catch (Throwable t) {
                System.out.println("Failed to load the trace file specified by the argument \"" + tracefile + "\"");
                return;
            }
        }

        if (sessionfile != null) {
            try {
                loadSession(new File(sessionfile));
            } catch (Throwable t) {
                System.out.println("Failed to load the session file specified by the argument \"" + sessionfile + "\"");
            }
        }
    }

    public static void main(String[] args) {
        Trace.setup();
        MainWindow w = new MainWindow();
        w.setVisible(true);
        w.parseArgs(args);
    }
}
