package org.graalvm.vm.trcview.arch.h8s.disasm;

import org.graalvm.vm.trcview.arch.CodeReader;
import org.graalvm.vm.trcview.arch.io.InstructionType;

public class H8SInstructionTypeDecoder {
    public static InstructionType getType(CodeReader reader) {
        int op = Short.toUnsignedInt(reader.peekI16(0));

        switch (op >> 8) {
            case 0x40: /* BRA d:8 */
                return InstructionType.JMP;
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
                return InstructionType.JCC;
            case 0x54:
                switch (op & 0xFF) {
                    case 0x70: /* RTS */
                        return InstructionType.RET;
                    default:
                        return InstructionType.OTHER;
                }
            case 0x55: /* BSR d:8 */
                return InstructionType.CALL;
            case 0x56:
                switch (op & 0xFF) {
                    case 0x70: /* RTE */
                        return InstructionType.RTI;
                    default:
                        return InstructionType.OTHER;
                }
            case 0x57:
                if ((op & 0xCF) != 0) {
                    return InstructionType.OTHER;
                } else {
                    /* TRAPA #x:2 */
                    return InstructionType.SYSCALL;
                }
            case 0x58:
                switch (op & 0xFF) {
                    case 0x00: /* BRA d:16 */
                        return InstructionType.JMP;
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
                        return InstructionType.JCC;
                    default:
                        return InstructionType.OTHER;
                }
            case 0x59:
                switch (op & 0x8F) {
                    case 0x00: /* JMP @ERn */
                        return InstructionType.JMP_INDIRECT;
                    default:
                        return InstructionType.OTHER;
                }
            case 0x5A: /* JMP @aa:24 */
                return InstructionType.JMP;
            case 0x5C:
                switch (op & 0xFF) {
                    case 0x00: /* BSR d:16 */
                        return InstructionType.CALL;
                    default:
                        return InstructionType.OTHER;
                }
            case 0x5D:
                switch (op & 0x8F) {
                    case 0x00: /* JSR @ERn */
                        return InstructionType.CALL;
                    default:
                        return InstructionType.OTHER;
                }
            case 0x5E: /* JSR @aa:24 */
                return InstructionType.CALL;
            default:
                return InstructionType.OTHER;
        }
    }
}
