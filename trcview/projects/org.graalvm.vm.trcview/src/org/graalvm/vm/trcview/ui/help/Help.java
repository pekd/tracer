package org.graalvm.vm.trcview.ui.help;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.graalvm.vm.util.ResourceLoader;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Help implements TreeModel {
    private static final Logger log = Trace.create(Help.class);

    private final Category root;

    public Help() {
        Category help;
        try {
            help = parseIndex();
        } catch (IOException e) {
            log.log(Levels.ERROR, "Failed to load help index: " + e.getMessage(), e);
            help = new Category("TRCView", null, Collections.emptyList());
        }
        root = help;
    }

    public static InputStream load(String path) {
        return ResourceLoader.loadResource(Help.class, "content/" + path);
    }

    public static URL url(String path) {
        return ResourceLoader.getURL(Help.class, "content/" + path);
    }

    private static Category parseIndex() throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(load("index.xml"));

            Element rootNode = xml.getDocumentElement();
            HelpNode result = parse(rootNode);
            if (result instanceof Category) {
                return (Category) result;
            } else {
                return new Category("TRCView", null, List.of(result));
            }
        } catch (ParserConfigurationException e) {
            log.log(Levels.ERROR, "Failed to instantiate DocumentBuilder: " + e.getMessage(), e);
        } catch (SAXException e) {
            log.log(Levels.ERROR, "Failed to parse help index: " + e.getMessage(), e);
        }

        // failure case
        return new Category("TRCView", null, Collections.emptyList());
    }

    private static HelpNode parse(Element xml) {
        String type = xml.getTagName();
        if (type.equals("page")) {
            return new Page(xml.getAttribute("name"), xml.getAttribute("file"));
        } else if (type.equals("category")) {
            String name = xml.getAttribute("name");
            List<HelpNode> result = new ArrayList<>();
            NodeList nodes = xml.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    result.add(parse((Element) node));
                }
            }
            String page = xml.getAttribute("page");
            return new Category(name, page, result);
        } else if (type.equals("help")) {
            String name = "TRCView";
            List<HelpNode> result = new ArrayList<>();
            NodeList nodes = xml.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    result.add(parse((Element) node));
                }
            }
            String page = xml.getAttribute("page");
            return new Category(name, page, result);
        } else {
            return new Category("???", null, Collections.emptyList());
        }
    }

    public Object getRoot() {
        return root;
    }

    public Object getChild(Object parent, int index) {
        if (parent instanceof Category) {
            Category cat = (Category) parent;
            return cat.getChildren().get(index);
        }
        return null;
    }

    public int getChildCount(Object parent) {
        if (parent instanceof Category) {
            Category cat = (Category) parent;
            return cat.getChildren().size();
        } else {
            return 0;
        }
    }

    public boolean isLeaf(Object node) {
        return !(node instanceof Category);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
        // TODO Auto-generated method stub
    }

    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof Category) {
            Category cat = (Category) parent;
            return cat.getChildren().indexOf(child);
        } else {
            return 0;
        }
    }

    public void addTreeModelListener(TreeModelListener l) {
        // tree is immutable, listener is useless
    }

    public void removeTreeModelListener(TreeModelListener l) {
        // tree is immutable, listener is useless
    }
}
