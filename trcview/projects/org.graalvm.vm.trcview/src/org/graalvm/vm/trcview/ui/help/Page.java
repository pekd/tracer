package org.graalvm.vm.trcview.ui.help;

import java.net.URL;

public class Page extends HelpNode {
    private final URL url;

    public Page(String name, String file) {
        super(name);
        url = Help.url(file);
    }

    public URL getURL() {
        return url;
    }
}
