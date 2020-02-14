package org.graalvm.vm.x86.trcview.test.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.graalvm.vm.trcview.analysis.ComputedSymbol;
import org.graalvm.vm.trcview.analysis.ComputedSymbol.Type;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.expression.TypeParser;
import org.graalvm.vm.trcview.storage.SessionStorage;
import org.graalvm.vm.trcview.storage.sql.SQLSessionStorage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SessionStorageTest {
    private static final String DESTROY_URL = "jdbc:derby:memory:trcviewsession;drop=true";

    private SessionStorage storage;

    @Before
    public void setup() throws SQLException {
        storage = new SQLSessionStorage();
        storage.setTrace(UUID.randomUUID().toString());
    }

    @After
    public void teardown() throws IOException {
        storage.close();
        try {
            DriverManager.getConnection(DESTROY_URL);
        } catch (SQLException e) {
            // swallow; Derby throws an exception here
        }
    }

    @Test
    public void createComputedSymbol() {
        ComputedSymbol sym = storage.getComputedSymbol(0x1000);
        assertNull(sym);

        storage.addSubroutine(0x1000, "test", null);

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertNull(sym.prototype);

        Set<ComputedSymbol> subs = storage.getSubroutines();
        assertEquals(1, subs.size());
        ComputedSymbol sub = subs.iterator().next();
        assertEquals(0x1000, sub.address);
        assertEquals("test", sub.name);
        assertEquals(Type.SUBROUTINE, sub.type);
        assertNull(sub.prototype);

        Set<ComputedSymbol> locs = storage.getLocations();
        assertEquals(0, locs.size());
    }

    @Test
    public void overwriteComputedSymbol() {
        ComputedSymbol sym = storage.getComputedSymbol(0x1000);
        assertNull(sym);

        storage.addSubroutine(0x1000, "test", null);

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertNull(sym.prototype);

        storage.addSubroutine(0x1000, "test2", null);

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test2", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertNull(sym.prototype);

        Set<ComputedSymbol> subs = storage.getSubroutines();
        assertEquals(1, subs.size());
        ComputedSymbol sub = subs.iterator().next();
        assertEquals(0x1000, sub.address);
        assertEquals("test2", sub.name);
        assertEquals(Type.SUBROUTINE, sub.type);
        assertNull(sub.prototype);

        Set<ComputedSymbol> locs = storage.getLocations();
        assertEquals(0, locs.size());
    }

    @Test
    public void renameComputedSymbol() {
        ComputedSymbol sym = storage.getComputedSymbol(0x1000);
        assertNull(sym);

        storage.addSubroutine(0x1000, "test", null);

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertNull(sym.prototype);

        storage.renameSymbol(sym, "test2");

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test2", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertNull(sym.prototype);

        Set<ComputedSymbol> subs = storage.getSubroutines();
        assertEquals(1, subs.size());
        ComputedSymbol sub = subs.iterator().next();
        assertEquals(0x1000, sub.address);
        assertEquals("test2", sub.name);
        assertEquals(Type.SUBROUTINE, sub.type);
        assertNull(sub.prototype);

        Set<ComputedSymbol> locs = storage.getLocations();
        assertEquals(0, locs.size());
    }

    @Test
    public void setPrototype() throws ParseException {
        Prototype proto1 = new TypeParser("void f(int)").parse().getPrototype();
        Prototype proto2 = new TypeParser("long f(char)").parse().getPrototype();

        ComputedSymbol sym = storage.getComputedSymbol(0x1000);
        assertNull(sym);

        storage.addSubroutine(0x1000, "test", proto1);

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertEquals(proto1, sym.prototype);

        storage.setPrototype(sym, proto2);

        sym = storage.getComputedSymbol(0x1000);
        assertNotNull(sym);
        assertEquals(0x1000, sym.address);
        assertEquals("test", sym.name);
        assertEquals(Type.SUBROUTINE, sym.type);
        assertEquals(proto2, sym.prototype);
    }

    @Test
    public void getComputedSymbols() {
        ComputedSymbol sym = storage.getComputedSymbol(0x1000);
        assertNull(sym);

        storage.addSubroutine(0x1000, "test 1", null);
        storage.addSubroutine(0x1001, "test 2", null);
        storage.addSubroutine(0x1002, "test 3", null);

        Set<ComputedSymbol> subs = storage.getSubroutines();
        assertEquals(3, subs.size());
        Iterator<ComputedSymbol> it = subs.iterator();
        ComputedSymbol sub = it.next();
        assertEquals(0x1000, sub.address);
        assertEquals("test 1", sub.name);
        assertEquals(Type.SUBROUTINE, sub.type);
        assertNull(sub.prototype);

        sub = it.next();
        assertEquals(0x1001, sub.address);
        assertEquals("test 2", sub.name);
        assertEquals(Type.SUBROUTINE, sub.type);
        assertNull(sub.prototype);

        sub = it.next();
        assertEquals(0x1002, sub.address);
        assertEquals("test 3", sub.name);
        assertEquals(Type.SUBROUTINE, sub.type);
        assertNull(sub.prototype);

        Set<ComputedSymbol> locs = storage.getLocations();
        assertEquals(0, locs.size());
    }

    @Test
    public void getNoCommentPC() {
        String comment = storage.getCommentForPC(0x1000);
        assertNull(comment);
    }

    @Test
    public void setCommentPC() {
        assertNull(storage.getCommentForPC(0x1000));

        storage.setCommentForPC(0x1000, "Comment 1");
        storage.setCommentForPC(0x1002, "Comment 2");
        storage.setCommentForPC(0x1004, "Comment 3");

        assertEquals("Comment 1", storage.getCommentForPC(0x1000));
        assertEquals("Comment 2", storage.getCommentForPC(0x1002));
        assertEquals("Comment 3", storage.getCommentForPC(0x1004));
        assertNull(storage.getCommentForPC(0x1006));
    }

    @Test
    public void overwriteCommentPC() {
        assertNull(storage.getCommentForPC(0x1000));

        storage.setCommentForPC(0x1000, "Comment 1");
        storage.setCommentForPC(0x1000, "Comment 2");
        storage.setCommentForPC(0x1000, "Comment 3");

        assertEquals("Comment 3", storage.getCommentForPC(0x1000));
    }

    @Test
    public void deleteCommentPC() {
        assertNull(storage.getCommentForPC(0x1000));

        storage.setCommentForPC(0x1000, "Comment 1");

        assertEquals("Comment 1", storage.getCommentForPC(0x1000));

        storage.setCommentForPC(0x1000, null);

        assertNull(storage.getCommentForPC(0x1000));
    }
}
