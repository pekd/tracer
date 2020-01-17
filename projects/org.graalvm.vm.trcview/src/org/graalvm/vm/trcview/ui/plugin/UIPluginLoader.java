package org.graalvm.vm.trcview.ui.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JMenu;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.TraceView;

public class UIPluginLoader {
    private static final ServiceLoader<UIPlugin> loader = ServiceLoader.load(UIPlugin.class);

    private final List<UIPlugin> plugins = new ArrayList<>();

    public UIPluginLoader(MainWindow main, JMenu menu, TraceView view) {
        load(main, menu, view);
    }

    private void load(MainWindow main, JMenu menu, TraceView view) {
        for (UIPlugin plugin : loader) {
            plugins.add(plugin);
            plugin.init(main, menu, view);
        }
    }

    public void traceLoaded(TraceAnalyzer analyzer) {
        for (UIPlugin plugin : plugins) {
            plugin.traceLoaded(analyzer);
        }
    }

    public List<Analyzer> getAnalyzers(Architecture arch) {
        List<Analyzer> analyzers = new ArrayList<>();
        for (UIPlugin plugin : plugins) {
            analyzers.addAll(plugin.getAnalyzers(arch));
        }
        return analyzers;
    }

    public int count() {
        return plugins.size();
    }
}
