package org.graalvm.vm.x86.trcview.test.expression;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.graalvm.vm.x86.trcview.analysis.type.DataType;
import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.expression.TypeParser;
import org.junit.Test;

public class TypeParserTest {
    @Test
    public void testMain001() throws ParseException {
        TypeParser p = new TypeParser("int main()");
        Function fun = p.parse();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(0, proto.args.size());
    }

    @Test
    public void testMain002() throws ParseException {
        TypeParser p = new TypeParser("int main(void)");
        Function fun = p.parse();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(0, proto.args.size());
    }

    @Test
    public void testMain003() throws ParseException {
        TypeParser p = new TypeParser("int main(int argc)");
        Function fun = p.parse();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.S32, proto.args.get(0).getType());
    }

    @Test
    public void testMain004() throws ParseException {
        TypeParser p = new TypeParser("int main(int argc, char** argv)");
        Function fun = p.parse();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(2, proto.args.size());
        assertEquals(DataType.S32, proto.args.get(0).getType());
        assertEquals(DataType.PTR, proto.args.get(1).getType());
        assertEquals(DataType.PTR, proto.args.get(1).getPointee().getType());
        assertEquals(DataType.S8, proto.args.get(1).getPointee().getPointee().getType());
    }
}
