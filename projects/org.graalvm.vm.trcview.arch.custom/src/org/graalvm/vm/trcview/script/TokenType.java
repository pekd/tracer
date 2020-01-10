package org.graalvm.vm.trcview.script;

public enum TokenType {
    // @formatter:off
    none("none"),
    ident("identifier"),
    number("number"),
    charConst("character constant"),
    stringConst("string constant"),
    plus("+"),
    minus("-"),
    times("*"),
    slash("/"),
    rem("%"),
    eql("=="),
    neq("!="),
    lt("<"),
    leq("<="),
    gt(">"),
    geq(">="),
    and("&&"),
    or("||"),
    shl("<<"),
    shr(">>"),
    bitand("&"),
    bitor("|"),
    xor("^"),
    not("!"),
    com("~"),
    assign("="),
    plusas("+="),
    minusas("-="),
    timesas("*="),
    slashas("/="),
    remas("%="),
    pplus("++"),
    mminus("--"),
    semicolon(";"),
    comma(","),
    period("."),
    arrow("->"),
    lpar("("),
    rpar(")"),
    lbrack("["),
    rbrack("]"),
    lbrace("{"),
    rbrace("}"),
    break_("break"),
    continue_("continue"),
    else_("else"),
    if_("if"),
    return_("return"),
    void_("void"),
    while_("while"),
    for_("for"),
    do_("do"),
    switch_("switch"),
    case_("case"),
    default_("default"),
    char_("char"),
    short_("short"),
    int_("int"),
    long_("long"),
    signed_("signed"),
    unsigned_("unsigned"),
    const_("const"),
    struct_("struct"),
    union_("union"),
    typedef_("typedef"),
    true_("true"),
    false_("false"),
    eof("end of file");
    // @formatter:on

    private String label;

    private TokenType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    public String label() {
        return label;
    }
}
