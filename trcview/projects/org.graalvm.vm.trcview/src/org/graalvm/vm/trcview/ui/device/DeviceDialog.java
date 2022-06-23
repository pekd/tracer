package org.graalvm.vm.trcview.ui.device;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.analysis.device.DeviceType;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.JumpListener;
import org.graalvm.vm.trcview.ui.event.StepListenable;
import org.graalvm.vm.trcview.ui.event.TraceListenable;
import org.graalvm.vm.trcview.ui.event.TraceListener;

@SuppressWarnings("serial")
public class DeviceDialog extends JDialog {
    private Map<Integer, Device> devices;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;

    public DeviceDialog(JFrame owner, TraceAnalyzer trc, JumpListener jump, StepListenable step, TraceListenable trace) {
        super(owner, "Device", false);

        setLayout(new BorderLayout());

        JTree tree;
        DeviceView deviceView;

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        root = new DefaultMutableTreeNode("Devices");
        split.setLeftComponent(new JScrollPane(tree = new JTree(model = new DefaultTreeModel(root, true))));
        split.setRightComponent(deviceView = new DeviceView(jump, trc));
        split.setResizeWeight(0.1);

        tree.addTreeSelectionListener(e -> {
            TreePath path = e.getPath();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.isLeaf() && !node.getAllowsChildren()) {
                Device dev = (Device) node.getUserObject();
                deviceView.setDevice(dev);
            }
        });

        step.addStepListener(deviceView);
        TraceListener listener = this::setTraceAnalyzer;
        trace.addTraceListener(listener);

        add(BorderLayout.CENTER, split);

        KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        deviceView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, esc);
        deviceView.getActionMap().put(esc, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                step.removeStepListener(deviceView);
                dispose();
            }
        });

        if (trc != null) {
            setTraceAnalyzer(trc);
        }

        setSize(800, 600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                step.removeStepListener(deviceView);
                trace.removeTraceListener(listener);
                dispose();
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        devices = trc.getDevices();
        buildTree();
    }

    private void buildTree() {
        Map<DeviceType, List<Device>> devs = new HashMap<>();
        for (Device dev : devices.values()) {
            List<Device> list = devs.get(dev.getType());
            if (list == null) {
                devs.put(dev.getType(), list = new ArrayList<>());
            }
            list.add(dev);
        }

        root.removeAllChildren();
        devs.entrySet().stream().sorted((a, b) -> a.getKey().getName().compareTo(b.getKey().getName())).forEach(type -> {
            DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(type.getKey().getName());
            type.getValue().stream().sorted((a, b) -> a.getName().compareTo(b.getName())).forEach(dev -> {
                typeNode.add(new DefaultMutableTreeNode(dev, false));
            });
            root.add(typeNode);
        });
        model.nodeStructureChanged(root);
    }
}
