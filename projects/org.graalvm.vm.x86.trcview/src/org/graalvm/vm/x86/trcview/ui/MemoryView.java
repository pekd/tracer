package org.graalvm.vm.x86.trcview.ui;

import static org.graalvm.vm.x86.trcview.ui.Utils.color;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.x86.node.debug.trace.StepRecord;
import org.graalvm.vm.x86.trcview.analysis.Search;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryRead;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryTrace;
import org.graalvm.vm.x86.trcview.analysis.memory.MemoryUpdate;
import org.graalvm.vm.x86.trcview.analysis.memory.VirtualMemorySnapshot;
import org.graalvm.vm.x86.trcview.expression.EvaluationException;
import org.graalvm.vm.x86.trcview.expression.ExpressionContext;
import org.graalvm.vm.x86.trcview.expression.Parser;
import org.graalvm.vm.x86.trcview.expression.ast.Expression;
import org.graalvm.vm.x86.trcview.io.BlockNode;
import org.graalvm.vm.x86.trcview.io.Node;
import org.graalvm.vm.x86.trcview.io.RecordNode;
import org.graalvm.vm.x86.trcview.ui.event.JumpListener;

@SuppressWarnings("serial")
public class MemoryView extends JPanel {
    private static final int LINESZ = 16;

    private static final String STYLE = "<style>\n" +
                    "html, body, pre {\n" +
                    "    padding: 0;\n" +
                    "    margin: 0;\n" +
                    "}\n" +
                    "pre {\n" +
                    "    font-family: " + Font.MONOSPACED + ";\n" +
                    "    font-size: 11pt;\n" +
                    "}\n" +
                    ".address {\n" +
                    "    color: " + color(Color.ORANGE) + ";\n" +
                    "    background-color: " + color(Color.BLACK) + ";\n" +
                    "}\n" +
                    ".change {\n" +
                    "    color: " + color(Color.RED) + ";\n" +
                    "}\n" +
                    ".highlight {\n" +
                    "    color: " + color(Color.MAGENTA) + ";\n" +
                    "}\n" +
                    ".changeaddr {\n" +
                    "    color: " + color(Color.RED) + ";\n" +
                    "    background-color: " + color(Color.BLACK) + ";\n" +
                    "}\n" +
                    "</style>";

    private JTextPane text;
    private JTextField addrinput;
    private MemoryTrace memory;
    private long address;
    private long highlightStart;
    private long highlightEnd;
    private long insn;
    private StepRecord step;
    private Expression expr;
    private Node lastUpdateNode;
    private Node nextReadNode;
    private JButton gotoLastUpdate;
    private JButton gotoNextRead;

    private Consumer<String> status;

    public MemoryView(Consumer<String> status, JumpListener jump) {
        super(new BorderLayout());
        this.status = status;

        text = new JTextPane();
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        text.setEditable(false);
        text.setContentType("text/html");
        add(BorderLayout.CENTER, new JScrollPane(text));

        addrinput = new JTextField("0x" + HexFormatter.tohex(0, 16));
        int sz = addrinput.getFont().getSize();
        addrinput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, sz));
        addrinput.setForeground(Color.BLACK);
        addrinput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String s = addrinput.getText();
                Parser parser = new Parser(s.trim());
                try {
                    expr = parser.parse();
                    addrinput.setForeground(Color.BLACK);
                    status.accept("Expression parsed successfully");
                    if (eval() != address) {
                        update();
                    }
                } catch (ParseException ex) {
                    addrinput.setForeground(Color.RED);
                    status.accept("Parse error: " + ex.getMessage());
                }
            }
        });

        add(BorderLayout.NORTH, addrinput);

        gotoLastUpdate = new JButton("Goto last update");
        gotoLastUpdate.setEnabled(lastUpdateNode != null);
        gotoLastUpdate.addActionListener(e -> {
            if (lastUpdateNode != null) {
                jump.jump(lastUpdateNode);
            }
        });

        gotoNextRead = new JButton("Goto next read");
        gotoNextRead.setEnabled(nextReadNode != null);
        gotoNextRead.addActionListener(e -> {
            if (nextReadNode != null) {
                jump.jump(nextReadNode);
            }
        });

        JPanel gotoButtons = new JPanel(new GridLayout(1, 2));
        gotoButtons.add(gotoLastUpdate);
        gotoButtons.add(gotoNextRead);
        add(BorderLayout.SOUTH, gotoButtons);
    }

    private static String html(char c) {
        switch (c) {
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            default:
                return Character.toString(c);
        }
    }

    public void setHighlight(long start, long end) {
        highlightStart = start;
        highlightEnd = end;
        update();
    }

    public void setStep(StepRecord step) {
        if (this.step == step) {
            return;
        }
        this.step = step;
        this.insn = step.getInstructionCount();
        update();
    }

    public void setMemoryTrace(MemoryTrace memory) {
        this.memory = memory;
        update();
    }

    private static boolean isPrintable(byte value) {
        return value >= 0x20 && value <= 0x7e; // ascii
    }

    private byte getI8(long addr) throws MemoryNotMappedException {
        return memory.getByte(addr, insn);
    }

    private boolean isHighlight(long ptr) {
        return Long.compareUnsigned(ptr, highlightStart) >= 0 && Long.compareUnsigned(ptr, highlightEnd) <= 0;
    }

    private boolean isChanged(long addr) {
        try {
            long prev = insn - 1;
            if (insn == 0) {
                prev = 0;
            }
            byte val1 = memory.getByte(addr, prev);
            byte val2 = memory.getByte(addr, insn);
            return val1 != val2;
        } catch (MemoryNotMappedException e) {
            return false;
        }
    }

    private long eval() {
        if (expr == null) {
            return 0;
        } else if (step == null) {
            return 0;
        } else {
            VirtualMemorySnapshot mem = new VirtualMemorySnapshot(memory, insn);
            ExpressionContext ctx = new ExpressionContext(step, mem);
            try {
                return expr.evaluate(ctx);
            } catch (EvaluationException e) {
                status.accept("Error evaluating expression: " + e.getMessage());
                return 0;
            }
        }
    }

    private String dump(long p, int size) {
        StringBuilder buf = new StringBuilder();
        long ptr = p;
        long ptr2 = ptr;
        boolean nl = true;
        byte[] line = new byte[LINESZ];
        boolean[] linevalid = new boolean[LINESZ];
        boolean[] linechange = new boolean[LINESZ];
        for (int i = 0; i < size; i++) {
            byte u8;
            nl = true;
            if (i % LINESZ == 0) {
                buf.append(HexFormatter.tohex(ptr, 16)).append(':');
            }
            buf.append(' ');
            if (isHighlight(ptr)) {
                buf.append("<span class=\"highlight\">");
            }
            boolean change = isChanged(ptr);
            if (change) {
                if (ptr == address) {
                    buf.append("<span class=\"changeaddr\">");
                } else {
                    buf.append("<span class=\"change\">");
                }
            } else if (ptr == address) {
                buf.append("<span class=\"address\">");
            }
            try {
                u8 = getI8(ptr);
                line[i % LINESZ] = u8;
                linevalid[i % LINESZ] = true;
                linechange[i % LINESZ] = change;
                buf.append(HexFormatter.tohex(Byte.toUnsignedInt(u8), 2));
            } catch (MemoryNotMappedException e) {
                linevalid[i % LINESZ] = false;
                buf.append("--");
            }
            if (change) {
                buf.append("</span>");
            }
            if (ptr == address) {
                buf.append("</span>");
            }
            if (isHighlight(ptr)) {
                buf.append("</span>");
            }
            ptr++;
            if (i % LINESZ == (LINESZ - 1)) {
                buf.append("   ");
                for (int j = 0; j < LINESZ; j++) {
                    if (linevalid[j % LINESZ]) {
                        u8 = line[j % LINESZ];
                    } else {
                        u8 = '?';
                    }
                    char ch = (char) (u8 & 0xff);
                    if (!isPrintable(u8)) {
                        ch = '.';
                    }
                    if (isHighlight(ptr2)) {
                        buf.append("<span class=\"highlight\">");
                    }
                    change = linechange[j % LINESZ] && linevalid[j % LINESZ];
                    if (change) {
                        if (ptr2 == address) {
                            buf.append("<span class=\"changeaddr\">");
                        } else {
                            buf.append("<span class=\"change\">");
                        }
                    } else if (ptr2 == address) {
                        buf.append("<span class=\"address\">");
                    }
                    buf.append(html(ch));
                    if (change) {
                        buf.append("</span>");
                    }
                    if (ptr2 == address) {
                        buf.append("</span>");
                    }
                    if (isHighlight(ptr2)) {
                        buf.append("</span>");
                    }
                    ptr2++;
                }
                buf.append('\n');
                nl = false;
            }
        }
        if (nl) {
            buf.append('\n');
        }
        return buf.toString();
    }

    private void update() {
        address = eval();
        String content = "";
        if (memory != null) {
            content = dump((address - 4 * LINESZ) & 0xFFFFFFFFFFFFFFF0L, 12 * LINESZ) + "\n\n";
            try {
                MemoryUpdate update = memory.getLastWrite(address, insn);
                Node node = null;
                if (update != null) {
                    node = update.node;
                } else {
                    node = memory.getMapNode(address, insn);
                }
                if (node != null) {
                    content += "Last write to this location:\n";
                    if (node instanceof RecordNode && ((RecordNode) node).getRecord() instanceof StepRecord) {
                        lastUpdateNode = node;
                        StepRecord record = (StepRecord) ((RecordNode) node).getRecord();
                        content += "0x" + HexFormatter.tohex(record.getPC(), 16) + ": " + record.getDisassembly() + " # instruction " + record.getInstructionCount();
                    } else {
                        Node lastStep = Search.previousStep(node);
                        lastUpdateNode = lastStep;
                        if (lastStep != null) {
                            StepRecord record = null;
                            if (lastStep instanceof BlockNode) {
                                record = ((BlockNode) lastStep).getHead();
                            } else {
                                record = (StepRecord) ((RecordNode) lastStep).getRecord();
                            }
                            if (record != null) {
                                content += "0x" + HexFormatter.tohex(record.getPC(), 16) + ": " + record.getDisassembly() + " # instruction " + record.getInstructionCount();
                            } else {
                                content += "Written by the kernel";
                            }
                        }
                    }
                    content += "\n" + node;
                } else {
                    lastUpdateNode = null;
                    content += "No write to this address found";
                }

                MemoryRead read = memory.getNextRead(address, insn);
                if (read != null) {
                    content += "\n\nNext read from this location:\n";
                    if (read.node instanceof RecordNode && ((RecordNode) read.node).getRecord() instanceof StepRecord) {
                        nextReadNode = read.node;
                        StepRecord record = (StepRecord) ((RecordNode) read.node).getRecord();
                        content += "0x" + HexFormatter.tohex(record.getPC(), 16) + ": " + record.getDisassembly() + " # instruction " + record.getInstructionCount();
                    } else {
                        Node lastStep = Search.previousStep(read.node);
                        nextReadNode = lastStep;
                        if (lastStep != null) {
                            StepRecord record = null;
                            if (lastStep instanceof BlockNode) {
                                record = ((BlockNode) lastStep).getHead();
                            } else {
                                record = (StepRecord) ((RecordNode) lastStep).getRecord();
                            }
                            if (record != null) {
                                content += "0x" + HexFormatter.tohex(record.getPC(), 16) + ": " + record.getDisassembly() + " # instruction " + record.getInstructionCount();
                            } else {
                                content += "Read by the kernel";
                            }
                        }
                    }
                    content += "\n" + read.node;
                } else {
                    nextReadNode = null;
                    content += "\n\nNo read from this address found";
                }
            } catch (MemoryNotMappedException e) {
                lastUpdateNode = null;
                nextReadNode = null;
                content += "Memory not mapped at this point in time";
            }
        }
        String html = "<html><head>" + STYLE + "</head><body><pre>" + content + "</pre></body></html>";
        text.setText(html);
        text.setCaretPosition(0);

        gotoLastUpdate.setEnabled(lastUpdateNode != null);
        gotoNextRead.setEnabled(nextReadNode != null);
    }
}
