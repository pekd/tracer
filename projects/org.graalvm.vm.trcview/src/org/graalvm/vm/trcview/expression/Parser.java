package org.graalvm.vm.trcview.expression;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.analysis.type.DataType;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Representation;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.expression.Token.TokenType;
import org.graalvm.vm.trcview.expression.ast.AddNode;
import org.graalvm.vm.trcview.expression.ast.AndNode;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.DivNode;
import org.graalvm.vm.trcview.expression.ast.EqNode;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.trcview.expression.ast.GeNode;
import org.graalvm.vm.trcview.expression.ast.GtNode;
import org.graalvm.vm.trcview.expression.ast.InvNode;
import org.graalvm.vm.trcview.expression.ast.LeNode;
import org.graalvm.vm.trcview.expression.ast.LogicAndNode;
import org.graalvm.vm.trcview.expression.ast.LogicOrNode;
import org.graalvm.vm.trcview.expression.ast.LtNode;
import org.graalvm.vm.trcview.expression.ast.MulNode;
import org.graalvm.vm.trcview.expression.ast.NeNode;
import org.graalvm.vm.trcview.expression.ast.NegNode;
import org.graalvm.vm.trcview.expression.ast.NotNode;
import org.graalvm.vm.trcview.expression.ast.OrNode;
import org.graalvm.vm.trcview.expression.ast.SarNode;
import org.graalvm.vm.trcview.expression.ast.ShlNode;
import org.graalvm.vm.trcview.expression.ast.ShrNode;
import org.graalvm.vm.trcview.expression.ast.SubNode;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
import org.graalvm.vm.trcview.expression.ast.XorNode;

/*
 * def       = ( "typedef" type ident ";" | struct | union ) .
 * struct    = "struct" [ ident ] "{"
 *           { type ident ";" }
 *           "}" ";"
 *           .
 * union     = "union" [ ident ] "{"
 *           { type ident ";" }
 *           "}" ";"
 *           .
 *
 * prototype = type ident ["<" rexpr ">" ] "(" [ type [ ident ] { "," type [ ident ] } ] ")" .
 * type      = basic { "*" ["const"] } ["$out" | "$dec" | "$hex" | "$char"] .
 * basic     = ["const"]
 *           ( ["unsigned" | "signed"] integer
 *           | fractional
 *           | "u8"
 *           | "s8"
 *           | "u16"
 *           | "s16"
 *           | "u32"
 *           | "s32"
 *           | "u64"
 *           | "s64"
 *           | "void" )
 *           [ "<" rexpr ">" ]
 *           .
 * integer   = "char"
 *           | "short" [ "int" ]
 *           | "int"
 *           | "long" [ "int" ]
 *           .
 * fractional= "float"
 *           | "double"
 *           .
 * rexpr     = and .
 *
 * expr      = lor { "||" lor } .
 * lor       = land { "&&" land } .
 * land      = rel { ("<" | "<=" | ">" | ">=" | "==") rel } .
 * rel       = or { "|" or } .
 * or        = xor { "^" xor } .
 * xor       = and { "&" and } .
 * and       = sum { ("+" | "-") sum } .
 * sum       = factor { ("*" | "/") factor } .
 * factor    = shift { ("<<" | ">>" | ">>>") shift } .
 * shift     = number
 *           | ident [ "(" [ expr { "," expr } ] ")" ]
 *           | "(" expr ")"
 *           | "!" expr
 *           | "-" expr .
 */
public class Parser {
    private Scanner scanner;

    private Token t;
    private Token la;
    private TokenType sym;

    public Parser(String s) {
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
                case "float":
                    la = new Token(TokenType.FLOAT);
                    break;
                case "double":
                    la = new Token(TokenType.DOUBLE);
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
                case "f32":
                    la = new Token(TokenType.F32);
                    break;
                case "f64":
                    la = new Token(TokenType.F64);
                    break;
                case "void":
                    la = new Token(TokenType.VOID);
                    break;
                case "struct":
                    la = new Token(TokenType.STRUCT);
                    break;
                case "union":
                    la = new Token(TokenType.UNION);
                    break;
                case "typedef":
                    la = new Token(TokenType.TYPEDEF);
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

    public Function parsePrototype() throws ParseException {
        scan();
        Function prototype = prototype();
        check(TokenType.EOF);
        return prototype;
    }

    private Function prototype() throws ParseException {
        Type returnType = type();
        check(TokenType.IDENT);
        String name = t.str;

        check(TokenType.LPAR);
        if (sym == TokenType.RPAR) {
            scan();
            return new Function(name, new Prototype(returnType));
        }
        List<Type> args = new ArrayList<>();
        List<String> argnames = new ArrayList<>();
        Type type = type();
        if (type.getType() == DataType.VOID) {
            check(TokenType.RPAR);
            return new Function(name, new Prototype(returnType, args, argnames));
        } else {
            args.add(type);
        }
        if (sym == TokenType.IDENT) {
            scan();
            argnames.add(t.str);
        } else {
            argnames.add(null);
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
                argnames.add(t.str);
            } else {
                argnames.add(null);
            }
        }
        check(TokenType.RPAR);

        return new Function(name, new Prototype(returnType, args, argnames));
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
            Expression expr = rexpr();
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
                case "$oct":
                    scan();
                    type.setRepresentation(Representation.OCT);
                    break;
                case "$hex":
                    scan();
                    type.setRepresentation(Representation.HEX);
                    break;
                case "$r50":
                case "$rad50":
                    scan();
                    type.setRepresentation(Representation.RAD50);
                    break;
                case "$fx32":
                    scan();
                    type.setRepresentation(Representation.FX32);
                    break;
                case "$float":
                    scan();
                    type.setRepresentation(Representation.FLOAT);
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
                    if (sym == TokenType.INT) {
                        scan();
                    }
                    return new Type(DataType.U16, isConst);
                case INT:
                    scan();
                    return new Type(DataType.U32, isConst);
                case LONG:
                    scan();
                    if (sym == TokenType.INT) {
                        scan();
                    }
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
                    if (sym == TokenType.INT) {
                        scan();
                    }
                    return new Type(DataType.S16, isConst);
                case INT:
                    scan();
                    return new Type(DataType.S32, isConst);
                case LONG:
                    scan();
                    if (sym == TokenType.INT) {
                        scan();
                    }
                    return new Type(DataType.S64, isConst);
                default:
                    error("unexpected token " + sym);
                    throw new AssertionError("unreachable");
            }
        }
    }

    private Type fractional(boolean isConst) throws ParseException {
        switch (sym) {
            case FLOAT:
                scan();
                return new Type(DataType.F32, isConst);
            case DOUBLE:
                scan();
                return new Type(DataType.F64, isConst);
            default:
                error("unexpected token " + sym);
                throw new AssertionError("unreachable");
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
            case FLOAT:
            case DOUBLE:
                return fractional(isConst);
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
            case F32:
                scan();
                return new Type(DataType.F32, isConst);
            case F64:
                scan();
                return new Type(DataType.F64, isConst);
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

    public Expression parseExpression() throws ParseException {
        scan();
        Expression expr = expr();
        check(TokenType.EOF);
        return expr;
    }

    private Expression rexpr() throws ParseException {
        return and();
    }

    private Expression expr() throws ParseException {
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
        Expression result = xor();
        while (sym == TokenType.XOR) {
            scan();
            result = new XorNode(result, xor());
        }
        return result;
    }

    private Expression xor() throws ParseException {
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
        Expression result = shift();
        while (sym == TokenType.SHL || sym == TokenType.SHR || sym == TokenType.SAR) {
            scan();
            if (t.type == TokenType.SHL) {
                result = new ShlNode(result, shift());
            } else if (t.type == TokenType.SHR) {
                result = new ShrNode(result, shift());
            } else if (t.type == TokenType.SAR) {
                result = new SarNode(result, shift());
            }
        }
        return result;
    }

    private Expression shift() throws ParseException {
        if (sym == TokenType.SUB) {
            scan();
            if (sym == TokenType.NUMBER) {
                scan();
                return new ValueNode(-t.value);
            } else {
                return new NegNode(expr());
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
                    args.add(expr());
                    while (sym == TokenType.COMMA) {
                        scan();
                        args.add(expr());
                    }
                }
                check(TokenType.RPAR);
                return new CallNode(name, args);
            } else {
                return new VariableNode(name);
            }
        } else if (sym == TokenType.LPAR) {
            scan();
            Expression result = expr();
            check(TokenType.RPAR);
            return result;
        } else if (sym == TokenType.NOT) {
            scan();
            return new NotNode(expr());
        } else if (sym == TokenType.INV) {
            scan();
            return new InvNode(expr());
        } else {
            error("unexpected token: " + sym);
            return null; // unreachable
        }
    }
}
