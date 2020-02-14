package org.graalvm.vm.trcview.storage.cassandra;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.graalvm.vm.trcview.storage.MemoryAccess;
import org.graalvm.vm.trcview.storage.Step;
import org.graalvm.vm.trcview.storage.StorageBackend;
import org.graalvm.vm.trcview.storage.TraceMetadata;
import org.graalvm.vm.util.log.Levels;
import org.graalvm.vm.util.log.Trace;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.InvalidKeyspaceException;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.protocol.internal.util.Bytes;

public class CassandraBackend extends StorageBackend {
    private static final Logger log = Trace.create(CassandraBackend.class);

    public static final String KEYSPACE = "TRCVIEW";

    private static final int BATCH_SIZE = 1000;
    private static final String CREATE_KEYSPACE = "CREATE KEYSPACE %s WITH REPLICATION = {'class':'SimpleStrategy','replication_factor':1};";

    // @formatter:off
    private static final String[] CREATE_SCHEMA = {
                    "CREATE TABLE TRACES (" +
                    "    ID UUID PRIMARY KEY," +
                    "    NAME VARCHAR," +
                    "    TIME TIMESTAMP," +
                    "    ARCH INT," +
                    "    STEPCOUNT BIGINT" +
                    ");",
                    "CREATE INDEX TRACES_NAME ON TRACES (NAME);"};

    private static final String[] CREATE_TRACE = {
                    "CREATE TABLE TRACE_%s (" +
                    "    ID      BIGINT," +
                    "    PARENT  BIGINT," +
                    "    TID     INT," +
                    "    PC      BIGINT," +
                    "    TYPE    TINYINT," +
                    "    MACHINECODE BLOB," +
                    "    STATE   BLOB," +
                    "    PRIMARY KEY (PARENT, ID, TID)" +
                    ") WITH CLUSTERING ORDER BY (ID ASC)" +
                    "AND compression = {'class': 'LZ4Compressor', 'chunk_length_in_kb': 128};",
                    "CREATE TABLE PCIDX_%s (" +
                    "    PC      BIGINT," +
                    "    ID      BIGINT," +
                    "    TID     INT," +
                    "    PRIMARY KEY (PC, ID)" +
                    ") WITH CLUSTERING ORDER BY (ID ASC)" +
                    "AND compression = {'class': 'LZ4Compressor', 'chunk_length_in_kb': 128};",
                    "CREATE TABLE MEMREAD_%s (" +
                    "    TID     INT," +
                    "    ID      BIGINT," +
                    "    ADDRESS BIGINT," +
                    "    VALUE   TINYINT," +
                    "    BASE    BIGINT," +
                    "    SIZE    TINYINT," +
                    "    PRIMARY KEY (ADDRESS, ID)" +
                    ") WITH CLUSTERING ORDER BY (ID DESC)" +
                    "AND compression = {'class': 'LZ4Compressor', 'chunk_length_in_kb': 128};",
                    "CREATE TABLE MEMWRITE_%s (" +
                    "    TID     INT," +
                    "    ID      BIGINT," +
                    "    ADDRESS BIGINT," +
                    "    VALUE   TINYINT," +
                    "    BASE    BIGINT," +
                    "    SIZE    TINYINT," +
                    "    PRIMARY KEY (ADDRESS, ID)" +
                    ") WITH CLUSTERING ORDER BY (ID DESC)" +
                    "AND compression = {'class': 'LZ4Compressor', 'chunk_length_in_kb': 128};"};

    private static final String CREATE_STEP =
                    "INSERT INTO TRACE_%s (ID, PARENT, TID, PC, TYPE, MACHINECODE, STATE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String CREATE_PCIDX =
                    "INSERT INTO PCIDX_%s (PC, ID, TID) VALUES (?, ?, ?)";
    private static final String CREATE_READ =
                    "INSERT INTO MEMREAD_%s (TID, ID, ADDRESS, VALUE, BASE, SIZE) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String CREATE_WRITE =
                    "INSERT INTO MEMWRITE_%s (TID, ID, ADDRESS, VALUE, BASE, SIZE) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
    // @formatter:on

    private final CqlSession session;

    private final PreparedStatement registerTrace;
    private final PreparedStatement getTrace;
    private final PreparedStatement updateStepCount;

    private PreparedStatement createStep;
    private PreparedStatement createPCIdx;
    private PreparedStatement createRead;
    private PreparedStatement createWrite;

    private PreparedStatement getSteps;
    private PreparedStatement getStepCount;
    private PreparedStatement getRead;
    private PreparedStatement getWrite;

    private UUID currentUuid;
    private String currentId;
    private short currentArch;
    private long stepCount;

    private final List<Statement<?>> statements = new ArrayList<>();
    private final List<CompletionStage<AsyncResultSet>> tasks = new ArrayList<>();

    public CassandraBackend() {
        CqlSession sess = null;
        try {
            sess = CqlSession.builder().withKeyspace(KEYSPACE).build();
        } catch (InvalidKeyspaceException e) {
            // keyspace does not exist
            log.info("Creating keyspace " + KEYSPACE);
            if (sess != null) {
                sess.close();
            }

            CqlSession tmpsess = CqlSession.builder().build();
            tmpsess.execute(String.format(CREATE_KEYSPACE, KEYSPACE));
            tmpsess.close();

            sess = CqlSession.builder().withKeyspace(KEYSPACE).build();
            for (String stmt : CREATE_SCHEMA) {
                sess.execute(stmt);
            }
        }

        session = sess;

        ResultSet rs = session.execute("SELECT release_version FROM system.local");
        log.info("Cassandra version: " + rs.one().getString(0));

        registerTrace = session.prepare("INSERT INTO TRACES (ID, NAME, TIME, ARCH, STEPCOUNT) VALUES (?, ?, ?, ?, 0)");
        getTrace = session.prepare("SELECT * FROM TRACES WHERE ID = ?");
        updateStepCount = session.prepare("UPDATE TRACES SET STEPCOUNT = ? WHERE ID = ?");
    }

    private void createStatements() {
        createStep = session.prepare(String.format(CREATE_STEP, currentId));
        createPCIdx = session.prepare(String.format(CREATE_PCIDX, currentId));
        createRead = session.prepare(String.format(CREATE_READ, currentId));
        createWrite = session.prepare(String.format(CREATE_WRITE, currentId));
        getSteps = session.prepare(String.format("SELECT * FROM TRACE_%s WHERE PARENT = ? AND ID >= ? LIMIT ?", currentId));
        getStepCount = session.prepare(String.format("SELECT COUNT(*) FROM TRACE_%s WHERE PARENT = ?", currentId));
        getRead = session.prepare(String.format("SELECT * FROM MEMREAD_%s WHERE ADDRESS = ? AND ID <= ? LIMIT 1", currentId));
        getWrite = session.prepare(String.format("SELECT * FROM MEMWRITE_%s WHERE ADDRESS = ? AND ID <= ? LIMIT 1", currentId));
    }

    @Override
    public List<TraceMetadata> list() {
        List<TraceMetadata> result = new ArrayList<>();
        ResultSet rs = session.execute("SELECT * FROM TRACES;");
        for (Row row : rs) {
            String id = row.getUuid("ID").toString();
            String name = row.getString("NAME");
            short arch = (short) row.getInt("ARCH");
            long steps = row.getLong("STEPCOUNT");
            result.add(new TraceMetadata(id, name, arch, steps));
        }
        return result;
    }

    @Override
    public void connect(String id) {
        ResultSet rs = session.execute(getTrace.bind(UUID.fromString(id)));
        Row row = rs.one();
        if (row == null) {
            throw new IllegalArgumentException("not found");
        }
        currentUuid = row.getUuid("ID");
        currentId = currentUuid.toString().replace("-", "");
        currentArch = (short) row.getInt("ARCH");

        createStatements();
    }

    @Override
    public void create(String name, short arch) {
        currentArch = arch;
        currentUuid = Uuids.random();
        Instant now = Instant.now();
        session.execute(registerTrace.bind(currentUuid, name, now, (int) arch));
        currentId = currentUuid.toString().replace("-", "");
        for (String stmt : CREATE_TRACE) {
            session.execute(String.format(stmt, currentId));
        }

        createStatements();
        stepCount = 0;
    }

    @Override
    public void createStep(int tid, long step, long parent, long pc, int type, byte[] machinecode, byte[] cpustate) {
        execute(createStep.bind(step, parent, tid, pc, (byte) type, ByteBuffer.wrap(machinecode), ByteBuffer.wrap(cpustate)));
        execute(createPCIdx.bind(pc, step, tid));
        stepCount++;
    }

    @Override
    public void createRead(int tid, long step, long address, int size, long value) {
        long val = value;
        for (int i = 0; i < size; i++) {
            execute(createRead.bind(tid, step, address + i, (byte) val, address, (byte) size));
            val >>= 8;
        }
    }

    @Override
    public void createRead(int tid, long step, long address, int size, long hi, long lo) {
        createRead(tid, step, address, size, lo);
        createRead(tid, step, address + 8, size, hi);
    }

    @Override
    public void createWrite(int tid, long step, long address, int size, long value) {
        long val = value;
        for (int i = 0; i < size; i++) {
            execute(createWrite.bind(tid, step, address + i, (byte) val, address, (byte) size));
            val >>= 8;
        }
    }

    @Override
    public void createWrite(int tid, long step, long address, int size, long hi, long lo) {
        createWrite(tid, step, address, size, lo);
        createWrite(tid, step, address + 8, size, hi);
    }

    private void execute(Statement<?> stmt) {
        statements.add(stmt);
        if (statements.size() >= BATCH_SIZE) {
            waitForTasks();
            for (Statement<?> s : statements) {
                if (tasks.size() > BATCH_SIZE) {
                    waitForTasks();
                }
                tasks.add(session.executeAsync(s));
            }
            statements.clear();
        }
    }

    private void waitForTasks() {
        if (!tasks.isEmpty()) {
            for (CompletionStage<AsyncResultSet> t : tasks) {
                try {
                    t.toCompletableFuture().get();
                } catch (ExecutionException | InterruptedException e) {
                    log.log(Levels.WARNING, "Error while completing future: " + e.getMessage(), e);
                }
            }
            tasks.clear();
        }
    }

    @Override
    public void flush() {
        for (Statement<?> stmt : statements) {
            tasks.add(session.executeAsync(stmt));
            if (tasks.size() >= BATCH_SIZE) {
                waitForTasks();
            }
        }
        waitForTasks();
        statements.clear();
        if (currentId != null) {
            session.execute(updateStepCount.bind(stepCount, currentUuid));
        }
    }

    @Override
    public void close() {
        flush();
        session.close();
    }

    @Override
    public List<Step> getSteps(long parent, long start, long count) {
        ResultSet rs = session.execute(getSteps.bind(parent, start, count));
        List<Step> result = new ArrayList<>();
        for (Row row : rs) {
            long id = row.getLong("ID");
            int tid = row.getInt("TID");
            long pc = row.getLong("PC");
            byte type = row.getByte("TYPE");
            byte[] machinecode = Bytes.getArray(row.getByteBuffer("MACHINECODE"));
            byte[] cpustate = Bytes.getArray(row.getByteBuffer("STATE"));
            result.add(new Step(tid, id, parent, pc, type, machinecode, cpustate));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public long getStepCount(long parent) {
        ResultSet rs = session.execute(getStepCount.bind(parent));
        return rs.one().getLong(0);
    }

    @Override
    public MemoryAccess getRead(long address, long step) {
        Row row = session.execute(getRead.bind(address, step)).one();
        if (row == null) {
            return null;
        } else {
            int tid = row.getInt("TID");
            long id = row.getLong("ID");
            long addr = row.getLong("ADDRESS");
            byte value = row.getByte("VALUE");
            long base = row.getLong("BASE");
            int size = row.getByte("SIZE");
            return new MemoryAccess(tid, id, addr, value, base, size);
        }
    }

    @Override
    public MemoryAccess getWrite(long address, long step) {
        Row row = session.execute(getWrite.bind(address, step)).one();
        if (row == null) {
            return null;
        } else {
            int tid = row.getInt("TID");
            long id = row.getLong("ID");
            long addr = row.getLong("ADDRESS");
            byte value = row.getByte("VALUE");
            long base = row.getLong("BASE");
            int size = row.getByte("SIZE");
            return new MemoryAccess(tid, id, addr, value, base, size);
        }
    }

    @Override
    public short getArchitecture() {
        return currentArch;
    }

    @Override
    public long getStepCount() {
        return stepCount;
    }
}
