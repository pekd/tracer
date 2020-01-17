package org.graalvm.vm.trcview.script;

import static org.graalvm.vm.trcview.script.TokenType.and;
import static org.graalvm.vm.trcview.script.TokenType.arrow;
import static org.graalvm.vm.trcview.script.TokenType.assign;
import static org.graalvm.vm.trcview.script.TokenType.bitand;
import static org.graalvm.vm.trcview.script.TokenType.bitor;
import static org.graalvm.vm.trcview.script.TokenType.charConst;
import static org.graalvm.vm.trcview.script.TokenType.char_;
import static org.graalvm.vm.trcview.script.TokenType.com;
import static org.graalvm.vm.trcview.script.TokenType.comma;
import static org.graalvm.vm.trcview.script.TokenType.const_;
import static org.graalvm.vm.trcview.script.TokenType.else_;
import static org.graalvm.vm.trcview.script.TokenType.eof;
import static org.graalvm.vm.trcview.script.TokenType.false_;
import static org.graalvm.vm.trcview.script.TokenType.ident;
import static org.graalvm.vm.trcview.script.TokenType.if_;
import static org.graalvm.vm.trcview.script.TokenType.int_;
import static org.graalvm.vm.trcview.script.TokenType.lbrace;
import static org.graalvm.vm.trcview.script.TokenType.lbrack;
import static org.graalvm.vm.trcview.script.TokenType.long_;
import static org.graalvm.vm.trcview.script.TokenType.lpar;
import static org.graalvm.vm.trcview.script.TokenType.minus;
import static org.graalvm.vm.trcview.script.TokenType.none;
import static org.graalvm.vm.trcview.script.TokenType.not;
import static org.graalvm.vm.trcview.script.TokenType.number;
import static org.graalvm.vm.trcview.script.TokenType.or;
import static org.graalvm.vm.trcview.script.TokenType.period;
import static org.graalvm.vm.trcview.script.TokenType.plus;
import static org.graalvm.vm.trcview.script.TokenType.rbrace;
import static org.graalvm.vm.trcview.script.TokenType.rbrack;
import static org.graalvm.vm.trcview.script.TokenType.rem;
import static org.graalvm.vm.trcview.script.TokenType.return_;
import static org.graalvm.vm.trcview.script.TokenType.rpar;
import static org.graalvm.vm.trcview.script.TokenType.semicolon;
import static org.graalvm.vm.trcview.script.TokenType.shl;
import static org.graalvm.vm.trcview.script.TokenType.short_;
import static org.graalvm.vm.trcview.script.TokenType.shr;
import static org.graalvm.vm.trcview.script.TokenType.signed_;
import static org.graalvm.vm.trcview.script.TokenType.slash;
import static org.graalvm.vm.trcview.script.TokenType.stringConst;
import static org.graalvm.vm.trcview.script.TokenType.struct_;
import static org.graalvm.vm.trcview.script.TokenType.times;
import static org.graalvm.vm.trcview.script.TokenType.true_;
import static org.graalvm.vm.trcview.script.TokenType.typedef_;
import static org.graalvm.vm.trcview.script.TokenType.unsigned_;
import static org.graalvm.vm.trcview.script.TokenType.void_;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.graalvm.vm.trcview.script.ast.ArrayAssignment;
import org.graalvm.vm.trcview.script.ast.ArrayDeclaration;
import org.graalvm.vm.trcview.script.ast.Assignment;
import org.graalvm.vm.trcview.script.ast.Block;
import org.graalvm.vm.trcview.script.ast.CallStatement;
import org.graalvm.vm.trcview.script.ast.Expression;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.ast.GetStructMember;
import org.graalvm.vm.trcview.script.ast.IfThenElse;
import org.graalvm.vm.trcview.script.ast.PointerAssignment;
import org.graalvm.vm.trcview.script.ast.PointerLoad;
import org.graalvm.vm.trcview.script.ast.PointerOperation;
import org.graalvm.vm.trcview.script.ast.PointerValueAssignment;
import org.graalvm.vm.trcview.script.ast.Return;
import org.graalvm.vm.trcview.script.ast.Statement;
import org.graalvm.vm.trcview.script.ast.StructDeclaration;
import org.graalvm.vm.trcview.script.ast.Typedef;
import org.graalvm.vm.trcview.script.ast.Variable;
import org.graalvm.vm.trcview.script.ast.VariableDeclaration;
import org.graalvm.vm.trcview.script.ast.expr.AddNode;
import org.graalvm.vm.trcview.script.ast.expr.AndNode;
import org.graalvm.vm.trcview.script.ast.expr.ArrayNode;
import org.graalvm.vm.trcview.script.ast.expr.ArrayRead;
import org.graalvm.vm.trcview.script.ast.expr.CallNode;
import org.graalvm.vm.trcview.script.ast.expr.ConstantNode;
import org.graalvm.vm.trcview.script.ast.expr.ConstantStringNode;
import org.graalvm.vm.trcview.script.ast.expr.DivNode;
import org.graalvm.vm.trcview.script.ast.expr.EqNode;
import org.graalvm.vm.trcview.script.ast.expr.GeNode;
import org.graalvm.vm.trcview.script.ast.expr.GtNode;
import org.graalvm.vm.trcview.script.ast.expr.InvNode;
import org.graalvm.vm.trcview.script.ast.expr.LeNode;
import org.graalvm.vm.trcview.script.ast.expr.LogicAndNode;
import org.graalvm.vm.trcview.script.ast.expr.LogicOrNode;
import org.graalvm.vm.trcview.script.ast.expr.LtNode;
import org.graalvm.vm.trcview.script.ast.expr.MulNode;
import org.graalvm.vm.trcview.script.ast.expr.NeNode;
import org.graalvm.vm.trcview.script.ast.expr.NegNode;
import org.graalvm.vm.trcview.script.ast.expr.NotNode;
import org.graalvm.vm.trcview.script.ast.expr.OrNode;
import org.graalvm.vm.trcview.script.ast.expr.PointerValueLoad;
import org.graalvm.vm.trcview.script.ast.expr.RemNode;
import org.graalvm.vm.trcview.script.ast.expr.ShlNode;
import org.graalvm.vm.trcview.script.ast.expr.ShrNode;
import org.graalvm.vm.trcview.script.ast.expr.SubNode;
import org.graalvm.vm.trcview.script.ast.expr.VariableNode;
import org.graalvm.vm.trcview.script.type.ArrayType;
import org.graalvm.vm.trcview.script.type.BasicType;
import org.graalvm.vm.trcview.script.type.PointerType;
import org.graalvm.vm.trcview.script.type.PrimitiveType;
import org.graalvm.vm.trcview.script.type.Struct;
import org.graalvm.vm.trcview.script.type.Struct.Member;
import org.graalvm.vm.trcview.script.type.Type;

/*
 * EBNF:
 *
 * program = { typedef | struct | vardecl | func } .
 * struct = "struct" ident [ "{" vardecl { vardecl } "}" ] .
 * typedef = "typedef" type ident .
 * basictype = [ "signed" | "unsigned" ] (
 *     | [ "short" | "long" | "long" "long" ] "int"
 *     | "char"
 *     | "short"
 *     | "long"
 *     ) .
 * type = { [ "const" ] [ "*" ] } ( struct | basictype | ident ) { "*" } .
 * vardecl = type ident { "[" ( ident | number ) "]" }
 *     [ "=" ( ident | charliteral | number ) ]
 *     ";" .
 * func = type ident "(" argslist ")" "{" { statement } "}" .
 * argslist = [ type ident { "," type ident } ] .
 * statement = ( type ident [ "=" expr ] ";"
 *     | expr
 *     ) .
 * expr =
 */
public class Parser {
    private Token t;
    private Token la;
    private TokenType sym;

    private final Scanner scanner;
    public final Errors errors;

    private int errdst;
    private final static int MIN_ERROR_DISTANCE = 3;

    public final TypeTable types;
    public final SymbolTable symtab;
    public final Map<String, Long> constants;

    public Parser(String s) {
        this(new Scanner(s));
    }

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        errors = scanner.errors;
        // Avoid crash when 1st symbol has scanner error.
        la = new Token(none, 1, 1);
        errdst = MIN_ERROR_DISTANCE;
        types = new TypeTable(this::error);
        symtab = new SymbolTable(this::error);
        constants = new HashMap<>();
    }

    public void error(Message msg, Object... msgParams) {
        if (errdst >= MIN_ERROR_DISTANCE) {
            errors.error(la.line, la.col, msg, msgParams);
        }
        errdst = 0;
    }

    private void scan() {
        t = la;
        la = scanner.next();
        sym = la.type;
        errdst++;
    }

    private void check(TokenType expected) {
        if (sym == expected) {
            scan();
        } else {
            error(Message.TOKEN_EXPECTED, expected);
        }
    }

    public void parse() {
        scan();
        program();
        check(eof);
    }

    private boolean isTypedef() {
        return sym == typedef_;
    }

    private boolean isStruct() {
        return sym == struct_;
    }

    private boolean isType() {
        switch (sym) {
            case ident:
            case union_:
            case struct_:
            case signed_:
            case unsigned_:
            case const_:
            case char_:
            case short_:
            case int_:
            case long_:
            case void_:
                return true;
            default:
                return false;
        }
    }

    private boolean isBasicType() {
        switch (sym) {
            case signed_:
            case unsigned_:
            case const_:
            case char_:
            case short_:
            case int_:
            case long_:
            case void_:
                return true;
            default:
                return false;
        }
    }

    private boolean isStatement() {
        switch (sym) {
            case ident:
            case return_:
            case for_:
            case break_:
            case continue_:
            case switch_:
            case do_:
            case true_:
            case false_:
            case if_:
                return true;
            default:
                return false;
        }
    }

    private void program() {
        while (isTypedef() || isStruct() || isType()) {
            if (isTypedef()) {
                Typedef typedef = typedef();
                check(semicolon);
                types.add(typedef.getType(), typedef.getName());
            } else if (isStruct()) {
                Struct struct = struct();
                check(semicolon);
                if (struct.getName() == null) {
                    error(Message.ANONYMOUS_STRUCT);
                }
                types.addStruct(struct, struct.getName());
            } else {
                Function func = func();
                symtab.define(func);
            }
        }
    }

    private Typedef typedef() {
        check(typedef_);
        Type type = type();
        check(ident);
        return new Typedef(type, t.str);
    }

    private Type type() {
        Type type;
        boolean isconst = false;

        while (sym == const_ || sym == times) {
            if (sym == const_) {
                isconst = true;
            }
            scan();
        }
        if (isStruct()) {
            Struct struct = struct();
            if (struct.getName() != null && struct.getMembers().size() > 0) {
                if (types.hasStruct(struct.getName())) {
                    Struct s = types.getStruct(struct.getName());
                    List<Member> members = s.getMembers();
                    if (members.isEmpty()) {
                        for (Member member : struct.getMembers()) {
                            s.addMember(member.name, member.type);
                        }
                    } else {
                        error(Message.DUPLICATE_TYPE, struct.getName());
                    }
                } else {
                    types.addStruct(struct, struct.getName());
                }
            } else if (struct.size() == 0) {
                String name = struct.getName();
                if (name != null) {
                    struct = types.getStruct(name);
                } else {
                    types.addStruct(struct, struct.getName());
                }
            }
            type = struct;
        } else if (isBasicType()) {
            type = basicType();
        } else if (sym == ident) {
            scan();
            type = types.get(t.str);
        } else {
            error(Message.TYPE_EXPECTED);
            type = new PrimitiveType(BasicType.VOID);
        }

        type.setConst(isconst);

        while (sym == times) {
            scan();
            type = new PointerType(type);
        }

        return type;
    }

    private VariableDeclaration vardecl() {
        Type type = type();
        check(ident);
        String name = t.str;
        while (sym == lbrack) {
            scan();
            if (sym == ident) {
                scan();
                // TODO: implement variable array sizes
                error(Message.NOT_IMPLEMENTED);
            } else if (sym == number) {
                scan();
                type = new ArrayType(type, t.val);
            } else {
                error(Message.ARRAY_DIMENSION);
            }
            check(rbrack);
        }
        return new VariableDeclaration(type, name);
    }

    private Struct struct() {
        check(struct_);

        Struct struct;
        if (sym == ident) {
            scan();
            struct = new Struct(t.str);
        } else {
            struct = new Struct();
        }

        if (sym == lbrace) {
            scan();
            while (sym != rbrace) {
                VariableDeclaration var = vardecl();
                struct.addMember(var.getName(), var.getType());
                check(semicolon);
            }
            check(rbrace);
        }
        return struct;
    }

    private Type basicType() {
        boolean unsigned = false;
        boolean hassign = false;
        if (sym == signed_) {
            scan();
            unsigned = false;
            hassign = true;
        } else if (sym == unsigned_) {
            scan();
            unsigned = true;
            hassign = true;
        }

        if (sym == short_) {
            scan();
            if (sym == int_) {
                scan();
            }
            return new PrimitiveType(BasicType.SHORT, unsigned);
        } else if (sym == long_) {
            boolean longlong = false;
            scan();
            if (sym == long_) {
                scan();
                longlong = true;
            }
            if (sym == int_) {
                scan();
            }
            if (longlong) {
                return new PrimitiveType(BasicType.LONGLONG, unsigned);
            } else {
                return new PrimitiveType(BasicType.LONG, unsigned);
            }
        } else if (sym == char_) {
            scan();
            return new PrimitiveType(BasicType.CHAR, unsigned);
        } else if (sym == int_) {
            scan();
            return new PrimitiveType(BasicType.INT, unsigned);
        } else if (sym == void_) {
            scan();
            if (hassign) {
                error(Message.VOID_WITH_SIGN);
            }
            return new PrimitiveType(BasicType.VOID);
        } else {
            error(Message.TYPE_EXPECTED);
            return new PrimitiveType(BasicType.VOID);
        }
    }

    private Function func() {
        symtab.enter();
        Type rettype = type();
        check(ident);
        String name = t.str;
        check(lpar);
        List<VariableDeclaration> vars = argslist();
        List<Type> argtypes = vars.stream().map(VariableDeclaration::getType).collect(Collectors.toList());
        List<Variable> argvars = vars.stream().map(x -> symtab.get(x.getName())).collect(Collectors.toList());
        check(rpar);
        if (sym == lbrace) {
            check(lbrace);
            List<Statement> body = new ArrayList<>();
            while (isType() || isStatement()) {
                Statement stmt = statement();
                if (stmt != null) {
                    body.add(stmt);
                }
            }
            check(rbrace);
            symtab.leave();
            return new Function(name, rettype, argtypes, argvars, body);
        } else {
            check(semicolon);
            symtab.leave();
            return new Function(name, rettype, argtypes, argvars);
        }
    }

    private List<VariableDeclaration> argslist() {
        List<VariableDeclaration> vars = new ArrayList<>();
        if (isType()) {
            Type type = type();
            check(ident);
            vars.add(new VariableDeclaration(type, t.str));
            symtab.define(t.str, type);
        }
        while (sym == comma) {
            scan();
            Type type = type();
            check(ident);
            vars.add(new VariableDeclaration(type, t.str));
            symtab.define(t.str, type);
        }
        return vars;
    }

    private Statement assignment(PointerOperation op, String name) {
        if (sym == lbrack) {
            // array access
            Type type = op.getType();
            if (!(type instanceof ArrayType) && !(type instanceof PointerType)) {
                error(Message.NOT_AN_ARRAY, name);
            }
            scan();
            Expression idx = expr();
            check(rbrack);
            check(assign);
            Expression val = expr();
            return new ArrayAssignment(op, idx, val);
        } else if (sym == period) {
            // struct member access
            String typename = t.str;
            Type type = op.getType();
            if (!(type instanceof Struct)) {
                error(Message.NOT_A_STRUCT, typename);
                type = new Struct();
            }
            scan();
            check(ident);
            String membername = t.str;
            Struct struct = (Struct) type;
            Member member = struct.getMember(membername);
            if (member == null) {
                error(Message.UNKNOWN_MEMBER, membername);
                member = Struct.DUMMY_MEMBER;
            }
            PointerOperation deref = new GetStructMember(op, member);
            if (sym == lbrack || sym == period) {
                return assignment(deref, membername);
            } else {
                check(assign);
                Expression expr = expr();
                return new PointerValueAssignment(deref, expr);
            }
        } else if (sym == arrow) {
            // struct member access
            String typename = t.str;
            Type type = op.getType();
            if (!(type instanceof PointerType)) {
                error(Message.NOT_A_POINTER, typename);
                type = new PointerType(new PrimitiveType(BasicType.VOID));
            }
            type = ((PointerType) type).getType();
            if (!(type instanceof Struct)) {
                error(Message.NOT_A_STRUCT, typename);
                type = new Struct();
            }
            scan();
            check(ident);
            String membername = t.str;
            Struct struct = (Struct) type;
            Member member = struct.getMember(membername);
            if (member == null) {
                error(Message.UNKNOWN_MEMBER, membername);
                member = Struct.DUMMY_MEMBER;
            }
            PointerOperation deref = new GetStructMember(op, member);
            if (sym == lbrack || sym == period) {
                return assignment(deref, membername);
            } else {
                check(assign);
                Expression expr = expr();
                return new PointerValueAssignment(deref, expr);
            }
        }
        return null;
    }

    private Expression designator(PointerOperation op, String name) {
        if (sym == lbrack) {
            // array access
            Type type = op.getType();
            if (!(type instanceof ArrayType) && !(type instanceof PointerType)) {
                error(Message.NOT_AN_ARRAY, name);
            }
            scan();
            Expression idx = expr();
            check(rbrack);
            return new ArrayRead(op, idx);
        } else if (sym == period) {
            scan();
            Struct struct;
            if (!(op.getType() instanceof Struct)) {
                error(Message.NOT_A_STRUCT, name);
                struct = new Struct();
            } else {
                struct = (Struct) op.getType();
            }
            check(ident);
            String field = t.str;
            Member member = struct.getMember(field);
            if (member == null) {
                error(Message.UNKNOWN_MEMBER, field);
                member = Struct.DUMMY_MEMBER;
            }
            PointerOperation deref = new GetStructMember(op, member);
            if (sym == lbrack || sym == period || sym == arrow) {
                return designator(deref, member.name);
            } else {
                return new PointerValueLoad(deref);
            }
        } else if (sym == arrow) {
            scan();
            Struct struct = null;
            if (!(op.getType() instanceof PointerType)) {
                error(Message.NOT_A_POINTER, name);
            } else {
                Type structType = ((PointerType) op.getType()).getType();
                if (!(structType instanceof Struct)) {
                    error(Message.NOT_A_STRUCT, name);
                } else {
                    struct = (Struct) structType;
                }
            }
            check(ident);
            String membername = t.str;
            if (struct != null) {
                Member member = struct.getMember(membername);
                if (member == null) {
                    error(Message.UNKNOWN_MEMBER, membername);
                    member = Struct.DUMMY_MEMBER;
                }
                PointerOperation deref = new GetStructMember(op, member);
                if (sym == lbrack || sym == period || sym == arrow) {
                    return designator(deref, member.name);
                } else {
                    return new PointerValueLoad(deref);
                }
            } else {
                return new ConstantNode(0);
            }
        }
        return new ConstantNode(0);
    }

    private Statement statement() {
        Statement stmt;
        if (sym == ident) {
            // check if it's a variable declaration
            scan();
            if (sym == times) {
                // pointer variable declaration
                String typename = t.str;
                scan();
                check(ident);
                String name = t.str;
                Type type = new PointerType(types.get(typename));
                Variable var = symtab.define(name, type);
                if (type instanceof ArrayType) {
                    stmt = new ArrayDeclaration(var);
                } else if (type instanceof Struct) {
                    stmt = new StructDeclaration(var);
                } else if (sym == assign) {
                    scan();
                    Expression expr = expr();
                    if (type instanceof PointerType) {
                        stmt = new PointerAssignment(var, expr);
                    } else {
                        stmt = new Assignment(var, expr);
                    }
                } else {
                    stmt = null;
                }
            } else if (sym == ident) {
                // variable declaration
                String typename = t.str;
                scan();
                String name = t.str;
                Type type = types.get(typename);
                Variable var = symtab.define(name, type);
                if (type instanceof ArrayType) {
                    stmt = new ArrayDeclaration(var);
                } else if (type instanceof Struct) {
                    stmt = new StructDeclaration(var);
                } else if (sym == assign) {
                    scan();
                    Expression expr = expr();
                    if (type instanceof PointerType) {
                        stmt = new PointerAssignment(var, expr);
                    } else {
                        stmt = new Assignment(var, expr);
                    }
                } else {
                    stmt = null;
                }
            } else if (sym == lbrack || sym == period || sym == arrow) {
                Variable var = symtab.get(t.str);
                PointerOperation op = new PointerLoad(var);
                stmt = assignment(op, t.str);
            } else if (sym == lpar) {
                // call
                String name = t.str;
                scan();
                List<Expression> args = new ArrayList<>();
                if (sym != rpar) {
                    args.add(expr());
                    while (sym == comma) {
                        scan();
                        args.add(expr());
                    }
                }
                check(rpar);
                Function func = symtab.getFunction(name);
                if (func == null) {
                    error(Message.UNKNOWN_SYMBOL, name);
                    func = symtab.errorfunc;
                }
                stmt = new CallStatement(func, args);
            } else {
                Variable var = symtab.get(t.str);
                check(assign);
                Expression expr = expr();
                if (var.getType() instanceof PointerType) {
                    stmt = new PointerAssignment(var, expr);
                } else {
                    stmt = new Assignment(var, expr);
                }
            }
        } else if (sym == return_) {
            scan();
            if (sym != semicolon) {
                Expression expr = expr();
                stmt = new Return(expr);
            } else {
                stmt = new Return();
            }
        } else if (sym == if_) {
            scan();
            check(lpar);
            Expression condition = expr();
            check(rpar);
            Statement then = statement();
            if (sym == else_) {
                scan();
                Statement otherwise = statement();
                return new IfThenElse(condition, then, otherwise);
            } else {
                return new IfThenElse(condition, then);
            }
        } else if (sym == lbrace) {
            scan();
            symtab.enter();
            List<Statement> stmts = new ArrayList<>();
            while (isType() || isStatement()) {
                Statement s = statement();
                if (s != null) {
                    stmts.add(s);
                }
            }
            symtab.leave();
            check(rbrace);
            return new Block(stmts);
        } else if (isType()) {
            VariableDeclaration vardecl = vardecl();
            Type type = vardecl.getType();
            Variable var = symtab.define(vardecl.getName(), type);
            if (type instanceof ArrayType) {
                stmt = new ArrayDeclaration(var);
            } else if (type instanceof Struct) {
                stmt = new StructDeclaration(var);
            } else if (sym == assign) {
                scan();
                Expression expr = expr();
                if (type instanceof PointerType) {
                    stmt = new PointerAssignment(var, expr);
                } else {
                    stmt = new Assignment(var, expr);
                }
            } else {
                stmt = null;
            }
        } else {
            stmt = expr();
        }
        check(semicolon);
        return stmt;
    }

    private Expression expr() {
        Expression result = lor();
        while (sym == or) {
            scan();
            result = new LogicOrNode(result, lor());
        }
        return result;
    }

    private Expression lor() {
        Expression result = land();
        while (sym == and) {
            scan();
            result = new LogicAndNode(result, land());
        }
        return result;
    }

    private Expression land() {
        Expression result = rel();
        while (true) {
            switch (sym) {
                case gt:
                    scan();
                    result = new GtNode(result, rel());
                    break;
                case lt:
                    scan();
                    result = new LtNode(result, rel());
                    break;
                case eql:
                    scan();
                    result = new EqNode(result, rel());
                    break;
                case neq:
                    scan();
                    result = new NeNode(result, rel());
                    break;
                case geq:
                    scan();
                    result = new GeNode(result, rel());
                    break;
                case leq:
                    scan();
                    result = new LeNode(result, rel());
                    break;
                default:
                    return result;
            }
        }
    }

    private Expression rel() {
        Expression result = or();
        while (sym == bitor) {
            scan();
            result = new OrNode(result, or());
        }
        return result;
    }

    private Expression or() {
        Expression result = and();
        while (sym == bitand) {
            scan();
            result = new AndNode(result, and());
        }
        return result;
    }

    private Expression and() {
        Expression result = sum();
        while (sym == plus || sym == minus) {
            scan();
            if (t.type == plus) {
                result = new AddNode(result, sum());
            } else {
                result = new SubNode(result, sum());
            }
        }
        return result;
    }

    private Expression sum() {
        Expression result = shift();
        while (sym == times || sym == slash || sym == rem) {
            scan();
            if (t.type == times) {
                result = new MulNode(result, shift());
            } else if (t.type == slash) {
                result = new DivNode(result, shift());
            } else {
                result = new RemNode(result, shift());
            }
        }
        return result;
    }

    private Expression shift() {
        Expression result = factor();
        while (sym == shl || sym == shr) {
            scan();
            if (t.type == shl) {
                result = new ShlNode(result, factor());
            } else {
                result = new ShrNode(result, factor());
            }
        }
        return result;
    }

    private Expression factor() {
        if (sym == minus) {
            scan();
            if (sym == number) {
                scan();
                return new ConstantNode(-t.val);
            } else {
                return new NegNode(expr());
            }
        } else if (sym == number) {
            scan();
            return new ConstantNode(t.val);
        } else if (sym == charConst) {
            scan();
            return new ConstantNode(t.val);
        } else if (sym == stringConst) {
            scan();
            String s = t.str;
            if (sym == stringConst) {
                StringBuilder buf = new StringBuilder(s);
                while (sym == stringConst) {
                    scan();
                    buf.append(t.str);
                }
                s = buf.toString();
            }
            return new ConstantStringNode(s);
        } else if (sym == ident) {
            scan();
            String name = t.str;
            if (constants.containsKey(name)) {
                return new ConstantNode(constants.get(name));
            }
            if (sym == lpar) {
                scan();
                List<Expression> args = new ArrayList<>();
                if (sym != rpar) {
                    args.add(expr());
                    while (sym == comma) {
                        scan();
                        args.add(expr());
                    }
                }
                check(rpar);
                Function func = symtab.getFunction(name);
                if (func == null) {
                    error(Message.UNKNOWN_SYMBOL, name);
                    func = symtab.errorfunc;
                }
                return new CallNode(func, args);
            } else if (sym == lbrack) {
                scan();
                Expression index = expr();
                check(rbrack);
                Variable var = symtab.get(name);
                return new ArrayNode(var, index);
            } else if (sym == period || sym == arrow) {
                Variable var = symtab.get(name);
                PointerOperation op = new PointerLoad(var);
                return designator(op, name);
            } else {
                Variable var = symtab.get(name);
                return new VariableNode(var);
            }
        } else if (sym == lpar) {
            scan();
            Expression result = expr();
            check(rpar);
            return result;
        } else if (sym == not) {
            scan();
            return new NotNode(expr());
        } else if (sym == com) {
            scan();
            return new InvNode(expr());
        } else if (sym == true_) {
            scan();
            return new ConstantNode(1);
        } else if (sym == false_) {
            scan();
            return new ConstantNode(0);
        } else {
            error(Message.FACTOR);
            return new ConstantNode(0);
        }
    }
}
