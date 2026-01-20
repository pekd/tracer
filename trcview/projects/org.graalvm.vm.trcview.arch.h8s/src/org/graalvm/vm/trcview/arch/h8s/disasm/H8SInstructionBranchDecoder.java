package org.graalvm.vm.trcview.arch.h8s.disasm;

import org.graalvm.vm.trcview.arch.BranchTarget;
import org.graalvm.vm.trcview.arch.CodeReader;

public class H8SInstructionBranchDecoder {
    public static BranchTarget getBranchTarget(CodeReader reader) {
        int op = Short.toUnsignedInt(reader.peekI16(0));

        switch (op >> 8) {
            case 0x40: /* BRA d:8 */
            case 0x41: /* BRN d:8 */
            case 0x42: /* BHI d:8 */
            case 0x43: /* BLS d:8 */
            case 0x44: /* BCC d:8 */
            case 0x45: /* BCS d:8 */
            case 0x46: /* BNE d:8 */
            case 0x47: /* BEQ d:8 */
            case 0x48: /* BVC d:8 */
            case 0x49: /* BVS d:8 */
            case 0x4A: /* BPL d:8 */
            case 0x4B: /* BMI d:8 */
            case 0x4C: /* BGE d:8 */
            case 0x4D: /* BLT d:8 */
            case 0x4E: /* BGT d:8 */
            case 0x4F: /* BLE d:8 */
                return new BranchTarget(reader.getPC() + 2 + (byte) op);
            case 0x55: /* BSR d:8 */
                return new BranchTarget(reader.getPC() + 2 + (byte) op);
            case 0x58: {
                short disp = reader.peekI16(2);
                switch (op & 0xFF) {
                    case 0x00: /* BRA d:16 */
                    case 0x10: /* BRN d:16 */
                    case 0x20: /* BHI d:16 */
                    case 0x30: /* BLS d:16 */
                    case 0x40: /* BCC d:16 */
                    case 0x50: /* BCS d:16 */
                    case 0x60: /* BNE d:16 */
                    case 0x70: /* BEQ d:16 */
                    case 0x80: /* BVC d:16 */
                    case 0x90: /* BVS d:16 */
                    case 0xA0: /* BPL d:16 */
                    case 0xB0: /* BMI d:16 */
                    case 0xC0: /* BGE d:16 */
                    case 0xD0: /* BLT d:16 */
                    case 0xE0: /* BGT d:16 */
                    case 0xF0: /* BLE d:16 */
                        return new BranchTarget(reader.getPC() + 4 + disp);
                    default:
                        return null;
                }
            }
            case 0x5A: { /* JMP @aa:24 */
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                long abs = (((long) op & 0xFF) << 16) | op2;
                return new BranchTarget(abs);
            }
            case 0x5C:
                switch (op & 0xFF) {
                    case 0x00: { /* BSR d:16 */
                        short disp = reader.peekI16(2);
                        return new BranchTarget(reader.getPC() + disp + 4);
                    }
                    default:
                        return null;
                }
            case 0x5E: { /* JSR @aa:24 */
                int op2 = Short.toUnsignedInt(reader.peekI16(2));
                long abs = (((long) op & 0xFF) << 16) | op2;
                return new BranchTarget(abs);
            }
            default:
                return null;
        }
    }
}
