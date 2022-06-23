package org.graalvm.vm.x86.el;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.x86.ArchitecturalState;
import org.graalvm.vm.x86.el.Token.TokenType;
import org.graalvm.vm.x86.el.ast.AddNode;
import org.graalvm.vm.x86.el.ast.AndNode;
import org.graalvm.vm.x86.el.ast.DivNode;
import org.graalvm.vm.x86.el.ast.EqNode;
import org.graalvm.vm.x86.el.ast.Expression;
import org.graalvm.vm.x86.el.ast.GeNode;
import org.graalvm.vm.x86.el.ast.GtNode;
import org.graalvm.vm.x86.el.ast.InvNode;
import org.graalvm.vm.x86.el.ast.LeNode;
import org.graalvm.vm.x86.el.ast.LogicAndNode;
import org.graalvm.vm.x86.el.ast.LogicOrNode;
import org.graalvm.vm.x86.el.ast.LtNode;
import org.graalvm.vm.x86.el.ast.MulNode;
import org.graalvm.vm.x86.el.ast.NeNode;
import org.graalvm.vm.x86.el.ast.NegNode;
import org.graalvm.vm.x86.el.ast.NotNode;
import org.graalvm.vm.x86.el.ast.OrNode;
import org.graalvm.vm.x86.el.ast.SubNode;
import org.graalvm.vm.x86.el.ast.ValueNode;
import org.graalvm.vm.x86.el.ast.VariableNode;

import com.oracle.truffle.api.CompilerAsserts;

/*
 * expr   = lor { "||" lor } .
 * lor    = land { "&&" land } .
 * land   = rel { ("<" | "<=" | ">" | ">=" | "==") rel } .
 * rel    = or { "|" or } .
 * or     = and { "&" and } .
 * and    = sum { ("+" | "-") sum } .
 * sum    = factor { ("*" | "/") factor } .
 * factor = number
 *        | ident [ "(" [ expr { "," expr } ] ")" ]
 *        | "(" expr ")"
 *        | "!" expr
 *        | "-" expr .
 */
public class ElParser {
    private Scanner scanner;

    private Token t;
    private Token la;
    private TokenType sym;

    private ArchitecturalState state;

    public ElParser(String s, ArchitecturalState state) {
        CompilerAsserts.neverPartOfCompilation();
        this.state = state;
        scanner = new Scanner(s);
    }

    private static void error(String msg) throws ParseException {
        throw new ParseException(msg, 0);
    }

    private void scan() throws ParseException {
        t = la;
        la = scanner.next();
        sym = la.type;
    }

    private void check(TokenType type) throws ParseException {
        if (la.type != type) {
            error("expected " + type + "; was " + la.type);
        }
        scan();
    }

    public Expression parse() throws ParseException {
        scan();
        Expression expr = expr();
        check(TokenType.EOF);
        return expr;
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
                return Call.create(name, args, state);
            } else {
                return new VariableNode(name, state);
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
