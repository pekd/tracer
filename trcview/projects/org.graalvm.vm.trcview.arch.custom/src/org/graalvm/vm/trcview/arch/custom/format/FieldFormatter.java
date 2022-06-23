package org.graalvm.vm.trcview.arch.custom.format;

import java.util.List;

import org.graalvm.vm.trcview.arch.io.CpuState;

public class FieldFormatter {
    private final List<Command> commands;

    public static abstract class Command {
        public abstract String execute(CpuState state);
    }

    public static class TextCommand extends Command {
        public final String text;

        public TextCommand(String text) {
            this.text = text;
        }

        @Override
        public String execute(CpuState state) {
            return text;
        }
    }

    public static class FieldCommand extends Command {
        public final String field;
        public final Format format;

        public FieldCommand(String field, Format format) {
            this.field = field;
            this.format = format;
        }

        @Override
        public String execute(CpuState state) {
            switch (format.numberfmt) {
                case Format.FORMAT_OCT:
                    return "{{" + format.format(state.get(field)) + "}}o";
                case Format.FORMAT_DEC:
                    return "{{" + format.format(state.get(field)) + "}}d";
                case Format.FORMAT_HEX:
                    return "{{" + format.format(state.get(field)) + "}}x";
                default:
                    return format.format(state.get(field));
            }
        }
    }

    public FieldFormatter(List<Command> commands) {
        this.commands = commands;
    }

    public FieldFormatter(String desc) {
        this.commands = FormatParser.parse(desc);
    }

    public String format(CpuState state) {
        StringBuilder buf = new StringBuilder();
        for (Command cmd : commands) {
            buf.append(cmd.execute(state));
        }
        return buf.toString();
    }
}
