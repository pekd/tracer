package org.graalvm.vm.x86.trcview.expression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.x86.trcview.analysis.type.DataType;
import org.graalvm.vm.x86.trcview.analysis.type.Function;
import org.graalvm.vm.x86.trcview.analysis.type.Prototype;
import org.graalvm.vm.x86.trcview.analysis.type.Representation;
import org.graalvm.vm.x86.trcview.analysis.type.Type;
import org.graalvm.vm.x86.trcview.expression.Token.TokenType;
import org.graalvm.vm.x86.trcview.expression.ast.AddNode;
import org.graalvm.vm.x86.trcview.expression.ast.AndNode;
import org.graalvm.vm.x86.trcview.expression.ast.CallNode;
import org.graalvm.vm.x86.trcview.expression.ast.DivNode;
import org.graalvm.vm.x86.trcview.expression.ast.EqNode;
import org.graalvm.vm.x86.trcview.expression.ast.Expression;
import org.graalvm.vm.x86.trcview.expression.ast.GeNode;
import org.graalvm.vm.x86.trcview.expression.ast.GtNode;
import org.graalvm.vm.x86.trcview.expression.ast.InvNode;
import org.graalvm.vm.x86.trcview.expression.ast.LeNode;
import org.graalvm.vm.x86.trcview.expression.ast.LogicAndNode;
import org.graalvm.vm.x86.trcview.expression.ast.LogicOrNode;
import org.graalvm.vm.x86.trcview.expression.ast.LtNode;
import org.graalvm.vm.x86.trcview.expression.ast.MulNode;
import org.graalvm.vm.x86.trcview.expression.ast.NeNode;
import org.graalvm.vm.x86.trcview.expression.ast.NegNode;
import org.graalvm.vm.x86.trcview.expression.ast.NotNode;
import org.graalvm.vm.x86.trcview.expression.ast.OrNode;
import org.graalvm.vm.x86.trcview.expression.ast.SubNode;
import org.graalvm.vm.x86.trcview.expression.ast.ValueNode;
import org.graalvm.vm.x86.trcview.expression.ast.VariableNode;

/*
 * prototype = type ident ["<" expr ">" ] "(" [ type [ ident ] { "," type [ ident ] } ] ")" .
 * type      = basic { "*" ["const"] } ["$out" | "$dec" | "$hex" | "$char"] .
 * basic     = ["const"]
 *           ( ["unsigned" | "signed"] integer
 *           | "u8"
 *           | "s8"
 *           | "u16"
 *           | "s16"
 *           | "u32"
 *           | "s32"
 *           | "u64"
 *           | "s64"
 *           | "void" )
 *           [ "<" expr ">" ]
 *           .
 * integer   = "char"
 *           | "short"
 *           | "int"
 *           | "long"
 *           .
 * expr      = sum { ("+" | "-") sum } .
 * sum       = factor { ("*" | "/") factor } .
 * factor    = number
 *           | ident [ "(" [ nexpr { "," nexpr } ] ")" ]
 *           | "(" nexpr ")"
 *           | "!" nexpr
 *           | "~" nexpr
 *           | "-" nexpr .
 * nexpr = (* normal expression with logic and comparison operators *)
 */
public class TypeParser {
    private Scanner scanner;

    private Token t;
    private Token la;
    private TokenType sym;

    public TypeParser(String s) {
        scanner = new Scanner(s);
    }

    private static void error(String msg) throws ParseException {
        throw new ParseException(msg, 0);
    }

    private void scan() throws ParseException {
        t = la;
        la = scanner.next();
        if (la.type == TokenType.IDENT) {
            switch (la.str) {
                case "unsigned":
                    la = new Token(TokenType.UNSIGNED);
                    break;
                case "signed":
                    la = new Token(TokenType.UNSIGNED);
                    break;
                case "const":
                    la = new Token(TokenType.CONST);
                    break;
                case "char":
                    la = new Token(TokenType.CHAR);
                    break;
                case "short":
                    la = new Token(TokenType.SHORT);
                    break;
                case "int":
                    la = new Token(TokenType.INT);
                    break;
                case "long":
                    la = new Token(TokenType.LONG);
                    break;
                case "u8":
                case "uint8_t":
                    la = new Token(TokenType.U8);
                    break;
                case "u16":
                case "uint16_t":
                    la = new Token(TokenType.U16);
                    break;
                case "u32":
                case "uint32_t":
                    la = new Token(TokenType.U32);
                    break;
                case "u64":
                case "uint64_t":
                case "size_t":
                    la = new Token(TokenType.U64);
                    break;
                case "s8":
                case "int8_t":
                    la = new Token(TokenType.S8);
                    break;
                case "s16":
                case "int16_t":
                    la = new Token(TokenType.S16);
                    break;
                case "s32":
                case "int32_t":
                    la = new Token(TokenType.S32);
                    break;
                case "s64":
                case "int64_t":
                case "ssize_t":
                    la = new Token(TokenType.S64);
                    break;
                case "void":
                    la = new Token(TokenType.VOID);
                    break;
            }
        }
        sym = la.type;
    }

    private void check(TokenType type) throws ParseException {
        if (la.type != type) {
            error("expected " + type + "; was " + la.type);
        }
        scan();
    }

    public Function parse() throws ParseException {
        scan();
        Function prototype = prototype();
        check(TokenType.EOF);
        return prototype;
    }

    private Function prototype() throws ParseException {
        Type returnType = type();
        check(TokenType.IDENT);
        String name = t.str;

        Expression expr = null;
        if (sym == TokenType.LT) {
            scan();
            expr = expr();
            check(TokenType.GT);
        }

        check(TokenType.LPAR);
        if (sym == TokenType.RPAR) {
            scan();
            return new Function(name, new Prototype(returnType, expr));
        }
        List<Type> args = new ArrayList<>();
        Type type = type();
        if (type.getType() == DataType.VOID) {
            check(TokenType.RPAR);
            return new Function(name, new Prototype(returnType, args, expr));
        } else {
            args.add(type);
        }
        if (sym == TokenType.IDENT) {
            scan();
            // name: t.str
        }
        while (sym == TokenType.COMMA) {
            scan();
            type = type();
            if (type.getType() == DataType.VOID) {
                error("unexpected type void");
            }
            args.add(type);
            if (sym == TokenType.IDENT) {
                scan();
                // name: t.str
            }
        }
        check(TokenType.RPAR);

        return new Function(name, new Prototype(returnType, args, expr));
    }

    private Type type() throws ParseException {
        Type type = basic();
        while (sym == TokenType.MUL) {
            scan();
            if (sym == TokenType.CONST) {
                scan();
                type = new Type(type, true);
            } else {
                type = new Type(type, false);
            }
        }
        if (sym == TokenType.LT) {
            scan();
            Expression expr = expr();
            type.setExpression(expr);
            check(TokenType.GT);
        }
        if (sym == TokenType.IDENT) {
            switch (la.str) {
                case "$out":
                    scan();
                    if (type.getType() == DataType.PTR || type.getType() == DataType.STRING) {
                        type.setRepresentation(Representation.HEX);
                    }
                    break;
                case "$char":
                    scan();
                    type.setRepresentation(Representation.CHAR);
                    break;
                case "$dec":
                    scan();
                    type.setRepresentation(Representation.DEC);
                    break;
                case "$hex":
                    scan();
                    type.setRepresentation(Representation.HEX);
                    break;
                default:
                    return type;
            }
        }
        return type;
    }

    private Type integer(boolean isConst) throws ParseException {
        boolean unsigned = false;
        if (sym == TokenType.UNSIGNED) {
            scan();
            unsigned = true;
        } else if (sym == TokenType.SIGNED) {
            scan();
        }
        if (unsigned) {
            switch (sym) {
                case CHAR:
                    scan();
                    return new Type(DataType.U8, isConst);
                case SHORT:
                    scan();
                    return new Type(DataType.U16, isConst);
                case INT:
                    scan();
                    return new Type(DataType.U32, isConst);
                case LONG:
                    scan();
                    return new Type(DataType.U64, isConst);
                default:
                    error("unexpected token " + sym);
                    throw new AssertionError("unreachable");
            }
        } else {
            switch (sym) {
                case CHAR:
                    scan();
                    return new Type(DataType.S8, isConst);
                case SHORT:
                    scan();
                    return new Type(DataType.S16, isConst);
                case INT:
                    scan();
                    return new Type(DataType.S32, isConst);
                case LONG:
                    scan();
                    return new Type(DataType.S64, isConst);
                default:
                    error("unexpected token " + sym);
                    throw new AssertionError("unreachable");
            }
        }
    }

    private Type basic() throws ParseException {
        boolean isConst = false;
        if (sym == TokenType.CONST) {
            scan();
            isConst = true;
        }

        switch (sym) {
            case UNSIGNED:
            case SIGNED:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
                return integer(isConst);
            case U8:
                scan();
                return new Type(DataType.U8, isConst);
            case S8:
                scan();
                return new Type(DataType.S8, isConst);
            case U16:
                scan();
                return new Type(DataType.U16, isConst);
            case S16:
                scan();
                return new Type(DataType.S16, isConst);
            case U32:
                scan();
                return new Type(DataType.U32, isConst);
            case S32:
                scan();
                return new Type(DataType.S32, isConst);
            case U64:
                scan();
                return new Type(DataType.U64, isConst);
            case S64:
                scan();
                return new Type(DataType.S64, isConst);
            case VOID:
                scan();
                return new Type(DataType.VOID);
            case IDENT: // TODO: user defined types
                scan();
                return new Type(DataType.VOID);
            default:
                error("unexpected token " + sym);
                throw new AssertionError("unreachable");
        }
    }

    private Expression expr() throws ParseException {
        Expression result = sum();
        while (sym == TokenType.ADD || sym == TokenType.SUB) {
            scan();
            if (t.type == TokenType.ADD) {
                result = new AddNode(result, sum());
            } else {
                result = new SubNode(result, sum());
            }
        }
        return result;
    }

    private Expression sum() throws ParseException {
        Expression result = factor();
        while (sym == TokenType.MUL || sym == TokenType.DIV) {
            scan();
            if (t.type == TokenType.MUL) {
                result = new MulNode(result, factor());
            } else {
                result = new DivNode(result, factor());
            }
        }
        return result;
    }

    private Expression factor() throws ParseException {
        if (sym == TokenType.SUB) {
            scan();
            if (sym == TokenType.NUMBER) {
                scan();
                return new ValueNode(-t.value);
            } else {
                return new NegNode(nexpr());
            }
        } else if (sym == TokenType.NUMBER) {
            scan();
            return new ValueNode(t.value);
        } else if (sym == TokenType.IDENT) {
            scan();
            String name = t.str;
            if (sym == TokenType.LPAR) {
                scan();
                List<Expression> args = new ArrayList<>();
                if (sym != TokenType.RPAR) {
                    args.add(nexpr());
                    while (sym == TokenType.COMMA) {
                        scan();
                        args.add(nexpr());
                    }
                }
                check(TokenType.RPAR);
                return new CallNode(name, args);
            } else {
                return new VariableNode(name);
            }
        } else if (sym == TokenType.LPAR) {
            scan();
            Expression result = nexpr();
            check(TokenType.RPAR);
            return result;
        } else if (sym == TokenType.NOT) {
            scan();
            return new NotNode(expr());
        } else if (sym == TokenType.INV) {
            scan();
            return new InvNode(nexpr());
        } else {
            error("unexpected token: " + sym);
            return null; // unreachable
        }
    }

    // nested expr
    private Expression nexpr() throws ParseException {
        Expression result = lor();
        while (sym == TokenType.LOR) {
            scan();
            result = new LogicOrNode(result, lor());
        }
        return result;
    }

    private Expression lor() throws ParseException {
        Expression result = land();
        while (sym == TokenType.LAND) {
            scan();
            result = new LogicAndNode(result, land());
        }
        return result;
    }

    private Expression land() throws ParseException {
        Expression result = rel();
        while (true) {
            switch (sym) {
                case GT:
                    scan();
                    result = new GtNode(result, rel());
                    break;
                case LT:
                    scan();
                    result = new LtNode(result, rel());
                    break;
                case EQ:
                    scan();
                    result = new EqNode(result, rel());
                    break;
                case NE:
                    scan();
                    result = new NeNode(result, rel());
                    break;
                case GE:
                    scan();
                    result = new GeNode(result, rel());
                    break;
                case LE:
                    scan();
                    result = new LeNode(result, rel());
                    break;
                default:
                    return result;
            }
        }
    }

    private Expression rel() throws ParseException {
        Expression result = or();
        while (sym == TokenType.OR) {
            scan();
            result = new OrNode(result, or());
        }
        return result;
    }

    private Expression or() throws ParseException {
        Expression result = and();
        while (sym == TokenType.AND) {
            scan();
            result = new AndNode(result, and());
        }
        return result;
    }

    private Expression and() throws ParseException {
        Expression result = sum();
        while (sym == TokenType.ADD || sym == TokenType.SUB) {
            scan();
            if (t.type == TokenType.ADD) {
                result = new AddNode(result, sum());
            } else {
                result = new SubNode(result, sum());
            }
        }
        return result;
    }
}
