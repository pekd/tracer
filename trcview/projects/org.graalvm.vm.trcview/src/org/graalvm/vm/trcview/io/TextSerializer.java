package org.graalvm.vm.trcview.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextSerializer {
    public static String encode(String... s) {
        StringBuilder buf = new StringBuilder();
        for (String str : s) {
            if (buf.length() > 0) {
                buf.append(';');
            }
            buf.append(encode(str));
        }
        return buf.toString();
    }

    public static String encode(String s) {
        if (s == null) {
            return "NULL";
        }
        StringBuilder buf = new StringBuilder();
        buf.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\r':
                    buf.append("\\r");
                    break;
                case '\n':
                    buf.append("\\n");
                    break;
                case '\t':
                    buf.append("\\t");
                    break;
                case '\b':
                    buf.append("\\b");
                    break;
                case '\f':
                    buf.append("\\f");
                    break;
                case '"':
                    buf.append("\\\"");
                    break;
                case '\'':
                    buf.append("\\\'");
                    break;
                case '\\':
                    buf.append("\\\\");
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }
        buf.append('"');
        return buf.toString();
    }

    public static String decode(String s) {
        if (s.equals("NULL")) {
            return null;
        } else {
            if (s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"') {
                throw new IllegalArgumentException("missing termination");
            }
            StringBuilder buf = new StringBuilder();
            boolean escape = false;
            for (int i = 1; i < s.length() - 1; i++) {
                char c = s.charAt(i);
                if (escape) {
                    switch (c) {
                        case '\\':
                            buf.append('\\');
                            break;
                        case 'r':
                            buf.append('\r');
                            break;
                        case 'n':
                            buf.append('\n');
                            break;
                        case 'f':
                            buf.append('\f');
                            break;
                        case 'b':
                            buf.append('\b');
                            break;
                        case '\'':
                            buf.append('\'');
                            break;
                        case '"':
                            buf.append('"');
                            break;
                        default:
                            throw new IllegalArgumentException("invalid escape sequence: " + c);
                    }
                    escape = false;
                } else {
                    if (c == '\\') {
                        escape = true;
                    } else {
                        buf.append(c);
                    }
                }
            }
            return buf.toString();
        }
    }

    private static enum State {
        START,
        N,
        U,
        L,
        ZERO,
        DECIMAL,
        HEX,
        STRING,
        ESCAPE,
        SEP;
    }

    public static String[] tokenize(String s) throws IOException {
        List<String> result = new ArrayList<>();
        State state = State.START;
        long number = 0;
        StringBuilder buf = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (state) {
                case START:
                    switch (c) {
                        case 'N':
                            state = State.N;
                            break;
                        case '0':
                            state = State.ZERO;
                            break;
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            state = State.DECIMAL;
                            number = c - '0';
                            break;
                        case '"':
                            state = State.STRING;
                            buf = new StringBuilder();
                            break;
                    }
                    break;
                case N:
                    if (c == 'U') {
                        state = State.U;
                    } else {
                        throw new IOException("invalid keyword");
                    }
                    break;
                case U:
                    if (c == 'L') {
                        state = State.L;
                    } else {
                        throw new IOException("invalid keyword");
                    }
                    break;
                case L:
                    if (c == 'L') {
                        result.add(null);
                        state = State.SEP;
                    } else {
                        throw new IOException("invalid keyword");
                    }
                    break;
                case ZERO:
                    if (c == 'x') {
                        state = State.HEX;
                        number = 0;
                    } else {
                        throw new IOException("invalid number");
                    }
                    break;
                case HEX:
                    number <<= 4;
                    if (c >= '0' && c <= '9') {
                        number |= c - '0';
                    } else if (c >= 'A' && c <= 'F') {
                        number |= c - 'A' + 10;
                    } else if (c >= 'a' && c <= 'f') {
                        number |= c - 'a' + 10;
                    } else if (c == ';') {
                        result.add(Long.toUnsignedString(number));
                        state = State.START;
                    } else {
                        throw new IOException("invalid number");
                    }
                    break;
                case DECIMAL:
                    number *= 10;
                    if (c >= '0' && c <= '9') {
                        number += c - '0';
                    } else if (c == ';') {
                        result.add(Long.toUnsignedString(number));
                        state = State.START;
                    } else {
                        throw new IOException("invalid number");
                    }
                    break;
                case STRING:
                    if (c == '"') {
                        state = State.SEP;
                        result.add(buf.toString());
                    } else if (c == '\\') {
                        state = State.ESCAPE;
                    } else {
                        buf.append(c);
                    }
                    break;
                case ESCAPE:
                    switch (c) {
                        case '\\':
                            buf.append('\\');
                            break;
                        case 'r':
                            buf.append('\r');
                            break;
                        case 'n':
                            buf.append('\n');
                            break;
                        case 'f':
                            buf.append('\f');
                            break;
                        case 'b':
                            buf.append('\b');
                            break;
                        case '\'':
                            buf.append('\'');
                            break;
                        case '"':
                            buf.append('"');
                            break;
                        default:
                            throw new IOException("invalid escape sequence: " + c);
                    }
                    state = State.STRING;
                    break;
                case SEP:
                    if (c == ';') {
                        state = State.START;
                    } else {
                        throw new IOException("separator expected");
                    }
                    break;
            }
        }
        switch (state) {
            case DECIMAL:
            case HEX:
                result.add(Long.toUnsignedString(number));
                break;
            case ZERO:
                result.add("0");
                break;
            case START:
            case SEP:
                break;
            default:
                throw new IOException("unexpected EOF in state " + state);
        }
        return result.toArray(new String[result.size()]);
    }
}
