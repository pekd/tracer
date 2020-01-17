package org.graalvm.vm.trcview.arch.custom.ui;

import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.custom.analysis.CustomAnalyzer;
import org.graalvm.vm.trcview.arch.custom.io.CustomTraceReader;
import org.graalvm.vm.trcview.arch.io.TraceReader;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.TraceView;
import org.graalvm.vm.trcview.ui.plugin.UIPlugin;
import org.graalvm.vm.util.ui.MessageBox;

public class CustomUI extends UIPlugin {
    private static final String ABOUT_TEXT = "<html><body>Custom Architecture Support v1.0</body></html>";

    private CustomAnalyzer analyzer;
    private boolean transform;

    @Override
    public void init(MainWindow main, JMenu pluginMenu, TraceView view) {
        transform = false;

        FileDialog load = new FileDialog(main, "Load...", FileDialog.LOAD);

        JMenu customMenu = new JMenu("Custom Architecture");
        JMenuItem define = new JMenuItem("Define...");
        define.addActionListener(e -> {
            load.setVisible(true);
            if (load.getFile() == null) {
                return;
            }
            String filename = load.getDirectory() + load.getFile();
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        define(new File(filename));
                    } catch (Exception ex) {
                        MessageBox.showError(main, ex);
                    }
                    return null;
                }
            };
            worker.execute();
        });
        JCheckBoxMenuItem transformTrace = new JCheckBoxMenuItem("Transform");
        transformTrace.setSelected(true);
        transformTrace.addActionListener(e -> {
            transform = transformTrace.isSelected();
        });
        transform = transformTrace.isSelected();
        JMenuItem about = new JMenuItem("About...");
        about.addActionListener(e -> JOptionPane.showMessageDialog(main, ABOUT_TEXT, "About...", JOptionPane.INFORMATION_MESSAGE));
        customMenu.add(define);
        customMenu.add(transformTrace);
        customMenu.addSeparator();
        customMenu.add(about);
        pluginMenu.add(customMenu);

    }

    public void define(File file) throws IOException {
        String script = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        setAnalyzer(new CustomAnalyzer(script));
    }

    public void setAnalyzer(CustomAnalyzer analyzer) {
        if (Architecture.getArchitecture(analyzer.getArchitecture().getId()) != null && (this.analyzer == null || this.analyzer.getArchitecture().getId() != analyzer.getArchitecture().getId())) {
            throw new IllegalArgumentException("Cannot overwrite existing architecture");
        }
        if (this.analyzer != null) {
            this.analyzer = null;
            Architecture.unregister(analyzer.getArchitecture());
        }
        Architecture.register(analyzer.getArchitecture());
        this.analyzer = analyzer;
    }

    @Override
    public List<Analyzer> getAnalyzers(Architecture arch) {
        if (arch == analyzer.getArchitecture()) {
            return Collections.emptyList();
        }

        List<Analyzer> result = new ArrayList<>();
        if (transform && analyzer != null) {
            result.add(analyzer);
        }
        return result;
    }

    @Override
    public void traceLoaded(TraceAnalyzer trc) {
        if (!transform || analyzer == null) {
            return;
        }

        if (trc.getArchitecture() == analyzer.getArchitecture()) {
            return;
        }

        if (analyzer.getEvents().isEmpty()) {
            return;
        }

        MainWindow window = new MainWindow(false);
        window.setVisible(true);
        window.setStatus("Loading...");
        TraceReader reader = new CustomTraceReader(analyzer);
        long size = analyzer.getEvents().size();
        String file = "Transformed trace [" + analyzer.getArchitecture().getName() + "]";
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    window.load(reader, size, file);
                } catch (IOException ex) {
                    MessageBox.showError(window, ex);
                    window.dispose();
                }
                return null;
            }
        };
        worker.execute();
    }
}
