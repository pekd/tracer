package org.graalvm.vm.trcview.ui.plugin;

import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;

import org.graalvm.vm.trcview.analysis.Analyzer;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.TraceView;

public abstract class UIPlugin {
    public abstract void init(MainWindow main, JMenu pluginMenu, TraceView view);

    public void traceLoaded(@SuppressWarnings("unused") TraceAnalyzer analyzer) {
        // nothing by default
    }

    public List<Analyzer> getAnalyzers(@SuppressWarnings("unused") Architecture arch) {
        return Collections.emptyList();
    }
}
