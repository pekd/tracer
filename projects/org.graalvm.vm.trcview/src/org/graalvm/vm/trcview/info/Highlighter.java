package org.graalvm.vm.trcview.info;

import java.awt.Color;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class Highlighter {
    private final Map<Long, Color> highlightPC;
    private final List<HighlightInfo> highlightExpression;

    public static class HighlightInfo {
        public final String expression;
        public final Expression expr;
        public final Color color;

        public HighlightInfo(String expression, Color color) throws ParseException {
            this.expression = expression;
            this.color = color;
            this.expr = new Parser(expression).parse();
        }
    }

    public Highlighter() {
        highlightPC = new HashMap<>();
        highlightExpression = new ArrayList<>();
    }

    public Color getColor(CpuState state, TraceAnalyzer trc) {
        long pc = state.getPC();
        ExpressionContext ctx = new ExpressionContext(state, trc);
        for (HighlightInfo info : highlightExpression) {
            try {
                long value = info.expr.evaluate(ctx);
                if (value != 0) {
                    return info.color;
                }
            } catch (EvaluationException e) {
                // ignore for now
            }
        }
        return highlightPC.get(pc);
    }

    public void setColor(long pc, Color color) {
        if (color == null) {
            highlightPC.remove(pc);
        } else {
            highlightPC.put(pc, color);
        }
    }

    public Map<Long, Color> getColors() {
        return Collections.unmodifiableMap(highlightPC);
    }

    public List<HighlightInfo> getExpressions() {
        return Collections.unmodifiableList(highlightExpression);
    }
}
