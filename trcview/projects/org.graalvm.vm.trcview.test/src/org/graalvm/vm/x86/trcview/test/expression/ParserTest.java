package org.graalvm.vm.x86.trcview.test.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.text.ParseException;

import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.AddNode;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.DivNode;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.expression.ast.MulNode;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
import org.junit.Test;

public class ParserTest {
    @Test
    public void testVariable001() throws ParseException {
        Parser p = new Parser("hello");
        Expression expr = p.parseExpression();
        assertSame(VariableNode.class, expr.getClass());
        assertEquals("hello", ((VariableNode) expr).name);
    }

    @Test
    public void testVariable002() throws ParseException {
        Parser p = new Parser("$");
        Expression expr = p.parseExpression();
        assertSame(VariableNode.class, expr.getClass());
        assertEquals("$", ((VariableNode) expr).name);
    }

    @Test
    public void testExpression001() throws ParseException {
        Parser p = new Parser("(eax + 4) * 2 + 0xBEEF / mem(0x42)");
        Expression expr = p.parseExpression();
        assertSame(AddNode.class, expr.getClass());
        AddNode root = (AddNode) expr;
        assertSame(MulNode.class, root.left.getClass());
        assertSame(DivNode.class, root.right.getClass());

        MulNode mul = (MulNode) root.left;
        DivNode div = (DivNode) root.right;
        assertSame(AddNode.class, mul.left.getClass());
        assertSame(ValueNode.class, mul.right.getClass());

        AddNode add4 = (AddNode) mul.left;
        assertSame(VariableNode.class, add4.left.getClass());
        assertSame(ValueNode.class, add4.right.getClass());

        assertEquals("eax", ((VariableNode) add4.left).name);
        assertEquals(4, ((ValueNode) add4.right).value);

        assertEquals(2, ((ValueNode) mul.right).value);

        assertSame(ValueNode.class, div.left.getClass());
        assertSame(CallNode.class, div.right.getClass());

        assertEquals(0xBEEF, ((ValueNode) div.left).value);

        CallNode call = (CallNode) div.right;
        assertEquals("mem", call.name);
        assertEquals(1, call.args.size());
        assertSame(ValueNode.class, call.args.get(0).getClass());
        assertEquals(0x42, ((ValueNode) call.args.get(0)).value);
    }

    @Test
    public void testExpression002() throws ParseException {
        Parser p = new Parser("if(x == 49 && y == 42, mem(0) & 0xFF, rax | 0xF)");
        Expression expr = p.parseExpression();
        assertEquals("if((x == 49) && (y == 42), mem(0) & 255, rax | 15)", expr.toString());
    }
}
