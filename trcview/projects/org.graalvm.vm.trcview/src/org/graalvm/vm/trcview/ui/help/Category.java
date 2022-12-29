package org.graalvm.vm.trcview.ui.help;

import java.net.URL;
import java.util.List;

public class Category extends HelpNode {
    private List<HelpNode> children;
    private URL url;

    public Category(String name, String page, List<HelpNode> children) {
        super(name);
        this.children = children;
        if (page == null || page.length() == 0) {
            this.url = null;
        } else {
            this.url = Help.url(page);
        }
    }

    public List<HelpNode> getChildren() {
        return children;
    }

    public URL getURL() {
        return url;
    }
}
