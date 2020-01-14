package org.graalvm.vm.trcview.ui.plugin;

import javax.swing.JMenu;

import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.TraceView;

public abstract class UIPlugin {
    public abstract void init(MainWindow main, JMenu pluginMenu, TraceView view);

    public abstract void setTraceAnalyzer(TraceAnalyzer analyzer);
}
