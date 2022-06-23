package org.graalvm.vm.x86.trcview.arch.custom.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.graalvm.vm.trcview.script.Parser;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.rt.Pointer;
import org.graalvm.vm.trcview.script.rt.Record;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.junit.Ignore;
import org.junit.Test;

public class EvalTest extends TestSupport {
    @Test(timeout = DEFAULT_TIMEOUT)
    public void main() {
        String code = "void main() {\n" +
                        "    int x = 42;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        main.execute(ctx);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void intmain() {
        String code = "int main() {\n" +
                        "    return 42;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx);
        assertEquals(42, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void mainargs1() {
        String code = "int main(int x) {\n" +
                        "    return x;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx, 42L);
        assertEquals(42, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void mainargs2() {
        String code = "int main(int x) {\n" +
                        "    return x * 2;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx, 42L);
        assertEquals(84, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void ifthenelse1() {
        String code = "int main(int x) {\n" +
                        "    if(x) {\n" +
                        "        return 42;\n" +
                        "    } else {\n" +
                        "        return 21;\n" +
                        "    }\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx, 1L);
        assertEquals(42, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void ifthenelse2() {
        String code = "int main(int x) {\n" +
                        "    if(x) {\n" +
                        "        return 42;\n" +
                        "    } else {\n" +
                        "        return 21;\n" +
                        "    }\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx, 0L);
        assertEquals(21, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void array1() {
        String code = "void main() {\n" +
                        "    int x[42];\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        main.execute(ctx);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void array2() {
        String code = "int main() {\n" +
                        "    int x[42];\n" +
                        "    return x[0];\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long value = main.execute(ctx);
        assertEquals(0, value);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void array3() {
        String code = "int main() {\n" +
                        "    int x[42];\n" +
                        "    x[10] = 42;\n" +
                        "    return x[0];\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long value = main.execute(ctx);
        assertEquals(0, value);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void array4() {
        String code = "int main() {\n" +
                        "    int x[42];\n" +
                        "    x[10] = 42;\n" +
                        "    return x[10];\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long value = main.execute(ctx);
        assertEquals(42, value);
    }

    @Test(timeout = DEFAULT_TIMEOUT, expected = IndexOutOfBoundsException.class)
    public void array5() {
        String code = "int main() {\n" +
                        "    int x[42];\n" +
                        "    x[42] = 42;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        main.execute(ctx);
        fail();
    }

    @Ignore
    @Test(timeout = DEFAULT_TIMEOUT)
    public void array6() {
        String code = "int main() {\n" +
                        "    int x;\n" +
                        "    x[42] = 42;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        main.execute(ctx);
        fail();
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void pointer1() {
        String code = "int main() {\n" +
                        "    char* str = alloca(10);\n" +
                        "    str[0] = 1;\n" +
                        "    str[1] = 2;\n" +
                        "    str[2] = 3;\n" +
                        "    return str[1];\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long value = main.execute(ctx);
        assertEquals(2, value);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void pointer2() {
        String code = "int main() {\n" +
                        "    char buf[10];\n" +
                        "    char* str = buf;\n" +
                        "    str[0] = 21;\n" +
                        "    str[1] = 42;\n" +
                        "    return buf[0];\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long value = main.execute(ctx);
        assertEquals(21, value);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void struct1() {
        String code = "struct point {\n" +
                        "    int x;\n" +
                        "    int y;\n" +
                        "};\n" +
                        "\n" +
                        "int main() {\n" +
                        "    struct point p;\n" +
                        "    p.x = 42;\n" +
                        "    p.y = 21;\n" +
                        "    return p.x;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long value = main.execute(ctx);
        assertEquals(42, value);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void sprintf1() {
        String code = "void main(char* out) {\n" +
                        "    sprintf(out, \"Hello world\");\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        byte[] buf = new byte[64];
        Pointer ptr = new Pointer(PointerType.CHARPTR, 0, new Record(PrimitiveType.CHAR, buf));
        main.execute(ctx, ptr);
        assertEquals("Hello world", ptr.cstr());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void sprintf2() {
        String code = "void main(char* out) {\n" +
                        "    sprintf(out, \"number: %d\", 42);\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        byte[] buf = new byte[64];
        Pointer ptr = new Pointer(PointerType.CHARPTR, 0, new Record(PrimitiveType.CHAR, buf));
        main.execute(ctx, ptr);
        assertEquals("number: 42", ptr.cstr());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void unsigned1() {
        String code = "void main(char* out) {\n" +
                        "    uint32_t value = 0x12345678;\n" +
                        "    out[0] = value >> 24;\n" +
                        "    out[1] = value >> 16;\n" +
                        "    out[2] = value >> 8;\n" +
                        "    out[3] = value;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        byte[] buf = new byte[4];
        Pointer ptr = new Pointer(PointerType.CHARPTR, 0, new Record(PrimitiveType.CHAR, buf));
        main.execute(ctx, ptr);
        assertEquals(0x12, buf[0]);
        assertEquals(0x34, buf[1]);
        assertEquals(0x56, buf[2]);
        assertEquals(0x78, buf[3]);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void div1() {
        String code = "void main(char* out) {\n" +
                        "    uint32_t value = 24;\n" +
                        "    uint32_t val = value / 8;\n" +
                        "    sprintf(out, \"%d\", val);\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        byte[] buf = new byte[4];
        Pointer ptr = new Pointer(PointerType.CHARPTR, 0, new Record(PrimitiveType.CHAR, buf));
        main.execute(ctx, ptr);
        assertEquals("3", ptr.cstr());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void logicor1() {
        String code = "int main() {\n" +
                        "    uint32_t val1 = 0;\n" +
                        "    uint32_t val2 = 0;\n" +
                        "    return val1 || val2;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx);
        assertEquals(0, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void logicor2() {
        String code = "int main() {\n" +
                        "    uint32_t val1 = 1;\n" +
                        "    uint32_t val2 = 0;\n" +
                        "    return val1 || val2;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx);
        assertEquals(1, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void logicor3() {
        String code = "int main() {\n" +
                        "    uint32_t val1 = 42;\n" +
                        "    uint32_t val2 = 0;\n" +
                        "    return val1 || val2;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx);
        assertEquals(1, result);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void logicor4() {
        String code = "int main() {\n" +
                        "    uint32_t val1 = 42;\n" +
                        "    uint32_t val2 = 7;\n" +
                        "    return val1 || val2;\n" +
                        "}\n";
        Parser p = parse(code);

        Context ctx = new Context();
        Function main = p.symtab.getFunction("main");
        assertNotNull(main);
        long result = main.execute(ctx);
        assertEquals(1, result);
    }
}
