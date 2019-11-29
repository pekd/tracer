package org.graalvm.vm.trcview.net.protocol.cmd;

import java.io.IOException;

import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public abstract class Command {
    public static final int NOP = 0;

    public static final int GET_SYMBOL = 1;
    public static final int GET_COMPUTED_SYMBOL = 2;
    public static final int RENAME_SYMBOL = 3;
    public static final int SET_PROTOTYPE = 4;
    public static final int GET_SUBROUTINES = 5;
    public static final int GET_LOCATIONS = 6;
    public static final int GET_SYMBOLS = 7;
    public static final int ADD_LISTENER = 8;
    public static final int ADD_SUBROUTINE = 9;
    public static final int REANALYZE = 10;

    public static final int GET_STEP_COUNT = 11;

    public static final int GET_ROOT = 12;
    public static final int GET_CHILDREN = 13;
    public static final int GET_NODE = 14;

    public static final int GET_INSTRUCTION = 15;
    public static final int GET_NEXT_STEP = 16;
    public static final int GET_PREVIOUS_STEP = 17;
    public static final int GET_NEXT_PC = 18;

    public static final int GET_I8 = 19;
    public static final int GET_I16 = 20;
    public static final int GET_I64 = 21;
    public static final int GET_LAST_READ = 22;
    public static final int GET_NEXT_READ = 23;
    public static final int GET_LAST_WRITE = 24;
    public static final int GET_MAP_NODE = 25;

    public static final int GET_BASE = 26;
    public static final int GET_LOAD_BIAS = 27;
    public static final int GET_OFFSET = 28;
    public static final int GET_FILE_OFFSET = 29;
    public static final int GET_FILENAME = 30;

    private final int type;

    protected Command(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public abstract void read(WordInputStream in) throws IOException;

    public abstract void write(WordOutputStream out) throws IOException;
}
