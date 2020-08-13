package org.graalvm.vm.trcview.arch.none.io;

public class TextCommand extends Command {
    private final String text;

    public TextCommand(String text) {
        this.text = text;
    }

    @Override
    public void execute(StringBuilder out, byte[] data) {
        out.append(text);
    }
}
