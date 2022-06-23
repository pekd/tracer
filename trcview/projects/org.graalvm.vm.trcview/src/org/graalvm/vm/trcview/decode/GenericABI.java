package org.graalvm.vm.trcview.decode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.expression.ast.Expression;

public class GenericABI extends ABI {
    private final GenericCallingConvention call = new GenericCallingConvention();
    private final GenericCallingConvention syscall = new GenericCallingConvention();

    private Expression syscallId;
    private final Map<Long, Function> syscalls = new HashMap<>();

    public GenericABI() {
        call.addChangeListener(this::fireChangeEvent);
        syscall.addChangeListener(this::fireChangeEvent);
    }

    @Override
    public GenericCallingConvention getCall() {
        return call;
    }

    @Override
    public GenericCallingConvention getSyscall() {
        return syscall;
    }

    public void setSyscallId(Expression expr) {
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

    @Override
    public Function getSyscall(long id) {
        return syscalls.get(id);
    }

    @Override
    public Expression getSyscallId() {
        return syscallId;
    }
}
