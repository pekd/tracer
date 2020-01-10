package org.graalvm.vm.trcview.arch.custom;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.custom.decode.CustomCallDecoder;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.EventParser;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.script.Parser;
import org.graalvm.vm.trcview.script.SymbolTable;
import org.graalvm.vm.trcview.script.TypeTable;
import org.graalvm.vm.trcview.script.ast.Function;
import org.graalvm.vm.trcview.script.ast.Intrinsic;
import org.graalvm.vm.trcview.script.rt.Context;
import org.graalvm.vm.trcview.script.type.Type;

public class CustomArchitecture extends Architecture {
    public final SymbolTable symbols;
    public final TypeTable types;
    public final Context context;
    private final short id;
    private String name;
    private String description;

    private Type stepType;
    private Type stateType;

    public CustomArchitecture(String script) {
        this(script, Collections.emptyList());
    }

    public CustomArchitecture(String script, List<Intrinsic> intrinsics) {
        Parser parser = new Parser(script);
        Intrinsics.register(parser.symtab, this);
        for (Intrinsic intrinsic : intrinsics) {
            parser.symtab.define(intrinsic);
        }
        parser.parse();
        if (parser.errors.numErrors() > 0) {
            throw new IllegalArgumentException(parser.errors.dump());
        }
        symbols = parser.symtab;
        types = parser.types;

        Function init = symbols.getFunction("init");
        if (init == null) {
            throw new IllegalArgumentException("no init function");
        }
        context = new Context(symbols);
        id = (short) init.execute(context);
    }

    @Override
    public short getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public Type getStepType() {
        return stepType;
    }

    void setStepType(Type stepType) {
        this.stepType = stepType;
    }

    public Type getStateType() {
        return stateType;
    }

    void setStateType(Type stateType) {
        this.stateType = stateType;
    }

    @Override
    public ArchTraceReader getTraceReader(InputStream in) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventParser getEventParser() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SyscallDecoder getSyscallDecoder() {
        return null;
    }

    @Override
    public CallDecoder getCallDecoder() {
        return new CustomCallDecoder();
    }

    @Override
    public int getTabSize() {
        return 8;
    }

    @Override
    public StepFormat getFormat() {
        return null;
    }

    @Override
    public boolean isSystemLevel() {
        return false;
    }
}
