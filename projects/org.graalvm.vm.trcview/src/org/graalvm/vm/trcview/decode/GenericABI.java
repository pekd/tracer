package org.graalvm.vm.trcview.decode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.expression.ast.Expression;

public class GenericABI extends ABI {
    private final List<Expression> callArguments = new ArrayList<>();
    private Expression callReturn;

    private Expression syscallId;
    private final Map<Long, Function> syscalls = new HashMap<>();
    private final List<Expression> syscallArguments = new ArrayList<>();
    private Expression syscallReturn;

    public void setReturnExpression(Expression expr) {
        callReturn = expr;
        fireChangeEvent();
    }

    public void setCallArguments(List<Expression> expr) {
        callArguments.clear();
        callArguments.addAll(expr);
        fireChangeEvent();
    }

    public void setSyscallIdExpression(Expression expr) {
        syscallId = expr;
        fireChangeEvent();
    }

    public void addSyscall(long id, Function format) {
        syscalls.put(id, format);
        fireChangeEvent();
    }

    public Map<Long, Function> getSyscalls() {
        return Collections.unmodifiableMap(syscalls);
    }

    public void setSyscalls(Map<Long, Function> sc) {
        syscalls.clear();
        syscalls.putAll(sc);
        fireChangeEvent();
    }

    public void setSyscallReturnExpression(Expression expr) {
        syscallReturn = expr;
        fireChangeEvent();
    }

    public void setSyscallArguments(List<Expression> expr) {
        syscallArguments.clear();
        syscallArguments.addAll(expr);
        fireChangeEvent();
    }

    @Override
    public Function getSyscall(long id) {
        return syscalls.get(id);
    }

    public List<Expression> getCallArguments() {
        return Collections.unmodifiableList(callArguments);
    }

    @Override
    public int getCallArgumentCount() {
        return callArguments.size();
    }

    @Override
    public Expression getCallArgument(int i) {
        return getCallArguments().get(i);
    }

    @Override
    public Expression getReturnExpression() {
        return callReturn;
    }

    @Override
    public Expression getSyscallIdExpression() {
        return syscallId;
    }

    public List<Expression> getSyscallArguments() {
        return Collections.unmodifiableList(syscallArguments);
    }

    @Override
    public int getSyscallArgumentCount() {
        return syscallArguments.size();
    }

    @Override
    public Expression getSyscallArgument(int i) {
        return syscallArguments.get(i);
    }

    @Override
    public Expression getSyscallReturnExpression() {
        return syscallReturn;
    }
}
