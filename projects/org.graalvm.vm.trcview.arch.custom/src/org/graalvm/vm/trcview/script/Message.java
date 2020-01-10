package org.graalvm.vm.trcview.script;

import java.text.MessageFormat;

public enum Message {
    // @formatter:off
    // error messages for Scanner
    EMPTY_CHARCONST("empty character constant"),
    UNDEFINED_ESCAPE("undefined escape character sequence ''\\{0}''"),
    MISSING_QUOTE("missing '' at end of character constant"),
    INVALID_CHAR("invalid character {0}"),
    BIG_NUM("{0} too big for integer constant"),
    EOF_IN_COMMENT("unexpected end of file in comment"),
    ILLEGAL_LINE_END("illegal line end in character constant"),

    // syntax errors
    FACTOR("number | ident | ''('' | ''-'' | ''!'' | ''~'' expected"),
    VOID_WITH_SIGN("void cannot be signed/unsigned"),
    ARRAY_DIMENSION("array dimension expected"),
    TYPE_EXPECTED("type expected"),
    TOKEN_EXPECTED("{0} expected"),

    // semantic errors
    ANONYMOUS_STRUCT("anonymous struct not allowed at this location"),
    DUPLICATE_TYPE("duplicate definition of type {0}"),
    UNKNOWN_TYPE("unknown type {0}"),
    UNKNOWN_SYMBOL("unknown symbol {0}"),
    REDEFINE_SYMBOL("symbol {0} already defined"),
    NOT_A_POINTER("{0} is not a pointer"),
    NOT_A_STRUCT("{0} is not a struct"),
    NOT_AN_ARRAY("{0} is not an array"),
    UNKNOWN_MEMBER("unknown member {0}"),

    // misc errors
    NOT_IMPLEMENTED("feature not implemented");
    // @formatter:on

    private final String msg;

    private Message(String msg) {
        this.msg = msg;
    }

    public String format(Object... params) {
        if (params.length != (msg.contains("{0}") ? 1 : 0)) {
            throw new Error("incorrect number of error message parameters");
        }
        return MessageFormat.format(msg, params);
    }
}
