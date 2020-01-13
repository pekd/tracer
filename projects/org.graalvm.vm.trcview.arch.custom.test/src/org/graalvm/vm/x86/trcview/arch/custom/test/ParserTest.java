package org.graalvm.vm.x86.trcview.arch.custom.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.graalvm.vm.trcview.script.Parser;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.trcview.script.type.Type;
import org.junit.Test;

public class ParserTest extends TestSupport {
    @Test(timeout = DEFAULT_TIMEOUT)
    public void testStruct001() {
        String code = "typedef unsigned char u8;\n" +
                        "typedef unsigned short u16;\n" +
                        "\n" +
                        "struct State {\n" +
                        "    u16 registers[8];\n" +
                        "    u16 psw;\n" +
                        "    int state;\n" +
                        "};";
        Parser p = parse(code);

        Struct state = p.types.getStruct("State");
        assertNotNull(state);
        assertEquals("State", state.getName());
        assertEquals(3, state.getMembers().size());
        List<Member> members = state.getMembers();
        assertEquals("registers", members.get(0).name);
        assertEquals(ArrayType.class, members.get(0).type.getClass());
        ArrayType registers = (ArrayType) members.get(0).type;
        assertEquals(8, registers.getSize());
        assertEquals(PrimitiveType.class, registers.getType().getClass());
        PrimitiveType u16 = (PrimitiveType) registers.getType();
        assertEquals(BasicType.SHORT, u16.getBasicType());
        assertTrue(u16.isUnsigned());
        assertEquals("psw", members.get(1).name);
        assertEquals(PrimitiveType.class, members.get(1).type.getClass());
        u16 = (PrimitiveType) members.get(1).type;
        assertEquals(BasicType.SHORT, u16.getBasicType());
        assertTrue(u16.isUnsigned());
        assertEquals("state", members.get(2).name);
        assertEquals(PrimitiveType.class, members.get(2).type.getClass());
        PrimitiveType int_ = (PrimitiveType) members.get(2).type;
        assertEquals(BasicType.INT, int_.getBasicType());
        assertFalse(int_.isUnsigned());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testStructDecl001() {
        String code = "typedef unsigned char u8;\n" +
                        "typedef unsigned short u16;\n" +
                        "\n" +
                        "struct State;\n" +
                        "struct State {\n" +
                        "    u16 registers[8];\n" +
                        "    u16 psw;\n" +
                        "    int state;\n" +
                        "};";
        Parser p = parse(code);

        Struct state = p.types.getStruct("State");
        assertNotNull(state);
        assertEquals("State", state.getName());
        assertEquals(3, state.getMembers().size());
        List<Member> members = state.getMembers();
        assertEquals("registers", members.get(0).name);
        assertEquals(ArrayType.class, members.get(0).type.getClass());
        ArrayType registers = (ArrayType) members.get(0).type;
        assertEquals(8, registers.getSize());
        assertEquals(PrimitiveType.class, registers.getType().getClass());
        PrimitiveType u16 = (PrimitiveType) registers.getType();
        assertEquals(BasicType.SHORT, u16.getBasicType());
        assertTrue(u16.isUnsigned());
        assertEquals("psw", members.get(1).name);
        assertEquals(PrimitiveType.class, members.get(1).type.getClass());
        u16 = (PrimitiveType) members.get(1).type;
        assertEquals(BasicType.SHORT, u16.getBasicType());
        assertTrue(u16.isUnsigned());
        assertEquals("state", members.get(2).name);
        assertEquals(PrimitiveType.class, members.get(2).type.getClass());
        PrimitiveType int_ = (PrimitiveType) members.get(2).type;
        assertEquals(BasicType.INT, int_.getBasicType());
        assertFalse(int_.isUnsigned());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testAnonymousStruct001() {
        String code = "typedef unsigned char u8;\n" +
                        "typedef unsigned short u16;\n" +
                        "\n" +
                        "typedef struct State {\n" +
                        "    u16 registers[8];\n" +
                        "    u16 psw;\n" +
                        "    int state;\n" +
                        "} STATE;";
        Parser p = parse(code);

        Struct state = p.types.getStruct("State");
        assertNotNull(state);
        assertEquals("State", state.getName());
        assertEquals(3, state.getMembers().size());
        List<Member> members = state.getMembers();
        assertEquals("registers", members.get(0).name);
        assertEquals(ArrayType.class, members.get(0).type.getClass());
        ArrayType registers = (ArrayType) members.get(0).type;
        assertEquals(8, registers.getSize());
        assertEquals(PrimitiveType.class, registers.getType().getClass());
        PrimitiveType u16 = (PrimitiveType) registers.getType();
        assertEquals(BasicType.SHORT, u16.getBasicType());
        assertTrue(u16.isUnsigned());
        assertEquals("psw", members.get(1).name);
        assertEquals(PrimitiveType.class, members.get(1).type.getClass());
        u16 = (PrimitiveType) members.get(1).type;
        assertEquals(BasicType.SHORT, u16.getBasicType());
        assertTrue(u16.isUnsigned());
        assertEquals("state", members.get(2).name);
        assertEquals(PrimitiveType.class, members.get(2).type.getClass());
        PrimitiveType int_ = (PrimitiveType) members.get(2).type;
        assertEquals(BasicType.INT, int_.getBasicType());
        assertFalse(int_.isUnsigned());

        Type typedef = p.types.get("STATE");
        assertSame(state, typedef);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testScript001() {
        String code = "int f(int x);\n" +
                        "\n" +
                        "int func() {\n" +
                        "    int x = 42;\n" +
                        "    x = f(x);\n" +
                        "    return x;\n" +
                        "}\n" +
                        "\n" +
                        "int f(int x) {\n" +
                        "    return x * 42;\n" +
                        "}\n" +
                        "\n" +
                        "void g(int* x) {\n" +
                        "    x[42] = 0;\n" +
                        "}\n";
        Parser p = parse(code);

        Function func = p.symtab.getFunction("func");
        Function f = p.symtab.getFunction("f");
        Function g = p.symtab.getFunction("g");
        assertNotNull(func);
        assertNotNull(f);
        assertNotNull(g);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void struct1() {
        String code = "struct s {\n" +
                        "    int x;\n" +
                        "};\n" +
                        "\n" +
                        "int func() {\n" +
                        "    struct s x;\n" +
                        "    x.x = 42;\n" +
                        "}\n";
        Parser p = parse(code);

        Function func = p.symtab.getFunction("func");
        assertNotNull(func);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void struct2() {
        String code = "struct info {\n" +
                        "    long pc;\n" +
                        "};\n" +
                        "\n" +
                        "int func(struct info* info) {\n" +
                        "    if(info->pc == 0x42) {\n" +
                        "        return 1;\n" +
                        "    }\n" +
                        "    return 0;\n" +
                        "}\n";
        Parser p = parse(code);

        Function func = p.symtab.getFunction("func");
        assertNotNull(func);
    }
}
