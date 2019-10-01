package org.graalvm.vm.x86.trcview.expression;

import java.text.ParseException;

import org.graalvm.vm.x86.trcview.expression.Token.TokenType;

public class Scanner {
    private String input;
    private int readptr;

    public Scanner(String s) {
        input = s;
        readptr = 0;
    }

    private int read() {
        if (readptr < input.length()) {
            return input.charAt(readptr++);
        } else {
            return -1;
        }
    }

    private void unread() {
        readptr--;
    }

    private static boolean isIdent(char c) {
        if (c == '_' || c == '$' || c == '.' || c == '@') {
            return true;
        }
        if (c >= '0' && c <= '9') {
            return true;
        }
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        if (c >= 'a' && c <= 'z') {
            return true;
        }
        return false;
    }

    private void error(String msg) throws ParseException {
        throw new ParseException(msg, readptr);
    }

    public Token next() throws ParseException {
        int c = read();
        while (c == ' ' || c == '\r' || c == '\n') {
            c = read();
        }

        long tmp;
        switch (c) {
            case -1:
                return new Token(TokenType.EOF);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                tmp = c - '0';
                c = read();
                if (tmp == 0 && c == 'x') {
                    int digits = 0;
                    while (true) {
                        c = read();
                        digits++;
                        if (c >= '0' && c <= '9') {
                            tmp <<= 4L;
                            tmp |= c - '0';
                        } else if (c >= 'A' && c <= 'F') {
                            tmp <<= 4L;
                            tmp |= c - 'A' + 0x0A;
                        } else if (c >= 'a' && c <= 'f') {
                            tmp <<= 4L;
                            tmp |= c - 'a' + 0x0A;
                        } else {
                            digits--;
                            break;
                        }
                    }
                    if (digits == 0) {
                        if (c == -1) {
                            error("unexpected eof");
                        } else {
                            error("unexpected char: '" + (char) c + "'");
                        }
                    }
                } else {
                    while (c >= '0' && c <= '9') {
                        tmp *= 10L;
                        tmp += c - '0';
                        c = read();
                    }
                }
                if (c != -1) {
                    if (isIdent((char) c)) {
                        error("unexpected char: '" + (char) c + "'");
                    }
                    unread();
                }
                return new Token(TokenType.NUMBER, tmp);
            case '+':
                return new Token(TokenType.ADD);
            case '-':
                return new Token(TokenType.SUB);
            case '*':
                return new Token(TokenType.MUL);
            case '/':
                return new Token(TokenType.DIV);
            case '(':
                return new Token(TokenType.LPAR);
            case ')':
                return new Token(TokenType.RPAR);
            case ',':
                return new Token(TokenType.COMMA);
            case '&':
                c = read();
                if (c == '&') {
                    return new Token(TokenType.LAND);
                } else {
                    unread();
                    return new Token(TokenType.AND);
                }
            case '|':
                c = read();
                if (c == '|') {
                    return new Token(TokenType.LOR);
                } else {
                    unread();
                    return new Token(TokenType.OR);
                }
            case '^':
                return new Token(TokenType.XOR);
            case '~':
                return new Token(TokenType.INV);
            case '<':
                c = read();
                if (c == '=') {
                    return new Token(TokenType.LE);
                } else if (c == '<') {
                    return new Token(TokenType.SHL);
                } else {
                    unread();
                    return new Token(TokenType.LT);
                }
            case '>':
                c = read();
                if (c == '=') {
                    return new Token(TokenType.GE);
                } else if (c == '>') {
                    return new Token(TokenType.SHR);
                } else {
                    unread();
                    return new Token(TokenType.GT);
                }
            case '=':
                c = read();
                if (c == '=') {
                    return new Token(TokenType.EQ);
                } else {
                    unread();
                    error("unknown char: '='");
                }
            case '!':
                c = read();
                if (c == '=') {
                    return new Token(TokenType.NE);
                } else {
                    unread();
                    return new Token(TokenType.NOT);
                }
            default:
                if (isIdent((char) c)) {
                    StringBuilder buf = new StringBuilder();
                    buf.append((char) c);
                    while (isIdent((char) (c = read()))) {
                        buf.append((char) c);
                    }
                    if (c != -1) {
                        unread();
                    }
                    return new Token(TokenType.IDENT, buf.toString());
                } else {
                    error("unknown char: '" + (char) c + "'");
                }
        }
        return null;
    }
}
