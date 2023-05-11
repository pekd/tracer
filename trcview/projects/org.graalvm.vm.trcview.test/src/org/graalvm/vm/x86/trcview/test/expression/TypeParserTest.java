package org.graalvm.vm.x86.trcview.test.expression;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;

import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Field;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.NameAlreadyUsedException;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Struct;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.analysis.type.TypeAlias;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
import org.junit.Test;

public class TypeParserTest {
    @Test
    public void testMain001() throws ParseException {
        Parser p = new Parser("int main()");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(0, proto.args.size());
    }

    @Test
    public void testMain002() throws ParseException {
        Parser p = new Parser("int main(void)");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(0, proto.args.size());
    }

    @Test
    public void testMain003() throws ParseException {
        Parser p = new Parser("int main(int argc)");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.S32, proto.args.get(0).getType());
    }

    @Test
    public void testMain004() throws ParseException {
        Parser p = new Parser("int main(int argc, char** argv)");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(2, proto.args.size());
        assertEquals(DataType.S32, proto.args.get(0).getType());
        assertEquals(DataType.PTR, proto.args.get(1).getType());
        assertEquals(DataType.PTR, proto.args.get(1).getPointee().getType());
        assertEquals(DataType.S8, proto.args.get(1).getPointee().getPointee().getType());
    }

    @Test
    public void testRepr001() throws ParseException {
        Parser p = new Parser("void putch(char c)");
        Function fun = p.parsePrototype();
        assertEquals("putch", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.VOID, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.S8, proto.args.get(0).getType());
        assertEquals(Representation.DEC, proto.args.get(0).getRepresentation());
    }

    @Test
    public void testRepr002() throws ParseException {
        Parser p = new Parser("void putch(char $char)");
        Function fun = p.parsePrototype();
        assertEquals("putch", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.VOID, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.S8, proto.args.get(0).getType());
        assertEquals(Representation.CHAR, proto.args.get(0).getRepresentation());
    }

    @Test
    public void testRepr003() throws ParseException {
        Parser p = new Parser("void putch(char $char c)");
        Function fun = p.parsePrototype();
        assertEquals("putch", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.VOID, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.S8, proto.args.get(0).getType());
        assertEquals(Representation.CHAR, proto.args.get(0).getRepresentation());
    }

    @Test
    public void testExpr001() throws ParseException {
        Parser p = new Parser("u16<r0> checksum(void*<r0>)");
        Function fun = p.parsePrototype();
        assertEquals("checksum", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.U16, proto.returnType.getType());
        assertEquals(new VariableNode("r0"), proto.returnType.getExpression());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.PTR, proto.args.get(0).getType());
        assertEquals(Representation.HEX, proto.args.get(0).getRepresentation());
        assertEquals(new VariableNode("r0"), proto.args.get(0).getExpression());
    }

    @Test
    public void primitiveArray() throws ParseException {
        Parser p = new Parser("struct S { int x[4]; };");
        Struct s = p.parseStruct();
        Field x = s.getField("x");
        assertEquals("x", x.getName());
        assertEquals(4, x.getType().getElements());
        assertEquals(16, x.getType().getSize());
        assertEquals(DataType.S32, x.getType().getType());
    }

    @Test
    public void testStructPtr() throws ParseException {
        UserTypeDatabase db = new UserTypeDatabase(ArchitectureTypeInfo.LP64);
        try {
            db.add(new Struct("str"));
        } catch (NameAlreadyUsedException e) {
            // ignore
        }

        Parser p = new Parser("void func(struct str* ptr)", db);
        Function fun = p.parsePrototype();
        assertEquals("func", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.VOID, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.PTR, proto.args.get(0).getType());
        Type t = proto.args.get(0).getPointee();
        assertEquals(DataType.STRUCT, t.getType());
        assertEquals("str", t.getStruct().getName());
        assertEquals(0, t.getStruct().getSize());
    }

    @Test
    public void testUnsignedInt() throws ParseException {
        Parser p = new Parser("int main(unsigned int argc)");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.U32, proto.args.get(0).getType());
    }

    @Test
    public void testUnsigned() throws ParseException {
        Parser p = new Parser("int main(unsigned argc)");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.U32, proto.args.get(0).getType());
    }

    @Test
    public void testUnsignedAnonymous() throws ParseException {
        Parser p = new Parser("int main(unsigned)");
        Function fun = p.parsePrototype();
        assertEquals("main", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S32, proto.returnType.getType());
        assertEquals(1, proto.args.size());
        assertEquals(DataType.U32, proto.args.get(0).getType());
    }

    @Test
    public void testTypedef() throws ParseException {
        UserTypeDatabase db = new UserTypeDatabase(ArchitectureTypeInfo.LP64);
        try {
            db.add(new TypeAlias("mode_t", new Type(DataType.U32)));
        } catch (NameAlreadyUsedException e) {
            // ignore
        }

        Parser p = new Parser("long open(const char* filename, int flags, mode_t mode)", db);
        Function fun = p.parsePrototype();
        assertEquals("open", fun.getName());
        Prototype proto = fun.getPrototype();
        assertEquals(DataType.S64, proto.returnType.getType());
        assertEquals(3, proto.args.size());
        assertEquals(DataType.PTR, proto.args.get(0).getType());
        assertEquals(DataType.S32, proto.args.get(1).getType());
        assertEquals(DataType.U32, proto.args.get(2).getType());
    }
}
