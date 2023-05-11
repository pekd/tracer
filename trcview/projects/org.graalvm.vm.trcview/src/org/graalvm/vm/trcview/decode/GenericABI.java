package org.graalvm.vm.trcview.decode;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.UserTypeDatabase;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.expression.ast.Expression;
import org.graalvm.vm.util.log.Trace;

public class GenericABI extends ABI {
    private static final Logger log = Trace.create(GenericABI.class);

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

    public void loadSyscallDefinitions(String syscallInfo, UserTypeDatabase types) {
        int lineno = 1;
        for (String line : syscallInfo.split("\n")) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            try {
                String[] parts = line.split(":");
                int id = Integer.parseInt(parts[0]);
                String decl = parts[1];

                Parser parser = new Parser(decl, types);
                parser.setReplaceStructByVoid(true);
                Function fun = parser.parsePrototype();
                addSyscall(id, fun);
            } catch (ParseException | NumberFormatException e) {
                log.info("Parse error in line " + lineno + ": " + e.getMessage() + " (line was: " + line + ")");
            }

            lineno++;
        }
    }
}
