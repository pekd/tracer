package org.graalvm.vm.trcview.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.data.Variable;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.trcview.ui.data.editor.DefaultElement;
import org.graalvm.vm.trcview.ui.data.editor.Element;
import org.graalvm.vm.trcview.ui.data.editor.Line;
import org.graalvm.vm.util.StringUtils;

public abstract class DataLine extends Line {
    protected final TraceAnalyzer trc;
    protected final long step;
    protected final long addr;
    protected final Type type;
    private List<Element> elements;

    protected boolean omitLabel = false;

    protected DataLine(long addr, Type type, long step, TraceAnalyzer trc) {
        this.addr = addr;
        this.type = type;
        this.step = step;
        this.trc = trc;
    }

    protected abstract void addData(List<Element> result);

    private List<Element> createElements() {
        StepFormat fmt = trc.getArchitecture().getFormat();

        List<Element> result = new ArrayList<>();
        String label = "";
        String address = fmt.formatAddress(addr);

        if (omitLabel) {
            label = StringUtils.repeat(" ", DataViewModel.NAME_WIDTH);
        } else {
            Symbol sym = trc.getSymbol(addr);
            if (sym != null && sym.getValue() == addr) {
                String name = sym.getName();
                if (name != null) {
                    label = StringUtils.pad(name, DataViewModel.NAME_WIDTH);
                }
            } else {
                Variable var = trc.getTypedMemory().get(addr);
                if (var != null && var.getAddress() == addr) {
                    String name = var.getName();
                    label = StringUtils.pad(name, DataViewModel.NAME_WIDTH);
                }
            }
        }

        result.add(new DefaultElement(address, Element.TYPE_COMMENT));
        result.add(new DefaultElement(" ", Element.TYPE_PLAIN));
        result.add(new DefaultElement(StringUtils.pad(label, DataViewModel.NAME_WIDTH), Element.TYPE_IDENTIFIER));
        result.add(new DefaultElement(" ", Element.TYPE_PLAIN));

        addData(result);

        return result;
    }

    @Override
    public List<Element> getElements() {
        if (elements == null) {
            elements = createElements();
        }
        return elements;
    }
}
