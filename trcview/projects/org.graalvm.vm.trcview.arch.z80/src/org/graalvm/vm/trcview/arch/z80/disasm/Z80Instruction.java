package org.graalvm.vm.trcview.arch.z80.disasm;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.Disassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.disasm.AssemblerInstruction;
import org.graalvm.vm.trcview.disasm.Operand;

public class Z80Instruction {
    public static final Z80IndexRegisterOperand Ih = new Z80IndexRegisterOperand("h");
    public static final Z80IndexRegisterOperand Il = new Z80IndexRegisterOperand("l");
    public static final Z80Operand I = new Z80IndexRegisterOperand();
    public static final Z80Operand B = new Z80ByteOperand();
    public static final Z80Operand R = new Z80RelativeOperand();
    public static final Z80Operand W = new Z80WordOperand();
    public static final Z80Operand X = new Z80IndexedOperand(true);
    public static final Z80Operand Y = new Z80IndexedOperand(false);
    public static final Z80RegisterOperand a = new Z80RegisterOperand("a");
    public static final Z80RegisterOperand b = new Z80RegisterOperand("b");
    public static final Z80RegisterOperand c = new Z80RegisterOperand("c");
    public static final Z80RegisterOperand d = new Z80RegisterOperand("d");
    public static final Z80RegisterOperand e = new Z80RegisterOperand("e");
    public static final Z80RegisterOperand h = new Z80RegisterOperand("h");
    public static final Z80RegisterOperand l = new Z80RegisterOperand("l");
    public static final Z80RegisterOperand i = new Z80RegisterOperand("i");
    public static final Z80RegisterOperand r = new Z80RegisterOperand("r");
    public static final Z80RegisterOperand af = new Z80RegisterOperand("af");
    public static final Z80RegisterOperand af_ = new Z80RegisterOperand("af'");
    public static final Z80RegisterOperand bc = new Z80RegisterOperand("bc");
    public static final Z80RegisterOperand de = new Z80RegisterOperand("de");
    public static final Z80RegisterOperand hl = new Z80RegisterOperand("hl");
    public static final Z80RegisterOperand sp = new Z80RegisterOperand("sp");

    public static final int ALWAYS = 0;
    public static final int Z = 1;
    public static final int NZ = 2;
    public static final int C = 3;
    public static final int NC = 4;
    public static final int PO = 5;
    public static final int PE = 6;
    public static final int P = 7;
    public static final int M = 8;

    // @formatter:off
    static final Z80Instruction INSTRUCTION_XX_CB[] = {
        null,null,null,null,null,null,ins("rlc", Y   ),null,
        null,null,null,null,null,null,ins("rrc", Y   ),null,
        null,null,null,null,null,null,ins("rl",  Y   ),null,
        null,null,null,null,null,null,ins("rr",  Y   ),null,
        null,null,null,null,null,null,ins("sla", Y   ),null,
        null,null,null,null,null,null,ins("sra", Y   ),null,
        null,null,null,null,null,null,ins("sll", Y   ),null,
        null,null,null,null,null,null,ins("srl", Y   ),null,
        null,null,null,null,null,null,ins("bit", 0, Y),null,
        null,null,null,null,null,null,ins("bit", 1, Y),null,
        null,null,null,null,null,null,ins("bit", 2, Y),null,
        null,null,null,null,null,null,ins("bit", 3, Y),null,
        null,null,null,null,null,null,ins("bit", 4, Y),null,
        null,null,null,null,null,null,ins("bit", 5, Y),null,
        null,null,null,null,null,null,ins("bit", 6, Y),null,
        null,null,null,null,null,null,ins("bit", 7, Y),null,
        null,null,null,null,null,null,ins("res", 0, Y),null,
        null,null,null,null,null,null,ins("res", 1, Y),null,
        null,null,null,null,null,null,ins("res", 2, Y),null,
        null,null,null,null,null,null,ins("res", 3, Y),null,
        null,null,null,null,null,null,ins("res", 4, Y),null,
        null,null,null,null,null,null,ins("res", 5, Y),null,
        null,null,null,null,null,null,ins("res", 6, Y),null,
        null,null,null,null,null,null,ins("res", 7, Y),null,
        null,null,null,null,null,null,ins("set", 0, Y),null,
        null,null,null,null,null,null,ins("set", 1, Y),null,
        null,null,null,null,null,null,ins("set", 2, Y),null,
        null,null,null,null,null,null,ins("set", 3, Y),null,
        null,null,null,null,null,null,ins("set", 4, Y),null,
        null,null,null,null,null,null,ins("set", 5, Y),null,
        null,null,null,null,null,null,ins("set", 6, Y),null,
        null,null,null,null,null,null,ins("set", 7, Y),null
    };

    static final Z80Instruction INSTRUCTION_CB[] = {
        ins("rlc", b)  ,ins("rlc", c)  ,ins("rlc", d)  ,ins("rlc", e)  ,ins("rlc", h)  ,ins("rlc", l)  ,ins("rlc", m(hl))  ,ins("rlc", a)  ,
        ins("rrc", b)  ,ins("rrc", c)  ,ins("rrc", d)  ,ins("rrc", e)  ,ins("rrc", h)  ,ins("rrc", l)  ,ins("rrc", m(hl))  ,ins("rrc", a)  ,
        ins("rl",  b)  ,ins("rl",  c)  ,ins("rl",  d)  ,ins("rl",  e)  ,ins("rl",  h)  ,ins("rl",  l)  ,ins("rl",  m(hl))  ,ins("rl",  a)  ,
        ins("rr",  b)  ,ins("rr",  c)  ,ins("rr",  d)  ,ins("rr",  e)  ,ins("rr",  h)  ,ins("rr",  l)  ,ins("rr",  m(hl))  ,ins("rr",  a)  ,
        ins("sla", b)  ,ins("sla", c)  ,ins("sla", d)  ,ins("sla", e)  ,ins("sla", h)  ,ins("sla", l)  ,ins("sla", m(hl))  ,ins("sla", a)  ,
        ins("sra", b)  ,ins("sra", c)  ,ins("sra", d)  ,ins("sra", e)  ,ins("sra", h)  ,ins("sra", l)  ,ins("sra", m(hl))  ,ins("sra", a)  ,
        ins("sll", b)  ,ins("sll", c)  ,ins("sll", d)  ,ins("sll", e)  ,ins("sll", h)  ,ins("sll", l)  ,ins("sll", m(hl))  ,ins("sll", a)  ,
        ins("srl", b)  ,ins("srl", c)  ,ins("srl", d)  ,ins("srl", e)  ,ins("srl", h)  ,ins("srl", l)  ,ins("srl", m(hl))  ,ins("srl", a)  ,
        ins("bit", 0,b),ins("bit", 0,c),ins("bit", 0,d),ins("bit", 0,e),ins("bit", 0,h),ins("bit", 0,l),ins("bit", 0,m(hl)),ins("bit", 0,a),
        ins("bit", 1,b),ins("bit", 1,c),ins("bit", 1,d),ins("bit", 1,e),ins("bit", 1,h),ins("bit", 1,l),ins("bit", 1,m(hl)),ins("bit", 1,a),
        ins("bit", 2,b),ins("bit", 2,c),ins("bit", 2,d),ins("bit", 2,e),ins("bit", 2,h),ins("bit", 2,l),ins("bit", 2,m(hl)),ins("bit", 2,a),
        ins("bit", 3,b),ins("bit", 3,c),ins("bit", 3,d),ins("bit", 3,e),ins("bit", 3,h),ins("bit", 3,l),ins("bit", 3,m(hl)),ins("bit", 3,a),
        ins("bit", 4,b),ins("bit", 4,c),ins("bit", 4,d),ins("bit", 4,e),ins("bit", 4,h),ins("bit", 4,l),ins("bit", 4,m(hl)),ins("bit", 4,a),
        ins("bit", 5,b),ins("bit", 5,c),ins("bit", 5,d),ins("bit", 5,e),ins("bit", 5,h),ins("bit", 5,l),ins("bit", 5,m(hl)),ins("bit", 5,a),
        ins("bit", 6,b),ins("bit", 6,c),ins("bit", 6,d),ins("bit", 6,e),ins("bit", 6,h),ins("bit", 6,l),ins("bit", 6,m(hl)),ins("bit", 6,a),
        ins("bit", 7,b),ins("bit", 7,c),ins("bit", 7,d),ins("bit", 7,e),ins("bit", 7,h),ins("bit", 7,l),ins("bit", 7,m(hl)),ins("bit", 7,a),
        ins("res", 0,b),ins("res", 0,c),ins("res", 0,d),ins("res", 0,e),ins("res", 0,h),ins("res", 0,l),ins("res", 0,m(hl)),ins("res", 0,a),
        ins("res", 1,b),ins("res", 1,c),ins("res", 1,d),ins("res", 1,e),ins("res", 1,h),ins("res", 1,l),ins("res", 1,m(hl)),ins("res", 1,a),
        ins("res", 2,b),ins("res", 2,c),ins("res", 2,d),ins("res", 2,e),ins("res", 2,h),ins("res", 2,l),ins("res", 2,m(hl)),ins("res", 2,a),
        ins("res", 3,b),ins("res", 3,c),ins("res", 3,d),ins("res", 3,e),ins("res", 3,h),ins("res", 3,l),ins("res", 3,m(hl)),ins("res", 3,a),
        ins("res", 4,b),ins("res", 4,c),ins("res", 4,d),ins("res", 4,e),ins("res", 4,h),ins("res", 4,l),ins("res", 4,m(hl)),ins("res", 4,a),
        ins("res", 5,b),ins("res", 5,c),ins("res", 5,d),ins("res", 5,e),ins("res", 5,h),ins("res", 5,l),ins("res", 5,m(hl)),ins("res", 5,a),
        ins("res", 6,b),ins("res", 6,c),ins("res", 6,d),ins("res", 6,e),ins("res", 6,h),ins("res", 6,l),ins("res", 6,m(hl)),ins("res", 6,a),
        ins("res", 7,b),ins("res", 7,c),ins("res", 7,d),ins("res", 7,e),ins("res", 7,h),ins("res", 7,l),ins("res", 7,m(hl)),ins("res", 7,a),
        ins("set", 0,b),ins("set", 0,c),ins("set", 0,d),ins("set", 0,e),ins("set", 0,h),ins("set", 0,l),ins("set", 0,m(hl)),ins("set", 0,a),
        ins("set", 1,b),ins("set", 1,c),ins("set", 1,d),ins("set", 1,e),ins("set", 1,h),ins("set", 1,l),ins("set", 1,m(hl)),ins("set", 1,a),
        ins("set", 2,b),ins("set", 2,c),ins("set", 2,d),ins("set", 2,e),ins("set", 2,h),ins("set", 2,l),ins("set", 2,m(hl)),ins("set", 2,a),
        ins("set", 3,b),ins("set", 3,c),ins("set", 3,d),ins("set", 3,e),ins("set", 3,h),ins("set", 3,l),ins("set", 3,m(hl)),ins("set", 3,a),
        ins("set", 4,b),ins("set", 4,c),ins("set", 4,d),ins("set", 4,e),ins("set", 4,h),ins("set", 4,l),ins("set", 4,m(hl)),ins("set", 4,a),
        ins("set", 5,b),ins("set", 5,c),ins("set", 5,d),ins("set", 5,e),ins("set", 5,h),ins("set", 5,l),ins("set", 5,m(hl)),ins("set", 5,a),
        ins("set", 6,b),ins("set", 6,c),ins("set", 6,d),ins("set", 6,e),ins("set", 6,h),ins("set", 6,l),ins("set", 6,m(hl)),ins("set", 6,a),
        ins("set", 7,b),ins("set", 7,c),ins("set", 7,d),ins("set", 7,e),ins("set", 7,h),ins("set", 7,l),ins("set", 7,m(hl)),ins("set", 7,a)
    };

    static final Z80Instruction INSTRUCTION_ED[] = {
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        ins("in", b,i(c)),ins("out", o(c),b),ins("sbc", hl,bc),ins("ld", m(W),bc),ins("neg"),ret("retn"),ins("im", 0),ins("ld", i,a),
        ins("in", c,i(c)),ins("out", o(c),c),ins("adc", hl,bc),ins("ld", bc,m(W)),null      ,rfi("reti"),null        ,ins("ld", r,a),
        ins("in", d,i(c)),ins("out", o(c),d),ins("sbc", hl,de),ins("ld", m(W),de),null      ,null       ,ins("im", 1),ins("ld", a,i),
        ins("in", e,i(c)),ins("out", o(c),e),ins("adc", hl,de),ins("ld", de,m(W)),null      ,null       ,ins("im", 2),ins("ld", a,r),
        ins("in", h,i(c)),ins("out", o(c),h),ins("sbc", hl,hl),ins("ld", m(W),hl),null      ,null       ,null        ,ins("rrd")    ,
        ins("in", l,i(c)),ins("out", o(c),l),ins("adc", hl,hl),ins("ld", hl,m(W)),null      ,null       ,null        ,ins("rld")    ,
        ins("in", 0,i(c)),ins("out", o(c),0),ins("sbc", hl,sp),ins("ld", m(W),sp),null      ,null       ,null        ,null          ,
        ins("in", a,i(c)),ins("out", o(c),a),ins("adc", hl,sp),ins("ld", sp,m(W)),null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        ins("ldi")       ,ins("cpi")        ,ins("ini")       ,ins("outi")       ,null      ,null       ,null        ,null          ,
        ins("ldd")       ,ins("cpd")        ,ins("ind")       ,ins("outd")       ,null      ,null       ,null        ,null          ,
        ins("ldir")      ,ins("cpir")       ,ins("inir")      ,ins("otir")       ,null      ,null       ,null        ,null          ,
        ins("lddr")      ,ins("cpdr")       ,ins("indr")      ,ins("otdr")       ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null          ,
        null             ,null              ,null             ,null              ,null      ,null       ,null        ,null
    };

    static final Z80Instruction INSTRUCTION_XX[] = {
        null           ,null            ,null             ,null              ,null            ,null            ,null           ,null           ,
        null           ,ins("add", I,bc),null             ,null              ,null            ,null            ,null           ,null           ,
        null           ,null            ,null             ,null              ,null            ,null            ,null           ,null           ,
        null           ,ins("add", I,de),null             ,null              ,null            ,null            ,null           ,null           ,
        null           ,ins("ld",  I,W) ,ins("ld", m(W),I),ins("inc", I)     ,ins("inc", Ih)  ,ins("dec", Ih)  ,ins("ld", Ih,B),null           ,
        null           ,ins("add", I,I) ,ins("ld", I,m(W)),ins("dec", I)     ,ins("inc", Il)  ,ins("dec", Il)  ,ins("ld", Il,B),null           ,
        null           ,null            ,null             ,null              ,ins("inc", X)   ,ins("dec", X)   ,ins("ld", X,B) ,null           ,
        null           ,ins("add", I,sp),null             ,null              ,null            ,null            ,null           ,null           ,
        null           ,null            ,null             ,null              ,ins("ld",  b,Ih),ins("ld",  b,Il),ins("ld", b,X) ,null           ,
        null           ,null            ,null             ,null              ,ins("ld",  c,Ih),ins("ld",  c,Il),ins("ld", c,X) ,null           ,
        null           ,null            ,null             ,null              ,ins("ld",  d,Ih),ins("ld",  d,Il),ins("ld", d,X) ,null           ,
        null           ,null            ,null             ,null              ,ins("ld",  e,Ih),ins("ld",  e,Il),ins("ld", e,X) ,null           ,
        ins("ld", Ih,b),ins("ld", Ih,c),ins("ld", Ih,d)   ,ins("ld", Ih,e)   ,ins("ld",  Ih,h),ins("ld",  Ih,l),ins("ld", h,X) ,ins("ld", Ih,a),
        ins("ld", Il,b),ins("ld", Il,c),ins("ld", Il,d)   ,ins("ld", Il,e)   ,ins("ld",  Il,h),ins("ld",  Il,l),ins("ld", l,X) ,ins("ld", Il,a),
        ins("ld", X,b) ,ins("ld", X,c) ,ins("ld", X,d)    ,ins("ld", X,e)    ,ins("ld",  X,h) ,ins("ld",  X,l) ,null           ,ins("ld", X,a) ,
        null           ,null           ,null              ,null              ,ins("ld",  a,Ih),ins("ld",  a,Il),ins("ld",  a,X),null           ,
        null           ,null           ,null              ,null              ,ins("add", a,Ih),ins("add", a,Il),ins("add", a,X),null           ,
        null           ,null           ,null              ,null              ,ins("adc", a,Ih),ins("adc", a,Il),ins("adc", a,X),null           ,
        null           ,null           ,null              ,null              ,ins("sub", Ih)  ,ins("sub", Il)  ,ins("sub", X)  ,null           ,
        null           ,null           ,null              ,null              ,ins("sbc", a,Ih),ins("sbc", a,Il),ins("sbc", a,X),null           ,
        null           ,null           ,null              ,null              ,ins("and", Ih)  ,ins("and", Il)  ,ins("and", X)  ,null           ,
        null           ,null           ,null              ,null              ,ins("xor", Ih)  ,ins("xor", Il)  ,ins("xor", X)  ,null           ,
        null           ,null           ,null              ,null              ,ins("or",  Ih)  ,ins("or",  Il)  ,ins("or",  X)  ,null           ,
        null           ,null           ,null              ,null              ,ins("cp",  Ih)  ,ins("cp",  Il)  ,ins("cp",  X)  ,null           ,
        null           ,null           ,null              ,null              ,null            ,null            ,null           ,null           ,
        null           ,null           ,null              ,und("fd", "cb")   ,null            ,null            ,null           ,null           ,
        null           ,null           ,null              ,null              ,null            ,null            ,null           ,null           ,
        null           ,null           ,null              ,null              ,null            ,null            ,null           ,null           ,
        null           ,ins("pop", I)  ,null              ,ins("ex", m(sp),I),null            ,ins("push", I)  ,null           ,null           ,
        null           ,jmp("jp", m(I)),null              ,null              ,null            ,null            ,null           ,null           ,
        null           ,null           ,null              ,null              ,null            ,null            ,null           ,null           ,
        null           ,ins("ld", sp,I),null              ,null              ,null            ,null            ,null           ,null
    };

    static final Z80Instruction INSTRUCTION_MAIN[] = {
        ins("nop")         ,ins("ld",  bc,W)   ,ins("ld",  m(bc),a),ins("inc", bc)      ,ins("inc",  b)      ,ins("dec",  b)      ,ins("ld",  b,B)    ,ins("rlca")        ,
        ins("ex",  af,af_) ,ins("add", hl,bc)  ,ins("ld",  a,m(bc)),ins("dec", bc)      ,ins("inc",  c)      ,ins("dec",  c)      ,ins("ld",  c,B)    ,ins("rrca")        ,
        jcc("djnz",R)      ,ins("ld",  de,W)   ,ins("ld",  m(de),a),ins("inc", de)      ,ins("inc",  d)      ,ins("dec",  d)      ,ins("ld",  d,B)    ,ins("rla")         ,
        jmp("jr",  R)      ,ins("add", hl,de)  ,ins("ld",  a,m(de)),ins("dec", de)      ,ins("inc",  e)      ,ins("dec",  e)      ,ins("ld",  e,B)    ,ins("rra")         ,
        jcc("jr",  NZ,R)   ,ins("ld",  hl,W)   ,ins("ld",  m(W),hl),ins("inc", hl)      ,ins("inc",  h)      ,ins("dec",  h)      ,ins("ld",  h,B)    ,ins("daa")         ,
        jcc("jr",  Z,R)    ,ins("add", hl,hl)  ,ins("ld",  hl,m(W)),ins("dec", hl)      ,ins("inc",  l)      ,ins("dec",  l)      ,ins("ld",  l,B)    ,ins("cpl")         ,
        jcc("jr",  NC,R)   ,ins("ld",  sp,W)   ,ins("ld",  m(W),a) ,ins("inc", sp)      ,ins("inc",  m(hl))  ,ins("dec",  m(hl))  ,ins("ld",  m(hl),B),ins("scf")         ,
        jcc("jr",  C,R)    ,ins("add", hl,sp)  ,ins("ld",  a,m(W)) ,ins("dec", sp)      ,ins("inc",  a)      ,ins("dec",  a)      ,ins("ld",  a,B)    ,ins("ccf")         ,
        ins("ld",  b,b)    ,ins("ld",  b,c)    ,ins("ld",  b,d)    ,ins("ld",  b,e)     ,ins("ld",   b,h)    ,ins("ld",   b,l)    ,ins("ld",  b,m(hl)),ins("ld",  b,a)    ,
        ins("ld",  c,b)    ,ins("ld",  c,c)    ,ins("ld",  c,d)    ,ins("ld",  c,e)     ,ins("ld",   c,h)    ,ins("ld",   c,l)    ,ins("ld",  c,m(hl)),ins("ld",  c,a)    ,
        ins("ld",  d,b)    ,ins("ld",  d,c)    ,ins("ld",  d,d)    ,ins("ld",  d,e)     ,ins("ld",   d,h)    ,ins("ld",   d,l)    ,ins("ld",  d,m(hl)),ins("ld",  d,a)    ,
        ins("ld",  e,b)    ,ins("ld",  e,c)    ,ins("ld",  e,d)    ,ins("ld",  e,e)     ,ins("ld",   e,h)    ,ins("ld",   e,l)    ,ins("ld",  e,m(hl)),ins("ld",  e,a)    ,
        ins("ld",  h,b)    ,ins("ld",  h,c)    ,ins("ld",  h,d)    ,ins("ld",  h,e)     ,ins("ld",   h,h)    ,ins("ld",   h,l)    ,ins("ld",  h,m(hl)),ins("ld",  h,a)    ,
        ins("ld",  l,b)    ,ins("ld",  l,c)    ,ins("ld",  l,d)    ,ins("ld",  l,e)     ,ins("ld",   l,h)    ,ins("ld",   l,l)    ,ins("ld",  l,m(hl)),ins("ld",  l,a)    ,
        ins("ld",  m(hl),b),ins("ld",  m(hl),c),ins("ld",  m(hl),d),ins("ld",  m(hl),e) ,ins("ld",   m(hl),h),ins("ld",   m(hl),l),ins("halt")        ,ins("ld",  m(hl),a),
        ins("ld",  a,b)    ,ins("ld",  a,c)    ,ins("ld",  a,d)    ,ins("ld",  a,e)     ,ins("ld",   a,h)    ,ins("ld",   a,l)    ,ins("ld",  a,m(hl)),ins("ld",  a,a)    ,
        ins("add", a,b)    ,ins("add", a,c)    ,ins("add", a,d)    ,ins("add", a,e)     ,ins("add",  a,h)    ,ins("add",  a,l)    ,ins("add", a,m(hl)),ins("add", a,a)    ,
        ins("adc", a,b)    ,ins("adc", a,c)    ,ins("adc", a,d)    ,ins("adc", a,e)     ,ins("adc",  a,h)    ,ins("adc",  a,l)    ,ins("adc", a,m(hl)),ins("adc", a,a)    ,
        ins("sub", b)      ,ins("sub", c)      ,ins("sub", d)      ,ins("sub", e)       ,ins("sub",  h)      ,ins("sub",  l)      ,ins("sub", m(hl))  ,ins("sub", a)      ,
        ins("sbc", a,b)    ,ins("sbc", a,c)    ,ins("sbc", a,d)    ,ins("sbc", a,e)     ,ins("sbc",  a,h)    ,ins("sbc",  a,l)    ,ins("sbc", a,m(hl)),ins("sbc", a,a)    ,
        ins("and", b)      ,ins("and", c)      ,ins("and", d)      ,ins("and", e)       ,ins("and",  h)      ,ins("and",  l)      ,ins("and", m(hl))  ,ins("and", a)      ,
        ins("xor", b)      ,ins("xor", c)      ,ins("xor", d)      ,ins("xor", e)       ,ins("xor",  h)      ,ins("xor",  l)      ,ins("xor", m(hl))  ,ins("xor", a)      ,
        ins("or",  b)      ,ins("or",  c)      ,ins("or",  d)      ,ins("or",  e)       ,ins("or",   h)      ,ins("or",   l)      ,ins("or",  m(hl))  ,ins("or",  a)      ,
        ins("cp",  b)      ,ins("cp",  c)      ,ins("cp",  d)      ,ins("cp",  e)       ,ins("cp",   h)      ,ins("cp",   l)      ,ins("cp",  m(hl))  ,ins("cp",  a)      ,
        rtc("ret", NZ)     ,ins("pop", bc)     ,jcc("jp",  NZ,W)   ,jmp("jp",  W)       ,jsr("call", NZ,W)   ,ins("push", bc)     ,ins("add", a,B)    ,rst("rst", 0x00)   ,
        rtc("ret", Z)      ,ret("ret")         ,jcc("jp",  Z,W)    ,ins("cb")           ,jsr("call", Z,W)    ,jsr("call", W)      ,ins("adc", a,B)    ,rst("rst", 0x08)   ,
        rtc("ret", NC)     ,ins("pop", de)     ,jcc("jp",  NC,W)   ,ins("out", o(B),a)  ,jsr("call", NC,W)   ,ins("push", de)     ,ins("sub", B)      ,rst("rst", 0x10)   ,
        rtc("ret", C)      ,ins("exx")         ,jcc("jp",  C,W)    ,ins("in",  a,i(B))  ,jsr("call", C,W)    ,und("dd")           ,ins("sbc", a,B)    ,rst("rst", 0x18)   ,
        rtc("ret", PO)     ,ins("pop", hl)     ,jcc("jp",  PO,W)   ,ins("ex",  m(sp),hl),jsr("call", PO,W)   ,ins("push", hl)     ,ins("and", B)      ,rst("rst", 0x20)   ,
        rtc("ret", PE)     ,jmp("jp",  m(hl))  ,jcc("jp",  PE,W)   ,ins("ex",  de,hl)   ,jsr("call", PE,W)   ,und("ed")           ,ins("xor", B)      ,rst("rst", 0x28)   ,
        rtc("ret", P)      ,ins("pop", af)     ,jcc("jp",  P,W)    ,ins("di")           ,jsr("call", P,W)    ,ins("push", af)     ,ins("or",  B)      ,rst("rst", 0x30)   ,
        rtc("ret", M)      ,ins("ld",  sp,hl)  ,jcc("jp",  M,W)    ,ins("ei")           ,jsr("call", M,W)    ,und("fd")           ,ins("cp",  B)      ,rst("rst", 0x38)
    };
    // @formatter:on

    private static Z80Operand m(Z80Operand x) {
        return new Z80MemoryOperand(x);
    }

    private static Z80Operand i(Z80Operand x) {
        return new Z80InputOperand(x);
    }

    private static Z80Operand o(Z80Operand x) {
        return new Z80OutputOperand(x);
    }

    private static Z80Instruction und(String mnemonic, String... operands) {
        Z80Operand[] ops = new Z80Operand[operands.length];
        for (int n = 0; n < operands.length; n++) {
            ops[n] = new Z80StringOperand(operands[n]);
        }
        return new Z80Instruction(InstructionType.OTHER, mnemonic, ops);
    }

    private static Z80Instruction ins(String mnemonic, Z80Operand... operands) {
        return new Z80Instruction(InstructionType.OTHER, mnemonic, operands);
    }

    private static Z80Instruction ins(String mnemonic, int value, Z80Operand... operands) {
        Z80Operand[] ops = new Z80Operand[operands.length + 1];
        System.arraycopy(operands, 0, ops, 1, operands.length);
        ops[0] = new Z80IntegerOperand(value);
        return new Z80Instruction(InstructionType.OTHER, mnemonic, ops);
    }

    private static Z80Instruction ins(String mnemonic, Z80Operand op, int value) {
        Z80Operand[] ops = new Z80Operand[]{op, new Z80IntegerOperand(value)};
        return new Z80Instruction(InstructionType.OTHER, mnemonic, ops);
    }

    private static Z80Instruction jmp(String mnemonic, Z80Operand target) {
        if (target instanceof Z80MemoryOperand) {
            return new Z80Instruction(InstructionType.JMP_INDIRECT, mnemonic, target);
        } else {
            return new Z80Instruction(InstructionType.JMP, mnemonic, target);
        }
    }

    private static Z80Instruction jcc(String mnemonic, Z80Operand target) {
        // reserved for djnz
        return new Z80Instruction(InstructionType.JCC, mnemonic, target);
    }

    private static Z80Instruction jcc(String mnemonic, int cond, Z80Operand target) {
        if (cond != ALWAYS) {
            return new Z80Instruction(InstructionType.JCC, cond, mnemonic, new Z80Operand[]{new Z80ConditionOperand(cond), target});
        } else {
            return new Z80Instruction(InstructionType.JCC, cond, mnemonic, target);
        }
    }

    private static Z80Instruction jsr(String mnemonic, Z80Operand target) {
        return new Z80Instruction(InstructionType.CALL, mnemonic, target);
    }

    private static Z80Instruction jsr(String mnemonic, int cond, Z80Operand target) {
        if (cond != ALWAYS) {
            return new Z80Instruction(InstructionType.CALL, cond, mnemonic, new Z80Operand[]{new Z80ConditionOperand(cond), target});
        } else {
            return new Z80Instruction(InstructionType.CALL, cond, mnemonic, target);
        }
    }

    private static Z80Instruction ret(String mnemonic) {
        return new Z80Instruction(InstructionType.RET, mnemonic);
    }

    private static Z80Instruction rfi(String mnemonic) {
        return new Z80Instruction(InstructionType.RTI, mnemonic);
    }

    private static Z80Instruction rtc(String mnemonic, int cond) {
        if (cond != ALWAYS) {
            return new Z80Instruction(InstructionType.RET, cond, mnemonic, new Z80Operand[]{new Z80ConditionOperand(cond)});
        } else {
            return new Z80Instruction(InstructionType.RET, cond, mnemonic);
        }
    }

    private static Z80Instruction rst(String mnemonic, int vector) {
        return new Z80Instruction(InstructionType.SYSCALL, mnemonic, new Z80Operand[]{new Z80IntegerOperand(vector)});
    }

    private final InstructionType type;
    private final int cond;
    private final String mnemonic;
    private final Z80Operand[] operands;

    public Z80Instruction(InstructionType type, String mnemonic) {
        this(type, ALWAYS, mnemonic, new Z80Operand[0]);
    }

    public Z80Instruction(InstructionType type, String mnemonic, Z80Operand operand) {
        this(type, ALWAYS, mnemonic, new Z80Operand[]{operand});
    }

    public Z80Instruction(InstructionType type, int cond, String mnemonic) {
        this(type, cond, mnemonic, new Z80Operand[0]);
    }

    public Z80Instruction(InstructionType type, int cond, String mnemonic, Z80Operand operand) {
        this(type, cond, mnemonic, new Z80Operand[]{operand});
    }

    public Z80Instruction(InstructionType type, String mnemonic, Z80Operand[] operands) {
        this(type, ALWAYS, mnemonic, operands);
    }

    public Z80Instruction(InstructionType type, int cond, String mnemonic, Z80Operand[] operands) {
        this.type = type;
        this.cond = cond;
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public InstructionType getType() {
        return type;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public int getCondition() {
        return cond;
    }

    private AssemblerInstruction disassemble(Z80MachineCode code) {
        Operand[] ops = new Operand[operands.length];
        for (int n = 0; n < operands.length; n++) {
            ops[n] = operands[n].disassemble(code);
        }
        return new AssemblerInstruction(mnemonic, ops);
    }

    public static AssemblerInstruction disassemble(CodeReader code, Disassembler disasm) {
        Z80MachineCode machineCode = new Z80MachineCode(code, disasm);
        Z80Instruction insn = machineCode.getInstruction();
        return insn.disassemble(machineCode);
    }

    public static Z80Instruction getInstruction(CodeReader code) {
        Z80MachineCode machineCode = new Z80MachineCode(code);
        return machineCode.getInstruction();
    }
}
