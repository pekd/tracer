package org.graalvm.vm.trcview.ui.device;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.graalvm.vm.trcview.analysis.device.Device;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.event.JumpListener;
import org.graalvm.vm.trcview.ui.event.StepListener;

@SuppressWarnings("serial")
public class DeviceView extends JPanel implements StepListener {
    private JLabel name;
    private DeviceEventView events;
    private DeviceRegisterView regs;

    public DeviceView(JumpListener jump, TraceAnalyzer trc) {
        super(new BorderLayout());
        add(BorderLayout.NORTH, name = new JLabel("No device"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(regs = new DeviceRegisterView());
        split.setBottomComponent(events = new DeviceEventView(jump));
        split.setResizeWeight(0.5);

        add(BorderLayout.CENTER, split);

        events.setTraceAnalyzer(trc);
    }

    public void setDevice(Device dev) {
        name.setText(dev.getName());
        events.setDevice(dev);
        regs.setDevice(dev);
    }

    @Override
    public void setStep(StepEvent step) {
        events.setStep(step);
        regs.setStep(step);
    }
}
