package org.graalvm.vm.trcview.arch.custom.format;

import java.util.ArrayList;
import java.util.List;

import org.graalvm.vm.trcview.arch.custom.format.FieldFormatter.Command;
import org.graalvm.vm.trcview.arch.custom.format.FieldFormatter.FieldCommand;
import org.graalvm.vm.trcview.arch.custom.format.FieldFormatter.TextCommand;

public class FormatParser {
    static List<Command> parse(String format) {
        List<Command> commands = new ArrayList<>();
        StringBuilder buf = new StringBuilder(format.length());
        StringBuilder tmp = null;
        int state = 0;
        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);
            switch (state) {
                case 0: // normal text
                    if (c == '$') {
                        commands.add(new TextCommand(buf.toString()));
                        buf = new StringBuilder();
                        state = 1;
                    } else if (c == '\\') {
                        state = 3;
                    } else {
                        buf.append(c);
                    }
                    break;
                case 1: // $
                    if (c == '{') {
                        tmp = new StringBuilder();
                        state = 2;
                    } else {
                        state = 0;
                        buf.append('$');
                    }
                    break;
                case 2:
                    if (c == '}') {
                        String field = tmp.toString();
                        Format fmt = new Format(Format.FORMAT_DEC, 1);
                        int idx = field.indexOf(';');
                        if (idx != -1) {
                            String fmtstr = field.substring(idx + 1);
                            field = field.substring(0, idx);
                            int width = Integer.parseInt(fmtstr.substring(1));
                            int type = Format.FORMAT_DEC;
                            switch (fmtstr.charAt(0)) {
                                case 'd':
                                case 'D':
                                    type = Format.FORMAT_DEC;
                                    break;
                                case 'o':
                                case 'O':
                                    type = Format.FORMAT_OCT;
                                    break;
                                case 'x':
                                case 'X':
                                    type = Format.FORMAT_HEX;
                                    break;
                            }
                            fmt = new Format(type, width);
                        }
                        commands.add(new FieldCommand(field, fmt));
                        state = 0;
                    } else {
                        tmp.append(c);
                    }
                    break;
                case 3:
                    switch (c) {
                        case 'r':
                            buf.append('\r');
                            break;
                        case 'n':
                            buf.append('\n');
                            break;
                        case 't':
                            buf.append('\t');
                            break;
                        default:
                            buf.append(c);
                            break;
                    }
                    state = 0;
                    break;
            }
        }
        if (state != 0) {
            throw new IllegalArgumentException("syntax error: unexpected EOF");
        }
        if (buf.length() > 0) {
            commands.add(new TextCommand(buf.toString()));
        }
        return commands;
    }
}
