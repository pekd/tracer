package org.graalvm.vm.trcview.arch.none.io;

public class FieldCommand extends Command {
    private final Field field;

    public FieldCommand(Field field) {
        this.field = field;
    }

    @Override
    public void execute(StringBuilder out, byte[] data) {
        out.append(field.format(data));
    }
}
