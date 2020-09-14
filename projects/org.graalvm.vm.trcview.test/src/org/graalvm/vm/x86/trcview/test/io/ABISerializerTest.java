package org.graalvm.vm.x86.trcview.test.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.decode.GenericABI;
import org.graalvm.vm.trcview.expression.ast.CallNode;
import org.graalvm.vm.trcview.expression.ast.ValueNode;
import org.graalvm.vm.trcview.expression.ast.VariableNode;
import org.graalvm.vm.trcview.expression.ast.XorNode;
import org.graalvm.vm.trcview.io.ABISerializer;
import org.junit.Test;

public class ABISerializerTest {
    @Test
    public void serializeNull() {
        GenericABI abi = new GenericABI();
        assertEquals("\"NULL;NULL\";\"NULL;NULL\";NULL", ABISerializer.store(abi));
    }

    @Test
    public void serialize() {
        GenericABI abi = new GenericABI();
        abi.getCall().setArguments(Arrays.asList(new VariableNode("r0"), new VariableNode("r1"), new VariableNode("r2"), new VariableNode("r3")));
        abi.getCall().setReturn(new VariableNode("r0"));
        abi.setSyscallId(new XorNode(new CallNode("getU16", Arrays.asList(new VariableNode("pc"))), new ValueNode(0104000)));
        abi.getSyscall().setArguments(Arrays.asList(new VariableNode("r0"), new VariableNode("r1"), new VariableNode("r2"), new VariableNode("r3")));
        abi.getSyscall().setReturn(new VariableNode("r0"));
        abi.addSyscall(0, new Function("test", new Prototype()));

        String def = "\"\\\"r0\\\";NULL;\\\"r0\\\";\\\"r1\\\";\\\"r2\\\";\\\"r3\\\"\";\"\\\"r0\\\";NULL;\\\"r0\\\";\\\"r1\\\";\\\"r2\\\";\\\"r3\\\"\";\"(getU16(pc) ^ 34816)\";\"0=void test()\"";
        assertEquals(def, ABISerializer.store(abi));
    }

    @Test
    public void deserializeNull() throws Exception {
        GenericABI abi = new GenericABI();
        ABISerializer.load(abi, "\"NULL;NULL\";\"NULL;NULL\";NULL");
        assertNull(abi.getSyscallId());
        assertTrue(abi.getSyscalls().isEmpty());

        assertNull(abi.getCall().getReturn());
        assertEquals(0, abi.getCall().getFixedArgumentCount());
        assertTrue(abi.getCall().getArguments().isEmpty());

        assertNull(abi.getSyscall().getReturn());
        assertEquals(0, abi.getSyscall().getFixedArgumentCount());
        assertTrue(abi.getSyscall().getArguments().isEmpty());
    }

    @Test
    public void deserialize() throws Exception {
        String def = "\"\\\"r0\\\";NULL;\\\"r0\\\";\\\"r1\\\";\\\"r2\\\";\\\"r3\\\"\";\"\\\"r0\\\";NULL;\\\"r0\\\";\\\"r1\\\";\\\"r2\\\";\\\"r3\\\"\";\"(getU16(pc) ^ 34816)\";\"0=void test()\"";
        GenericABI abi = new GenericABI();
        ABISerializer.load(abi, def);

        assertNotNull(abi.getSyscallId());
        assertEquals(1, abi.getSyscalls().size());
        assertNotNull(abi.getSyscall(0));
        assertEquals("test", abi.getSyscall(0).getName());

        assertNotNull(abi.getCall().getReturn());
        assertTrue(abi.getCall().getReturn() instanceof VariableNode);
        assertEquals(4, abi.getCall().getFixedArgumentCount());
        assertEquals(4, abi.getCall().getArguments().size());

        assertNotNull(abi.getSyscall().getReturn());
        assertTrue(abi.getSyscall().getReturn() instanceof VariableNode);
        assertEquals(4, abi.getSyscall().getFixedArgumentCount());
        assertEquals(4, abi.getSyscall().getArguments().size());
    }
}
