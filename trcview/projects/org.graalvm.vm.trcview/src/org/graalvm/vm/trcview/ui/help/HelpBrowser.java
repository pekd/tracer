package org.graalvm.vm.trcview.ui.help;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.TreePath;

import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

@SuppressWarnings("serial")
public class HelpBrowser extends JDialog {
    private static final Logger log = Trace.create(HelpBrowser.class);

    private Help help;
    private JTree tree;
    private JTextPane htmlview;

    public HelpBrowser(JFrame parent) {
        super(parent, "TRCView Online Documentation");

        setLayout(new BorderLayout());

        help = new Help();
        tree = new JTree(help);

        htmlview = new JTextPane();
        htmlview.setFont(new Font(Font.DIALOG, Font.PLAIN, 11));
        htmlview.setBackground(UIManager.getColor("TextField.background"));
        htmlview.setEditable(false);
        htmlview.setContentType("text/html");

        htmlview.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    URL url = e.getURL();
                    if (url.toString().startsWith("http://") || url.toString().startsWith("https://")) {
                        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(url.toURI());
                            } catch (Exception ex) {
                                log.log(Levels.ERROR, "Failed to launch browser: " + ex.getMessage(), ex);
                            }
                        }
                    } else {
                        try {
                            htmlview.setPage(url);
                        } catch (IOException ex) {
                            log.log(Levels.ERROR, "Failed to navigate to " + url + ": " + ex.getMessage(), ex);
                        }
                    }
                }
            }
        });

        tree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            HelpNode node = (HelpNode) path.getLastPathComponent();
            if (node instanceof Page) {
                Page page = (Page) node;
                try {
                    htmlview.setPage(page.getURL());
                } catch (IOException ex) {
                    log.log(Levels.ERROR, "Failed to navigate to " + page.getURL() + ": " + ex.getMessage(), ex);
                }
            } else if (node instanceof Category) {
                Category cat = (Category) node;
                if (cat.getURL() != null) {
                    try {
                        htmlview.setPage(cat.getURL());
                    } catch (IOException ex) {
                        log.log(Levels.ERROR, "Failed to navigate to " + cat.getURL() + ": " + ex.getMessage(), ex);
                    }
                }
            }
        });

        tree.setSelectionPath(new TreePath(help.getRoot()));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setLeftComponent(new JScrollPane(tree));
        split.setRightComponent(new JScrollPane(htmlview));
        split.setResizeWeight(0.1);

        add(BorderLayout.CENTER, split);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setSize(640, 480);
    }
}
