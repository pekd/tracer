package org.graalvm.vm.trcview.info;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class FormattedExpression {
    private final String raw;
    private final StepFormat format;
    private final String type;
    private final Expression[] expr;

    public FormattedExpression(StepFormat format, String expression) throws ParseException {
        List<Expression> expressions = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        StringBuilder fmt = new StringBuilder();
        int state = 0;
        int i = 0;
        for (char c : expression.toCharArray()) {
            i++;
            switch (state) {
                case 0: // TEXT
                    switch (c) {
                        case '%':
                            state = 1;
                            break;
                        case '\\':
                            state = 3;
                            break;
                        default:
                            fmt.append(c);
                    }
                    break;
                case 1: // '%'
                    if (c == '%') {
                        state = 0;
                        fmt.append('%');
                    } else if (c == '{') {
                        state = 2;
                        buf = new StringBuilder();
                    } else {
                        throw new ParseException("invalid character '" + c + "'", i);
                    }
                    break;
                case 2: // expression
                    if (c == '}') {
                        state = 0;
                        Parser parser = new Parser(buf.toString());
                        expressions.add(parser.parse());
                        fmt.append('%');
                    } else {
                        buf.append(c);
                    }
                    break;
                case 3: // escape
                    state = 0;
                    switch (c) {
                        case 'r':
                            buf.append('\r');
                            break;
                        case 'n':
                            buf.append('\n');
                            break;
                        case 't':
                            buf.append('\t');
                            break;
                        default:
                            throw new ParseException("invalid escape sequence '\\" + c + "'", i);
                    }
            }
        }
        this.format = format;
        this.type = fmt.toString();
        this.expr = expressions.toArray(new Expression[expressions.size()]);
        this.raw = expression;
    }

    public String getExpression() {
        return raw;
    }

    public StepFormat getStepFormat() {
        return format;
    }

    public String getFormat() {
        return type;
    }

    public Expression[] getExpressions() {
        return expr;
    }

    public String evaluate(CpuState step, TraceAnalyzer trc) throws EvaluationException {
        ExpressionContext ctx = new ExpressionContext(step, trc);
        long[] values = new long[expr.length];
        for (int i = 0; i < expr.length; i++) {
            values[i] = expr[i].evaluate(ctx);
        }
        return Formatter.format(type, format, step.getStep(), trc, values);
    }

    @Override
    public String toString() {
        return "FormattedExpression[" + raw + "]";
    }
}
