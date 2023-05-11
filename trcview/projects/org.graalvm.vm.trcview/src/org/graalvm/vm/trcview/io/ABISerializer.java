package org.graalvm.vm.trcview.io;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.decode.GenericCallingConvention;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;

public class ABISerializer {
    private static String encode(GenericCallingConvention cc) {
        String[] parts = new String[cc.getFixedArgumentCount() + 2];
        if (cc.getReturn() != null) {
            parts[0] = cc.getReturn().toString();
        } else {
            parts[0] = null;
        }
        if (cc.getStack() != null) {
            parts[1] = cc.getStack().toString();
        } else {
            parts[1] = null;
        }
        for (int i = 0; i < cc.getFixedArgumentCount(); i++) {
            parts[i + 2] = cc.getArgument(i).toString();
        }
        return TextSerializer.encode(parts);
    }

    private static void decode(GenericCallingConvention cc, String s) throws IOException, ParseException {
        String[] parts = TextSerializer.tokenize(s);
        if (parts[0] != null) {
            cc.setReturn(new Parser(parts[0]).parseExpression());
        } else {
            cc.setReturn(null);
        }
        if (parts[1] != null) {
            cc.setStack(new Parser(parts[1]).parseExpression());
        } else {
            cc.setStack(null);
        }
        List<Expression> args = new ArrayList<>();
        for (int i = 0; i < parts.length - 2; i++) {
            args.add(new Parser(parts[i + 2]).parseExpression());
        }
        cc.setArguments(args);
    }

    public static String store(GenericABI abi) {
        String[] parts = new String[3 + abi.getSyscalls().size()];
        parts[0] = encode(abi.getCall());
        parts[1] = encode(abi.getSyscall());
        parts[2] = abi.getSyscallId() != null ? abi.getSyscallId().toString() : null;
        List<Entry<Long, Function>> syscalls = new ArrayList<>(abi.getSyscalls().entrySet());
        for (int i = 0; i < syscalls.size(); i++) {
            long id = syscalls.get(i).getKey();
            Function func = syscalls.get(i).getValue();
            parts[i + 3] = id + "=" + func.toString();
        }
        return TextSerializer.encode(parts);
    }

    public static void load(GenericABI abi, String s, UserTypeDatabase db) throws IOException, ParseException {
        String[] parts = TextSerializer.tokenize(s);
        decode(abi.getCall(), parts[0]);
        decode(abi.getSyscall(), parts[1]);
        if (parts[2] != null) {
            abi.setSyscallId(new Parser(parts[2], db).parseExpression());
        } else {
            abi.setSyscallId(null);
        }
        Map<Long, Function> syscalls = new HashMap<>();
        for (int i = 0; i < parts.length - 3; i++) {
            String def = parts[i + 3];
            int idx = def.indexOf('=');
            if (idx == -1) {
                throw new IOException("Invalid syscall declaration: " + def);
            }
            try {
                long id = Long.parseUnsignedLong(def.substring(0, idx));
                Function func = new Parser(def.substring(idx + 1), db).parsePrototype();
                syscalls.put(id, func);
            } catch (NumberFormatException e) {
                throw new IOException("invalid syscall id: " + e.getMessage(), e);
            }
        }
        abi.setSyscalls(syscalls);
    }
}
