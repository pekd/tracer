package org.graalvm.vm.trcview.arch.none.io;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.io.WordInputStream;

public class GenericStateDescription {
    private final Map<String, Field> fields = new HashMap<>();
    private final List<Command> commands = new ArrayList<>();
    private final int size;

    private final Strings strings = new Strings();

    private final StepFormat fmt;

    public GenericStateDescription(WordInputStream in) throws IOException {
        int numberfmt = in.read8bit();
        int addrwidth = in.read8bit();
        int wordwidth = in.read8bit();
        int machinecodesz = in.read8bit();

        fmt = new StepFormat(numberfmt & 0x7F, addrwidth, wordwidth, machinecodesz, numberfmt < 0);

        size = in.read16bit();

        int count = in.read16bit();
        for (int i = 0; i < count; i++) {
            int offset = in.read16bit();
            int type = (byte) in.read8bit();
            int len = in.read8bit();
            byte[] rawname = new byte[len];
            in.read(rawname);
            String name = new String(rawname, StandardCharsets.UTF_8);
            Field field = new Field(name, offset, type & 0x7F, type < 0);
            addField(field);
        }

        // parse format commands
        int state = 0;
        StringBuilder buf = new StringBuilder();
        String format = IO.readString(in);
        for (char c : format.toCharArray()) {
            switch (state) {
                default:
                case 0: // normal text
                    switch (c) {
                        case '$':
                            state = 1; // '$'
                            break;
                        case '\\':
                            state = 3; // '\\'
                            break;
                        default:
                            buf.append(c);
                            break;
                    }
                    break;
                case 1: // '$'
                    if (c == '{') {
                        state = 2;
                        if (buf.length() > 0) {
                            commands.add(new TextCommand(buf.toString()));
                        }
                        buf = new StringBuilder();
                    } else {
                        state = 0;
                        buf.append("$").append(c);
                    }
                    break;
                case 2: // "${"
                    if (c == '}') {
                        state = 0;
                        String fieldName = buf.toString();
                        buf = new StringBuilder();
                        Field field = getField(fieldName);
                        if (field == null) {
                            commands.add(new TextCommand("${" + fieldName + "}"));
                        } else {
                            commands.add(new FieldCommand(field));
                        }
                    } else {
                        buf.append(c);
                    }
                    break;
                case 3: // '\\'
                    state = 0;
                    buf.append(c);
                    break;
            }
        }
        if (buf.length() > 0) {
            commands.add(new TextCommand(buf.toString()));
        }
    }

    public void addField(Field field) {
        fields.put(field.getName(), field);
    }

    public Field getField(String name) {
        return fields.get(name);
    }

    public int getSize() {
        return size;
    }

    public String format(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (Command cmd : commands) {
            cmd.execute(buf, data);
        }
        return buf.toString();
    }

    public StepFormat getFormat() {
        return fmt;
    }

    public String[] readStrings(WordInputStream in) throws IOException {
        return strings.read(in);
    }
}
