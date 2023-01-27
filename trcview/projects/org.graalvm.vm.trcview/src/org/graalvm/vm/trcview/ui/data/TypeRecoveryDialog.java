package org.graalvm.vm.trcview.ui.data;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.ArrayRecovery;
import org.graalvm.vm.trcview.data.ChainTarget;
import org.graalvm.vm.trcview.data.CodeSemantics;
import org.graalvm.vm.trcview.data.MemoryChainTarget;
import org.graalvm.vm.trcview.data.RegisterChainTarget;
import org.graalvm.vm.trcview.data.RegisterTypeMap;
import org.graalvm.vm.trcview.data.SemanticInfo;
import org.graalvm.vm.trcview.data.Semantics;
import org.graalvm.vm.trcview.data.ir.RegisterOperand;
import org.graalvm.vm.trcview.data.type.VariableType;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.MainWindow;
import org.graalvm.vm.trcview.ui.Utils;
import org.graalvm.vm.trcview.ui.event.StepListenable;
import org.graalvm.vm.trcview.ui.event.StepListener;
import org.graalvm.vm.trcview.ui.event.TraceListenable;
import org.graalvm.vm.trcview.ui.event.TraceListener;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;

@SuppressWarnings("serial")
public class TypeRecoveryDialog extends JDialog implements StepListener {
    private static final boolean debug = false;

    private TraceAnalyzer trc;
    private StepEvent step;

    private JTextPane text;

    private JComboBox<String> regSelector;
    private GraphModel graph;

    public TypeRecoveryDialog(JFrame owner, TraceAnalyzer trc, StepListenable step, TraceListenable trace) {
        super(owner, "Type Recovery", false);

        setLayout(new BorderLayout());

        JLabel status = new JLabel("Ready");

        JTabbedPane tabs = new JTabbedPane();

        JPanel types = new JPanel(new BorderLayout());
        tabs.addTab("Type Recovery", types);

        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/plain");
        types.add(BorderLayout.CENTER, new JScrollPane(text));
        types.add(BorderLayout.SOUTH, status);

        JPanel debugTrace = new JPanel(new BorderLayout());
        tabs.addTab("Debug Trace", debugTrace);

        regSelector = new JComboBox<>(new RegisterModel(0));
        regSelector.addItemListener(e -> updateDebugTrace());
        debugTrace.add(BorderLayout.NORTH, regSelector);

        JList<String> list = new JList<>(graph = new GraphModel());
        list.setFont(MainWindow.FONT);
        debugTrace.add(BorderLayout.CENTER, new JScrollPane(list));

        add(BorderLayout.CENTER, tabs);

        if (trc != null) {
            setTraceAnalyzer(trc);
        }

        step.addStepListener(this);

        TraceListener listener = this::setTraceAnalyzer;
        trace.addTraceListener(listener);

        setSize(800, 600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                step.removeStepListener(TypeRecoveryDialog.this);
                trace.removeTraceListener(listener);
                dispose();
            }
        });
    }

    public void setTraceAnalyzer(TraceAnalyzer trc) {
        this.trc = trc;
        StepEvent s = Utils.getStep(trc.getInstruction(0));
        if (s != null) {
            setStep(s);
        }
        int regcnt = trc.getArchitecture().getRegisterCount();
        regSelector.setModel(new RegisterModel(regcnt));
        if (regcnt > 0) {
            regSelector.setSelectedIndex(0);
        }
    }

    public void setStep(StepEvent step) {
        this.step = step;
        int regcount = trc.getArchitecture().getRegisterCount();
        long pc = step.getPC();
        StringBuilder buf = new StringBuilder();
        if (trc.getTypeRecovery() == null) {
            return;
        }

        Semantics semantics = trc.getTypeRecovery().getSemantics();
        for (int i = 0; i < regcount; i++) {
            long flags = semantics.resolve(pc, new RegisterOperand(i));
            long flowflags = semantics.resolveData(pc, new RegisterOperand(i));

            flags &= ~VariableType.SOLVED.getMask();

            boolean live = semantics.isLive(pc, i);
            for (int reg : step.getRegisterWrites()) {
                if (reg == i) {
                    live = true;
                    break;
                }
            }

            buf.append("register ");
            buf.append(String.format("%02d", i));
            buf.append(":");

            buf.append(" (");
            buf.append(HexFormatter.tohex(flags, 16));
            buf.append(")");

            buf.append(" [");
            buf.append(live ? 'L' : 'D');
            if (BitTest.test(flowflags, VariableType.MUL_BIT)) {
                buf.append('M');
            } else {
                buf.append('-');
            }
            if (BitTest.test(flowflags, VariableType.ADDSUB_BIT)) {
                buf.append('A');
            } else {
                buf.append('-');
            }
            buf.append("]");

            buf.append(":[");
            if (BitTest.test(flags, VariableType.MUL_BIT)) {
                buf.append('M');
            } else {
                buf.append('-');
            }
            if (BitTest.test(flags, VariableType.ADDSUB_BIT)) {
                buf.append('A');
            } else {
                buf.append('-');
            }
            buf.append("]");

            if (flags == 0) {
                buf.append(" [ ] --\n");
            } else if (flags == VariableType.BREAK_BIT) {
                buf.append(" [B] --\n");
            } else if (flags == VariableType.CHAIN_BIT) {
                buf.append(" [C] --\n");
            } else {
                if (BitTest.test(flags, VariableType.BREAK_BIT)) {
                    buf.append(" [B]");
                } else if (BitTest.test(flags, VariableType.CHAIN_BIT)) {
                    buf.append(" [C]");
                } else {
                    buf.append(" [ ]");
                }
                for (VariableType type : VariableType.getTypeConstraints()) {
                    if (BitTest.test(flags, type.getMask())) {
                        buf.append(' ');
                        buf.append(type.getName());
                    }
                }
                buf.append(" => ");
                buf.append(VariableType.resolve(flags, trc.getArchitecture().getTypeInfo().getPointerSize()));
                buf.append('\n');
            }
        }

        int[] reads = step.getRegisterReads();
        int[] writes = step.getRegisterWrites();

        buf.append("\nRegister reads:  ").append(IntStream.of(reads).sorted().mapToObj(x -> "r" + x).collect(Collectors.joining(", ")));
        buf.append("\nRegister writes: ").append(IntStream.of(writes).sorted().mapToObj(x -> "r" + x).collect(Collectors.joining(", ")));

        buf.append('\n');

        // memory accesses
        buf.append("\n\nMEMORY:\n");
        if (debug) {
            buf.append(semantics.getSteps(pc).stream().sorted((a, b) -> Long.compareUnsigned(a.getStep(), b.getStep())).map(x -> Long.toUnsignedString(x.getStep())).collect(Collectors.joining(" ")));
        }
        buf.append("\n=> R: ");
        buf.append(LongStream.of(semantics.getDataReads(pc)).sorted().mapToObj(HexFormatter::tohex).collect(Collectors.joining(" ")));
        buf.append("\n=> W: ");
        buf.append(LongStream.of(semantics.getDataWrites(pc)).sorted().mapToObj(HexFormatter::tohex).collect(Collectors.joining(" ")));
        buf.append("\n\nARRAY: ");
        buf.append(ArrayRecovery.recoverArray(semantics, pc, true));

        // debugging feature: show data per position
        buf.append("\n\nDEBUG:\n");
        for (int i = 0; i < regcount; i++) {
            long flags = semantics.getDirect(pc, new RegisterOperand(i));
            buf.append("register ");
            buf.append(String.format("%02d", i));
            buf.append(":");
            buf.append(" [");
            if (BitTest.test(flags, VariableType.MUL_BIT)) {
                buf.append('M');
            } else {
                buf.append('-');
            }
            if (BitTest.test(flags, VariableType.ADDSUB_BIT)) {
                buf.append('A');
            } else {
                buf.append('-');
            }
            buf.append("]");
            if (flags == 0) {
                buf.append(" [ ] --\n");
            } else if (flags == VariableType.CHAIN_BIT) {
                buf.append(" [C] --\n");
            } else {
                if (BitTest.test(flags, VariableType.CHAIN_BIT)) {
                    buf.append(" [C]");
                } else {
                    buf.append(" [ ]");
                }
                for (VariableType type : VariableType.getTypeConstraints()) {
                    if (BitTest.test(flags, type.getMask())) {
                        buf.append(' ');
                        buf.append(type.getName());
                    }
                }
                buf.append('\n');
            }
        }

        if (debug) {
            StepFormat fmt = trc.getArchitecture().getFormat();
            buf.append("\nLinks:\n");
            buf.append("Implicit: ").append(fmt.formatAddress(semantics.getChain(pc))).append('\n');
            buf.append("Explicit:");
            for (RegisterTypeMap map : semantics.getExtraChain(pc)) {
                buf.append(' ');
                buf.append(fmt.formatAddress(map.getPC()));
            }
            buf.append("\nForward:");
            for (RegisterTypeMap map : semantics.getForwardChain(pc)) {
                buf.append(' ');
                buf.append(fmt.formatAddress(map.getPC()));
            }
            buf.append("\nClosure per register:\n");

            for (int i = 0; i < regcount; i++) {
                List<ChainTarget> set = new ArrayList<>();
                semantics.resolve(pc, new RegisterOperand(i), set);
                buf.append("register ");
                buf.append(String.format("%02d", i));
                buf.append(":");
                set.stream().map(x -> {
                    if (x instanceof RegisterChainTarget) {
                        RegisterChainTarget tgt = (RegisterChainTarget) x;
                        return fmt.formatAddress(tgt.map.getPC()) + "[r" + tgt.register + "]";
                    } else if (x instanceof MemoryChainTarget) {
                        MemoryChainTarget tgt = (MemoryChainTarget) x;
                        return "[" + fmt.formatAddress(tgt.address) + "]";
                    } else {
                        return "?";
                    }
                }).forEach(x -> buf.append(' ').append(x));
                buf.append('\n');
            }

            buf.append("\n\nLinks per register:\n");
            CodeSemantics cs = (CodeSemantics) semantics;
            for (int i = 0; i < regcount; i++) {
                RegisterTypeMap map = cs.get(pc);
                Set<ChainTarget> forward = map.getForwardChain(i);
                Set<ChainTarget> reverse = map.getReverseChain(i);

                buf.append("register ");
                buf.append(String.format("%02d", i));
                buf.append(":\n");
                buf.append("forward = ");
                buf.append(forward.stream().map(x -> {
                    if (x instanceof RegisterChainTarget) {
                        RegisterChainTarget t = (RegisterChainTarget) x;
                        return fmt.formatAddress(t.map.getPC()) + "[r" + t.register + "]";
                    } else if (x instanceof MemoryChainTarget) {
                        MemoryChainTarget t = (MemoryChainTarget) x;
                        return fmt.formatAddress(t.address) + "[step=" + t.step + "]";
                    } else {
                        return "???";
                    }
                }).sorted().collect(Collectors.joining(" ")));
                buf.append("\nreverse = ");
                buf.append(reverse.stream().map(x -> {
                    if (x instanceof RegisterChainTarget) {
                        RegisterChainTarget t = (RegisterChainTarget) x;
                        return fmt.formatAddress(t.map.getPC()) + "[r" + t.register + "]";
                    } else if (x instanceof MemoryChainTarget) {
                        MemoryChainTarget t = (MemoryChainTarget) x;
                        return fmt.formatAddress(t.address) + "[step=" + t.step + "]";
                    } else {
                        return "???";
                    }
                }).sorted().collect(Collectors.joining(" ")));

                buf.append("\n");
            }
        }

        SemanticInfo info = new SemanticInfo();
        step.getSemantics(info);

        buf.append("\nSemantics:");
        for (String line : info.getOperations()) {
            buf.append('\n').append(line);
        }

        text.setText(buf.toString());
        text.setCaretPosition(0);

        updateDebugTrace();
    }

    private void updateDebugTrace() {
        if (trc == null || step == null) {
            return;
        }

        int regcount = trc.getArchitecture().getRegisterCount();
        int reg = regSelector.getSelectedIndex();
        if (reg < 0 || reg >= regcount) {
            return;
        }

        long pc = step.getPC();
        if (trc.getTypeRecovery() == null) {
            return;
        }

        Semantics semantics = trc.getTypeRecovery().getSemantics();

        List<ChainTarget> list = new ArrayList<>();
        semantics.resolve(pc, new RegisterOperand(reg), list);

        List<String> result = graph.getList();
        result.clear();
        result.add("Graph Size: " + list.size());
        int ptrsz = trc.getArchitecture().getTypeInfo().getPointerSize();
        StepFormat fmt = trc.getArchitecture().getFormat();
        for (ChainTarget target : list) {
            long flags = 0;
            StringBuilder buf = new StringBuilder();
            if (target instanceof RegisterChainTarget) {
                RegisterChainTarget tgt = (RegisterChainTarget) target;
                int r = tgt.register;
                flags = tgt.map.getDirect(r);
                long ip = tgt.map.getPC();

                buf.append(fmt.formatAddress(ip));
                buf.append(": r");
                buf.append(r);
            } else if (target instanceof MemoryChainTarget) {
                MemoryChainTarget tgt = (MemoryChainTarget) target;

                buf.append("[");
                buf.append(fmt.formatAddress(tgt.address));
                buf.append("@");
                buf.append(tgt.step);
                buf.append("]");
            } else {
                buf.append("[");
                buf.append(target.getClass().getSimpleName());
                buf.append("]");
            }

            int len = buf.length();
            for (int i = len; i < 32; i++) {
                buf.append(' ');
            }

            buf.append(" = (");
            buf.append(HexFormatter.tohex(flags, 16));
            buf.append(") => [");
            if (BitTest.test(flags, VariableType.MUL_BIT)) {
                buf.append('M');
            } else {
                buf.append('-');
            }
            if (BitTest.test(flags, VariableType.ADDSUB_BIT)) {
                buf.append('A');
            } else {
                buf.append('-');
            }
            buf.append("]");

            if (flags == 0) {
                buf.append(" [ ] --\n");
            } else if (flags == VariableType.BREAK_BIT) {
                buf.append(" [B] --\n");
            } else if (flags == VariableType.CHAIN_BIT) {
                buf.append(" [C] --\n");
            } else {
                if (BitTest.test(flags, VariableType.BREAK_BIT)) {
                    buf.append(" [B]");
                } else if (BitTest.test(flags, VariableType.CHAIN_BIT)) {
                    buf.append(" [C]");
                } else {
                    buf.append(" [ ]");
                }
                for (VariableType type : VariableType.getTypeConstraints()) {
                    if (BitTest.test(flags, type.getMask())) {
                        buf.append(' ');
                        buf.append(type.getName());
                    }
                }
                buf.append(" => ");
                buf.append(VariableType.resolve(flags, ptrsz));
                buf.append('\n');
            }

            result.add(buf.toString());
        }

        graph.changed();
    }

    private static class RegisterModel extends DefaultComboBoxModel<String> {
        private int regcnt;

        public RegisterModel(int regcnt) {
            this.regcnt = regcnt;
        }

        @Override
        public int getSize() {
            return regcnt;
        }

        @Override
        public String getElementAt(int index) {
            return "Register " + index;
        }
    }

    private static class GraphModel extends AbstractListModel<String> {
        private List<String> list;

        public GraphModel() {
            list = new ArrayList<>();
        }

        public List<String> getList() {
            return list;
        }

        public int getSize() {
            return list.size();
        }

        public String getElementAt(int index) {
            return list.get(index);
        }

        public void changed() {
            fireContentsChanged(this, 0, getSize());
        }
    }
}
