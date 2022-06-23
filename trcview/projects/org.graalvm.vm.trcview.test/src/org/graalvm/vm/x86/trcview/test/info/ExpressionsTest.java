package org.graalvm.vm.x86.trcview.test.info;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.text.ParseException;

import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.info.Expressions;
import org.graalvm.vm.trcview.info.FormattedExpression;
import org.junit.Before;
import org.junit.Test;

public class ExpressionsTest {
    private final StepFormat format = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, false);

    private Expressions expressions;

    @Before
    public void setup() {
        expressions = new Expressions();
    }

    @Test
    public void test1() throws ParseException {
        expressions.setExpression(0, format, "test = %%noodle");
        FormattedExpression expr = expressions.getExpression(0);
        assertEquals("test = %noodle", expr.getFormat());
        assertEquals(0, expr.getExpressions().length);
        assertSame(format, expr.getStepFormat());
    }

    @Test
    public void test2() throws ParseException {
        expressions.setExpression(0, format, "test = %{rax}x");
        FormattedExpression expr = expressions.getExpression(0);
        assertEquals("test = %x", expr.getFormat());
        assertEquals(1, expr.getExpressions().length);
        assertSame(format, expr.getStepFormat());
    }

    @Test
    public void test3() throws ParseException, EvaluationException {
        expressions.setExpression(0, format, "test = %{42}x, %{1 + 1}d");
        FormattedExpression expr = expressions.getExpression(0);
        assertEquals("test = %x, %d", expr.getFormat());
        assertEquals(2, expr.getExpressions().length);
        assertSame(format, expr.getStepFormat());
        assertEquals("test = 2a, 2", expr.evaluate(new MockState(), null));
    }
}
