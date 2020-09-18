package org.graalvm.vm.trcview.storage.sql;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.graalvm.vm.posix.elf.Symbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol.Type;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.arch.io.TraceSymbol;
import org.graalvm.vm.trcview.expression.Parser;
import org.graalvm.vm.trcview.storage.SessionStorage;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

public class SQLSessionStorage extends SessionStorage {
    private static final Logger log = Trace.create(SQLSessionStorage.class);

    private static final String DEFAULT_URL = "jdbc:derby:memory:trcviewsession;create=true";

    private static final int TYPE_LOCATION = 0;
    private static final int TYPE_SUBROUTINE = 1;

    // NULL stream for Derby logging
    public static final OutputStream NULL = new OutputStream() {
        @Override
        public void write(int b) {
            // nothing
        }
    };

    // @formatter:off
    private static final String[] CREATE_TABLES = {
                    "CREATE TABLE SYMBOL (" +
                    "    TRACE      VARCHAR(36) NOT NULL," +
                    "    VALUE      BIGINT NOT NULL," +
                    "    SIZE       BIGINT NOT NULL," +
                    "    TYPE       SMALLINT NOT NULL," +
                    "    BIND       SMALLINT NOT NULL," +
                    "    VISIBILITY SMALLINT NOT NULL," +
                    "    SHNDX      INT NOT NULL," +
                    "    NAME       VARCHAR(256) NOT NULL," +
                    "    PROTOTYPE  CLOB," +
                    "    PRIMARY KEY (TRACE, VALUE, SIZE, TYPE, VISIBILITY)" +
                    ")",
                    "CREATE TABLE COMPUTEDSYMBOL (" +
                    "    TRACE      VARCHAR(36) NOT NULL," +
                    "    VALUE      BIGINT NOT NULL," +
                    "    SIZE       BIGINT NOT NULL WITH DEFAULT 0," +
                    "    TYPE       SMALLINT NOT NULL," +
                    "    NAME       VARCHAR(256) NOT NULL," +
                    "    PROTOTYPE  CLOB," +
                    "    PRIMARY KEY (TRACE, VALUE)" +
                    ")",
                    "CREATE TABLE COMMENTINSN (" +
                    "    TRACE      VARCHAR(36) NOT NULL," +
                    "    INSN       BIGINT NOT NULL," +
                    "    TEXT       CLOB NOT NULL," +
                    "    PRIMARY KEY (TRACE, INSN)" +
                    ")",
                    "CREATE TABLE COMMENTPC (" +
                    "    TRACE      VARCHAR(36) NOT NULL," +
                    "    PC         BIGINT NOT NULL," +
                    "    TEXT       CLOB NOT NULL," +
                    "    PRIMARY KEY (TRACE, PC)" +
                    ")",
                    "CREATE TABLE EXPRESSION (" +
                    "    TRACE      VARCHAR(36) NOT NULL," +
                    "    PC         BIGINT NOT NULL," +
                    "    EXPRESSION CLOB NOT NULL," +
                    "    PRIMARY KEY (TRACE, PC)" +
                    ")",
                    "CREATE TABLE HIGHLIGHT (" +
                    "    TRACE      VARCHAR(36) NOT NULL," +
                    "    PC         BIGINT NOT NULL," +
                    "    COLOR      VARCHAR(8) NOT NULL," +
                    "    PRIMARY KEY (TRACE, PC)" +
                    ")",
    };
    // @formatter:on

    private final Connection con;

    private final PreparedStatement getSymbol;
    private final PreparedStatement getComputedSymbol;
    private final PreparedStatement getComputedSymbols;
    private final PreparedStatement getAllComputedSymbols;
    private final PreparedStatement createSymbol;
    private final PreparedStatement updateSymbol;
    private final PreparedStatement updateSymbolName;
    private final PreparedStatement updateSymbolPrototype;

    private final PreparedStatement getCommentPC;
    private final PreparedStatement getCommentsPC;
    private final PreparedStatement getCommentInsn;
    private final PreparedStatement getCommentsInsn;
    private final PreparedStatement createCommentPC;
    private final PreparedStatement createCommentInsn;
    private final PreparedStatement updateCommentPC;
    private final PreparedStatement updateCommentInsn;
    private final PreparedStatement deleteCommentPC;
    private final PreparedStatement deleteCommentInsn;

    private final PreparedStatement getHighlight;
    private final PreparedStatement getHighlights;
    private final PreparedStatement createHighlight;
    private final PreparedStatement updateHighlight;
    private final PreparedStatement deleteHighlight;

    private String trace;

    public SQLSessionStorage() throws SQLException {
        this(null, DEFAULT_URL, null, null);
    }

    public SQLSessionStorage(String driver, String url) throws SQLException {
        this(driver, url, null, null);
    }

    public SQLSessionStorage(String driver, String url, String user, String pass) throws SQLException {
        // Derby hack
        System.setProperty("derby.stream.error.field", this.getClass().getCanonicalName() + ".NULL");

        if (driver != null) {
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                log.log(Levels.WARNING, "Cannot load driver " + driver, e);
            }
        }

        if (user == null) {
            con = DriverManager.getConnection(url);
        } else {
            con = DriverManager.getConnection(url, user, pass);
        }

        DatabaseMetaData metadata = con.getMetaData();
        String productName = metadata.getDatabaseProductName();
        String productVersion = metadata.getDatabaseProductVersion();
        String driverName = metadata.getDriverName();
        String driverVersion = metadata.getDriverVersion();

        log.info("Connected to " + productName + " " + productVersion + " using driver " + driverName + " " + driverVersion);

        // try to create tables
        try (Statement stmt = con.createStatement()) {
            for (String sql : CREATE_TABLES) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            // tables probably exist already
            log.info("Statement failed: " + e.getMessage());
        }

        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        con.setAutoCommit(true);

        getSymbol = con.prepareStatement("SELECT * FROM SYMBOL WHERE TRACE = ? AND ? BETWEEN VALUE AND (VALUE + SIZE) ORDER BY SIZE ASC FETCH FIRST ROW ONLY");
        getComputedSymbol = con.prepareStatement("SELECT * FROM COMPUTEDSYMBOL WHERE TRACE = ? AND ? BETWEEN VALUE AND (VALUE + SIZE) ORDER BY SIZE ASC FETCH FIRST ROW ONLY");
        getComputedSymbols = con.prepareStatement("SELECT * FROM COMPUTEDSYMBOL WHERE TRACE = ? AND TYPE = ? ORDER BY VALUE ASC");
        getAllComputedSymbols = con.prepareStatement("SELECT * FROM COMPUTEDSYMBOL WHERE TRACE = ? ORDER BY VALUE ASC");
        createSymbol = con.prepareStatement("INSERT INTO COMPUTEDSYMBOL (TRACE, VALUE, SIZE, TYPE, NAME, PROTOTYPE) VALUES (?, ?, ?, ?, ?, ?)");
        updateSymbol = con.prepareStatement("UPDATE COMPUTEDSYMBOL SET SIZE = ?, TYPE = ?, NAME = ?, PROTOTYPE = ? WHERE TRACE = ? AND VALUE = ?");
        updateSymbolName = con.prepareStatement("UPDATE COMPUTEDSYMBOL SET NAME = ? WHERE TRACE = ? AND VALUE = ?");
        updateSymbolPrototype = con.prepareStatement("UPDATE COMPUTEDSYMBOL SET PROTOTYPE = ? WHERE TRACE = ? AND VALUE = ?");

        getCommentPC = con.prepareStatement("SELECT * FROM COMMENTPC WHERE TRACE = ? AND PC = ?");
        getCommentsPC = con.prepareStatement("SELECT * FROM COMMENTPC WHERE TRACE = ?");
        getCommentInsn = con.prepareStatement("SELECT * FROM COMMENTINSN WHERE TRACE = ? AND INSN = ?");
        getCommentsInsn = con.prepareStatement("SELECT * FROM COMMENTINSN WHERE TRACE = ?");
        createCommentPC = con.prepareStatement("INSERT INTO COMMENTPC (TRACE, PC, TEXT) VALUES (?, ?, ?)");
        createCommentInsn = con.prepareStatement("INSERT INTO COMMENTINSN (TRACE, INSN, TEXT) VALUES (?, ?, ?)");
        updateCommentPC = con.prepareStatement("UPDATE COMMENTPC SET TEXT = ? WHERE TRACE = ? AND PC = ?");
        updateCommentInsn = con.prepareStatement("UPDATE COMMENTINSN SET TEXT = ? WHERE TRACE = ? AND INSN = ?");
        deleteCommentPC = con.prepareStatement("DELETE FROM COMMENTPC WHERE TRACE = ? AND PC = ?");
        deleteCommentInsn = con.prepareStatement("DELETE FROM COMMENTINSN WHERE TRACE = ? AND INSN = ?");

        getHighlight = con.prepareStatement("SELECT * FROM HIGHLIGHT WHERE TRACE = ? AND PC = ?");
        getHighlights = con.prepareStatement("SELECT * FROM HIGHLIGHT WHERE TRACE = ?");
        createHighlight = con.prepareStatement("INSERT INTO HIGHLIGHT (TRACE, PC, COLOR) VALUES (?, ?, ?)");
        updateHighlight = con.prepareStatement("UPDATE HIGHLIGHT SET COLOR = ? WHERE TRACE = ? AND PC = ?");
        deleteHighlight = con.prepareStatement("DELETE FROM HIGHLIGHT WHERE TRACE = ? AND PC = ?");
    }

    @Override
    public void close() throws IOException {
        try {
            con.rollback();
            con.setAutoCommit(false);
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
        try {
            con.close();
        } catch (SQLException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void setTrace(String trace) {
        this.trace = trace;
    }

    private static Symbol getSymbol(ResultSet rs) throws SQLException {
        String name = rs.getString("NAME");
        long value = rs.getLong("VALUE");
        long size = rs.getLong("SIZE");
        int bind = rs.getInt("BIND");
        int type = rs.getInt("TYPE");
        int visibility = rs.getInt("VISIBILITY");
        int shndx = rs.getInt("SHNDX");
        return new TraceSymbol(name, value, size, bind, type, visibility, (short) shndx);
    }

    @Override
    public Symbol getSymbol(long pc) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getSymbol.setString(1, trace);
            getSymbol.setLong(2, pc);
            try (ResultSet rs = getSymbol.executeQuery()) {
                if (rs.next()) {
                    return getSymbol(rs);
                } else {
                    // no match
                    return null;
                }
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getSymbol: " + e.getMessage(), e);
            return null;
        }
    }

    private static ComputedSymbol getComputedSymbol(ResultSet rs) throws SQLException {
        String name = rs.getString("NAME");
        long address = rs.getLong("VALUE");
        int typeId = rs.getInt("TYPE");
        String proto = rs.getString("PROTOTYPE");
        Type type = null;
        switch (typeId) {
            default:
            case TYPE_LOCATION:
                type = Type.LOCATION;
                break;
            case TYPE_SUBROUTINE:
                type = Type.SUBROUTINE;
                break;
        }
        ComputedSymbol sym = new ComputedSymbol(name, address, type);
        if (proto != null) {
            try {
                Function fun = new Parser(proto).parsePrototype();
                sym.prototype = fun.getPrototype();
            } catch (ParseException e) {
                log.log(Levels.INFO, "Invalid prototype " + proto + " stored in database: " + e.getMessage(), e);
            }
        }
        return sym;
    }

    @Override
    public ComputedSymbol getComputedSymbol(long pc) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getComputedSymbol.setString(1, trace);
            getComputedSymbol.setLong(2, pc);
            try (ResultSet rs = getComputedSymbol.executeQuery()) {
                if (rs.next()) {
                    return getComputedSymbol(rs);
                } else {
                    // no symbol found
                    return null;
                }
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getComputedSymbol: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void renameSymbol(ComputedSymbol sym, String name) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            updateSymbolName.setString(1, name);
            updateSymbolName.setString(2, trace);
            updateSymbolName.setLong(3, sym.address);
            if (updateSymbolName.executeUpdate() != 1) {
                log.log(Levels.WARNING, "Failed to rename symbol at location 0x" + HexFormatter.tohex(sym.address));
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing updateSymbolName: " + e.getMessage(), e);
        }
    }

    @Override
    public void setPrototype(ComputedSymbol sym, Prototype prototype) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            if (prototype == null) {
                updateSymbolPrototype.setString(1, null);
            } else {
                String str = prototype.returnType.toString() + " f(" + prototype.args.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
                updateSymbolPrototype.setString(1, str);
            }
            updateSymbolPrototype.setString(2, trace);
            updateSymbolPrototype.setLong(3, sym.address);
            if (updateSymbolPrototype.executeUpdate() != 1) {
                log.log(Levels.WARNING, "Failed to prototype of symbol at location 0x" + HexFormatter.tohex(sym.address));
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing updateSymbolPrototype: " + e.getMessage(), e);
        }
    }

    @Override
    public Set<ComputedSymbol> getSubroutines() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getComputedSymbols.setString(1, trace);
            getComputedSymbols.setInt(2, TYPE_SUBROUTINE);
            try (ResultSet rs = getComputedSymbols.executeQuery()) {
                Set<ComputedSymbol> result = new HashSet<>();
                while (rs.next()) {
                    result.add(getComputedSymbol(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getComputedSymbols: " + e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ComputedSymbol> getLocations() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getComputedSymbols.setString(1, trace);
            getComputedSymbols.setInt(2, TYPE_LOCATION);
            try (ResultSet rs = getComputedSymbols.executeQuery()) {
                Set<ComputedSymbol> result = new HashSet<>();
                while (rs.next()) {
                    result.add(getComputedSymbol(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getComputedSymbols: " + e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public Collection<ComputedSymbol> getSymbols() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getAllComputedSymbols.setString(1, trace);
            try (ResultSet rs = getAllComputedSymbols.executeQuery()) {
                Set<ComputedSymbol> result = new HashSet<>();
                while (rs.next()) {
                    result.add(getComputedSymbol(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getAllComputedSymbols: " + e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    @Override
    public void createComputedSymbol(ComputedSymbol sym) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            con.setAutoCommit(false);
            ComputedSymbol oldsym = getComputedSymbol(sym.address);
            if (oldsym == null) {
                createSymbol.setString(1, trace);
                createSymbol.setLong(2, sym.address);
                createSymbol.setLong(3, 0); // size
                switch (sym.type) {
                    default:
                    case LOCATION:
                        createSymbol.setInt(4, TYPE_LOCATION);
                        break;
                    case SUBROUTINE:
                        createSymbol.setInt(4, TYPE_SUBROUTINE);
                        break;
                }
                createSymbol.setString(5, sym.name);
                if (sym.prototype == null) {
                    createSymbol.setString(6, null);
                } else {
                    String str = sym.prototype.returnType.toString() + " f(" + sym.prototype.args.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
                    createSymbol.setString(6, str);
                }
                if (createSymbol.executeUpdate() != 1) {
                    log.log(Levels.WARNING, "Failed to insert symbol " + sym.name + " at location 0x" + HexFormatter.tohex(sym.address));
                }
            } else {
                updateSymbol.setLong(1, 0); // size
                switch (sym.type) {
                    default:
                    case LOCATION:
                        updateSymbol.setInt(2, TYPE_LOCATION);
                        break;
                    case SUBROUTINE:
                        updateSymbol.setInt(2, TYPE_SUBROUTINE);
                        break;
                }
                updateSymbol.setString(3, sym.name);
                if (sym.prototype == null) {
                    updateSymbol.setString(4, null);
                } else {
                    String str = sym.prototype.returnType.toString() + " f(" + sym.prototype.args.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
                    updateSymbol.setString(4, str);
                }
                updateSymbol.setString(5, trace);
                updateSymbol.setLong(6, sym.address);
                if (updateSymbol.executeUpdate() != 1) {
                    log.log(Levels.WARNING, "Failed to update symbol " + sym.name + " at location 0x" + HexFormatter.tohex(sym.address));
                }
            }
            con.commit();
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing createSymbol: " + e.getMessage(), e);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                log.log(Levels.WARNING, "Error while restoring autocommit: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void setCommentForPC(long pc, String comment) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            con.setAutoCommit(false);
            if (getCommentForPC(pc) == null) {
                // insert
                if (comment == null) {
                    // nothing to insert
                    return;
                }
                try {
                    createCommentPC.setString(1, trace);
                    createCommentPC.setLong(2, pc);
                    createCommentPC.setString(3, comment);
                    if (createCommentPC.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to create comment at location 0x" + HexFormatter.tohex(pc));
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing createCommentPC: " + e.getMessage(), e);
                }
            } else if (comment == null) {
                // delete
                try {
                    deleteCommentPC.setString(1, trace);
                    deleteCommentPC.setLong(2, pc);
                    if (deleteCommentPC.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to delete comment at location 0x" + HexFormatter.tohex(pc));
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing deleteCommentPC: " + e.getMessage(), e);
                }
            } else {
                // update
                try {
                    updateCommentPC.setString(1, comment);
                    updateCommentPC.setString(2, trace);
                    updateCommentPC.setLong(3, pc);
                    if (updateCommentPC.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to update comment at location 0x" + HexFormatter.tohex(pc));
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing updateCommentPC: " + e.getMessage(), e);
                }
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing commit: " + e.getMessage(), e);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                log.log(Levels.WARNING, "Error while restoring autocommit: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String getCommentForPC(long pc) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getCommentPC.setString(1, trace);
            getCommentPC.setLong(2, pc);
            try (ResultSet rs = getCommentPC.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TEXT");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getCommentPC: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void setCommentForInsn(long insn, String comment) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            con.setAutoCommit(false);
            if (getCommentForInsn(insn) == null) {
                // insert
                if (comment == null) {
                    // nothing to insert
                    return;
                }
                try {
                    createCommentInsn.setString(1, trace);
                    createCommentInsn.setLong(2, insn);
                    createCommentInsn.setString(3, comment);
                    if (createCommentInsn.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to create comment at insn #" + insn);
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing createCommentInsn: " + e.getMessage(), e);
                }
            } else if (comment == null) {
                // delete
                try {
                    deleteCommentInsn.setString(1, trace);
                    deleteCommentInsn.setLong(2, insn);
                    if (deleteCommentInsn.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to delete comment at insn #" + insn);
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing deleteCommentInsn: " + e.getMessage(), e);
                }
            } else {
                // update
                try {
                    updateCommentInsn.setString(1, comment);
                    updateCommentInsn.setString(2, trace);
                    updateCommentInsn.setLong(3, insn);
                    if (updateCommentInsn.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to update comment at insn #" + insn);
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing updateCommentInsn: " + e.getMessage(), e);
                }
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing commit: " + e.getMessage(), e);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                log.log(Levels.WARNING, "Error while restoring autocommit: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String getCommentForInsn(long insn) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getCommentInsn.setString(1, trace);
            getCommentInsn.setLong(2, insn);
            try (ResultSet rs = getCommentInsn.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("TEXT");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getCommentInsn: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<Long, String> getCommentsForInsns() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getCommentsInsn.setString(1, trace);
            try (ResultSet rs = getCommentsInsn.executeQuery()) {
                Map<Long, String> result = new HashMap<>();
                while (rs.next()) {
                    long insn = rs.getLong("INSN");
                    String comment = rs.getString("TEXT");
                    result.put(insn, comment);
                }
                return result;
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getCommentsInsn: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<Long, String> getCommentsForPCs() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getCommentsPC.setString(1, trace);
            try (ResultSet rs = getCommentsPC.executeQuery()) {
                Map<Long, String> result = new HashMap<>();
                while (rs.next()) {
                    long pc = rs.getLong("PC");
                    String comment = rs.getString("TEXT");
                    result.put(pc, comment);
                }
                return result;
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getCommentsPC: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void setExpression(long pc, String expression) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        // TODO Auto-generated method stub

    }

    @Override
    public String getExpression(long pc) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Long, String> getExpressions() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        // TODO Auto-generated method stub
        return null;
    }

    private static Color getColor(ResultSet rs) throws SQLException {
        String color = rs.getString("COLOR");
        int r = Integer.parseInt(color.substring(0, 2), 16);
        int g = Integer.parseInt(color.substring(2, 4), 16);
        int b = Integer.parseInt(color.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    private static String getColor(Color c) {
        return String.format("%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    @Override
    public void setColor(long pc, Color color) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            con.setAutoCommit(false);
            if (getColor(pc) == null) {
                // insert
                if (color == null) {
                    // nothing to insert
                    return;
                }
                try {
                    createHighlight.setString(1, trace);
                    createHighlight.setLong(2, pc);
                    createHighlight.setString(3, getColor(color));
                    if (createHighlight.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to create highlight at location 0x" + HexFormatter.tohex(pc));
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing createHighlight: " + e.getMessage(), e);
                }
            } else if (color == null) {
                // delete
                try {
                    deleteHighlight.setString(1, trace);
                    deleteHighlight.setLong(2, pc);
                    if (deleteHighlight.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to delete highlight at location 0x" + HexFormatter.tohex(pc));
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing deleteHighlight: " + e.getMessage(), e);
                }
            } else {
                // update
                try {
                    updateHighlight.setString(1, getColor(color));
                    updateHighlight.setString(2, trace);
                    updateHighlight.setLong(3, pc);
                    if (updateHighlight.executeUpdate() != 1) {
                        log.log(Levels.WARNING, "Failed to update highlight at location 0x" + HexFormatter.tohex(pc));
                    }
                } catch (SQLException e) {
                    log.log(Levels.WARNING, "Error while executing updateHighlight: " + e.getMessage(), e);
                }
            }
            con.commit();
            con.setAutoCommit(true);
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing commit: " + e.getMessage(), e);
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                log.log(Levels.WARNING, "Error while restoring autocommit: " + e.getMessage(), e);
            }
        }

    }

    @Override
    public Color getColor(long pc) {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getHighlight.setString(1, trace);
            getHighlight.setLong(2, pc);
            try (ResultSet rs = getHighlight.executeQuery()) {
                if (rs.next()) {
                    return getColor(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getHighlight: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Map<Long, Color> getColors() {
        if (trace == null) {
            throw new IllegalStateException("No trace selected");
        }
        try {
            getHighlights.setString(1, trace);
            try (ResultSet rs = getHighlight.executeQuery()) {
                Map<Long, Color> colors = new HashMap<>();
                while (rs.next()) {
                    long pc = rs.getLong("PC");
                    Color color = getColor(rs);
                    colors.put(pc, color);
                }
                return colors;
            }
        } catch (SQLException e) {
            log.log(Levels.WARNING, "Error while executing getHighlights: " + e.getMessage(), e);
            return null;
        }
    }
}
