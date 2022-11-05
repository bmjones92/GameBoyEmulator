package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.register.Register;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;

import java.util.ArrayList;
import java.util.List;

import static org.guide.gameboy.processor.opcode.OP_ROTATE.Direction;

/**
 * Responsible for mapping opcodes to the corresponding instructions for the Game Boy's instruction set.
 *
 * @author Brendan Jones
 */
public class OpcodeTable {

    /**
     * The list of instructions.
     */
    private final List<Opcode> opcodes = new ArrayList<>(0x200);

    /**
     * Creates a new opcode table.
     *
     * @param cpu The processor to bind instructions to.
     * @param mmu The memory to bind instructions to.
     */
    public OpcodeTable(Processor cpu, Memory mmu) {
        final var memory = new MemoryBindings(mmu, cpu.getPC());

        // Block: 0x000 -> 0x00F
        opcodes.add(new OP_NOP()); // 0x00
        opcodes.add(new OP_LD(cpu.getBC(), memory, false)); // 0x01
        opcodes.add(new OP_LD(cpu.getBC(), cpu.getA())); // 0x02
        opcodes.add(new OP_INC(cpu.getBC(), false)); // 0x03
        opcodes.add(new OP_INC(cpu.getB())); // 0x04
        opcodes.add(new OP_DEC(cpu.getB())); // 0x05
        opcodes.add(new OP_LD(cpu.getB(), memory, false)); // 0x06
        opcodes.add(new OP_ROTATE(cpu.getA(), Direction.LEFT, true, true)); // 0x07
        opcodes.add(new OP_LD(memory, cpu.getSP())); // 0x08
        opcodes.add(new OP_ADD(cpu.getHL(), cpu.getBC())); // 0x09
        opcodes.add(new OP_LD(cpu.getA(), cpu.getBC())); // 0x0A
        opcodes.add(new OP_DEC(cpu.getBC(), false)); // 0x0B
        opcodes.add(new OP_INC(cpu.getC())); // 0x0C
        opcodes.add(new OP_DEC(cpu.getC())); // 0x0D
        opcodes.add(new OP_LD(cpu.getC(), memory, false)); // 0x0E
        opcodes.add(new OP_ROTATE(cpu.getA(), Direction.RIGHT, true, true)); // 0x0F

        // Block: 0x010 -> 0x01F
        opcodes.add(new OP_STOP()); // 0x10
        opcodes.add(new OP_LD(cpu.getDE(), memory, false)); // 0x11
        opcodes.add(new OP_LD(cpu.getDE(), cpu.getA())); // 0x12
        opcodes.add(new OP_INC(cpu.getDE(), false)); // 0x13
        opcodes.add(new OP_INC(cpu.getD())); // 0x14
        opcodes.add(new OP_DEC(cpu.getD())); // 0x15
        opcodes.add(new OP_LD(cpu.getD(), memory, false)); // 0x16
        opcodes.add(new OP_ROTATE(cpu.getA(), Direction.LEFT, false, true)); // 0x17
        opcodes.add(new OP_JR(memory)); // 0x18
        opcodes.add(new OP_ADD(cpu.getHL(), cpu.getDE())); // 0x19
        opcodes.add(new OP_LD(cpu.getA(), cpu.getDE())); // 0x1A
        opcodes.add(new OP_DEC(cpu.getDE(), false)); // 0x1B
        opcodes.add(new OP_INC(cpu.getE())); // 0x1C
        opcodes.add(new OP_DEC(cpu.getE())); // 0x1D
        opcodes.add(new OP_LD(cpu.getE(), memory, false)); // 0x1E
        opcodes.add(new OP_ROTATE(cpu.getA(), Direction.RIGHT, false, true)); // 0x1F

        // Block: 0x020 -> 0x02F
        opcodes.add(new OP_JR(Flag.Z, false, memory)); // 0x20
        opcodes.add(new OP_LD(cpu.getHL(), memory, false)); // 0x21
        opcodes.add(new OP_LDI(cpu.getHL(), cpu.getA())); // 0x22
        opcodes.add(new OP_INC(cpu.getHL(), false)); // 0x23
        opcodes.add(new OP_INC(cpu.getH())); // 0x24
        opcodes.add(new OP_DEC(cpu.getH())); // 0x25
        opcodes.add(new OP_LD(cpu.getH(), memory, false)); // 0x26
        opcodes.add(new OP_DA(cpu.getA())); // 0x27
        opcodes.add(new OP_JR(Flag.Z, true, memory)); // 0x28
        opcodes.add(new OP_ADD(cpu.getHL(), cpu.getHL())); // 0x29
        opcodes.add(new OP_LDI(cpu.getA(), cpu.getHL())); // 0x2A
        opcodes.add(new OP_DEC(cpu.getHL(), false)); // 0x2B
        opcodes.add(new OP_INC(cpu.getL())); // 0x2C
        opcodes.add(new OP_DEC(cpu.getL())); // 0x2D
        opcodes.add(new OP_LD(cpu.getL(), memory, false)); // 0x2E
        opcodes.add(new OP_CPL(cpu.getA())); // 0x2F

        // Block 0x030 -> 0x03F
        opcodes.add(new OP_JR(Flag.C, false, memory)); // 0x30
        opcodes.add(new OP_LD(cpu.getSP(), memory)); // 0x31
        opcodes.add(new OP_LDD(cpu.getHL(), cpu.getA())); // 0x32
        opcodes.add(new OP_INC(cpu.getSP())); // 0x33
        opcodes.add(new OP_INC(cpu.getHL(), true)); // 0x34
        opcodes.add(new OP_DEC(cpu.getHL(), true)); // 0x35
        opcodes.add(new OP_LD(cpu.getHL(), memory, true)); // 0x36
        opcodes.add(new OP_SCF()); // 0x37
        opcodes.add(new OP_JR(Flag.C, true, memory)); // 0x38
        opcodes.add(new OP_ADD(cpu.getHL(), cpu.getSP())); // 0x39
        opcodes.add(new OP_LDD(cpu.getA(), cpu.getHL())); // 0x3A
        opcodes.add(new OP_DEC(cpu.getSP())); // 0x3B
        opcodes.add(new OP_INC(cpu.getA())); // 0x3C
        opcodes.add(new OP_DEC(cpu.getA())); // 0x3D
        opcodes.add(new OP_LD(cpu.getA(), memory, false)); // 0x3E
        opcodes.add(new OP_CCF()); // 0x3F

        // The list of registers.
        Register[] registers = new Register[]{cpu.getB(), cpu.getC(), cpu.getD(), cpu.getE(), cpu.getH(), cpu.getL(),
                cpu.getHL(), cpu.getA()};

        // Initialize LD instruction block (0x040 -> 0x07F).
        for (Register target : registers) {
            if (target instanceof Register16) {
                for (Register source : registers) {
                    if (source instanceof Register16) {
                        opcodes.add(new OP_HALT());
                    } else {
                        opcodes.add(new OP_LD((Register16) target, (Register8) source));
                    }
                }
            } else {
                for (Register source : registers) {
                    if (source instanceof Register16) {
                        opcodes.add(new OP_LD((Register8) target, (Register16) source));
                    } else {
                        opcodes.add(new OP_LD((Register8) target, false, (Register8) source, false));
                    }
                }
            }
        }

        // Initialize ADD instruction block (0x080 -> 0x087).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_ADD(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_ADD(cpu.getA(), (Register8) source));
            }
        }

        // Initialize ADC instruction block (0x088 -> 0x08F).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_ADC(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_ADC(cpu.getA(), (Register8) source));
            }
        }

        // Initialize SUB instruction block without carry flag (0x090 -> 0x097).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_SUB(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_SUB(cpu.getA(), (Register8) source));
            }
        }

        // Initialize SUB instruction block with carry flag (0x098 -> 0x09F).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_SBC(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_SBC(cpu.getA(), (Register8) source));
            }
        }

        // Initialize AND instruction block (0x0A0 -> 0x0A7).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_AND(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_AND(cpu.getA(), (Register8) source));
            }
        }

        // Initialize XOR instruction block (0x0A8 -> 0x0AF).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_XOR(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_XOR(cpu.getA(), (Register8) source));
            }
        }

        // Initialize OR instruction block (0x0B0 -> 0x0B7).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_OR(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_OR(cpu.getA(), (Register8) source));
            }
        }

        // Initialize CP instruction block (0x0B8 -> 0x0BF).
        for (Register source : registers) {
            if (source instanceof Register16) {
                opcodes.add(new OP_CP(cpu.getA(), (Register16) source));
            } else {
                opcodes.add(new OP_CP(cpu.getA(), (Register8) source));
            }
        }

        // Block: 0x0C0 -> 0x0CF
        opcodes.add(new OP_RET(Flag.Z, false)); // 0xC0
        opcodes.add(new OP_POP(cpu.getBC())); // 0xC1
        opcodes.add(new OP_JP(Flag.Z, false, memory)); // 0xC2
        opcodes.add(new OP_JP(memory)); // 0xC3
        opcodes.add(new OP_CALL(Flag.Z, false, memory)); // 0xC4
        opcodes.add(new OP_PUSH(cpu.getBC())); // 0xC5
        opcodes.add(new OP_ADD(cpu.getA(), memory)); // 0xC6
        opcodes.add(new OP_RST(0x00)); // 0xC7
        opcodes.add(new OP_RET(Flag.Z, true)); // 0xC8
        opcodes.add(new OP_RET(false)); // 0xC9
        opcodes.add(new OP_JP(Flag.Z, true, memory)); // 0xCA
        opcodes.add(null); // 0xCB
        opcodes.add(new OP_CALL(Flag.Z, true, memory)); // 0xCC
        opcodes.add(new OP_CALL(memory)); // 0xCD
        opcodes.add(new OP_ADC(cpu.getA(), memory)); // 0xCE
        opcodes.add(new OP_RST(0x08)); // 0xCF

        // Block: 0x0D0 -> 0x0DF
        opcodes.add(new OP_RET(Flag.C, false)); // 0xD0
        opcodes.add(new OP_POP(cpu.getDE())); // 0xD1
        opcodes.add(new OP_JP(Flag.C, false, memory)); // 0xD2
        opcodes.add(null); // 0xD3
        opcodes.add(new OP_CALL(Flag.C, false, memory)); // 0xD4
        opcodes.add(new OP_PUSH(cpu.getDE())); // 0xD5
        opcodes.add(new OP_SUB(cpu.getA(), memory)); // 0xD6
        opcodes.add(new OP_RST(0x10)); // 0xD7
        opcodes.add(new OP_RET(Flag.C, true)); // 0xD8
        opcodes.add(new OP_RET(true)); // 0xD9
        opcodes.add(new OP_JP(Flag.C, true, memory)); // 0xDA
        opcodes.add(null); // 0xDB
        opcodes.add(new OP_CALL(Flag.C, true, memory)); // 0xDC
        opcodes.add(null); // 0xDD
        opcodes.add(new OP_SBC(cpu.getA(), memory)); // 0xDE
        opcodes.add(new OP_RST(0x18)); // 0xDF

        // Block: 0x0E0 -> 0x0EF
        opcodes.add(new OP_LD(memory, cpu.getA(), false)); // 0xE0
        opcodes.add(new OP_POP(cpu.getHL())); // 0xE1
        opcodes.add(new OP_LD(cpu.getC(), true, cpu.getA(), false)); // 0xE2
        opcodes.add(null); // 0xE3
        opcodes.add(null); // 0xE4
        opcodes.add(new OP_PUSH(cpu.getHL())); // 0xE5
        opcodes.add(new OP_AND(cpu.getA(), memory)); // 0xE6
        opcodes.add(new OP_RST(0x20)); // 0xE7
        opcodes.add(new OP_ADD(cpu.getSP(), memory)); // 0xE8
        opcodes.add(new OP_JP(cpu.getHL())); // 0xE9
        opcodes.add(new OP_LD(memory, cpu.getA(), true)); // 0xEA
        opcodes.add(null); // 0xEB
        opcodes.add(null); // 0xEC
        opcodes.add(null); // 0xED
        opcodes.add(new OP_XOR(cpu.getA(), memory)); // 0xEE
        opcodes.add(new OP_RST(0x28)); // 0xEF

        // Block: 0x0F0 -> 0x0FF
        opcodes.add(new OP_LD(cpu.getA(), memory, true)); // 0xF0
        opcodes.add(new OP_POP(cpu.getAF())); // 0xF1
        opcodes.add(new OP_LD(cpu.getA(), false, cpu.getC(), true)); // 0xF2
        opcodes.add(new OP_INT_ENABLE(false)); // 0xF3
        opcodes.add(null); // 0xF4
        opcodes.add(new OP_PUSH(cpu.getAF())); // 0xF5
        opcodes.add(new OP_OR(cpu.getA(), memory)); // 0xF6
        opcodes.add(new OP_RST(0x30)); // 0xF7
        opcodes.add(new OP_LD(cpu.getHL(), cpu.getSP(), memory)); // 0xF8
        opcodes.add(new OP_LD(cpu.getSP(), cpu.getHL())); // 0xF9
        opcodes.add(new OP_LD(cpu.getA(), memory)); // 0xFA
        opcodes.add(new OP_INT_ENABLE(true)); // 0xFB
        opcodes.add(null); // 0xFC
        opcodes.add(null); // 0xFD
        opcodes.add(new OP_CP(cpu.getA(), memory)); // 0xFE
        opcodes.add(new OP_RST(0x38)); // 0xFF

        // Initialize RLC instruction block (0x100 -> 0x107).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_ROTATE((Register16) register, Direction.LEFT, true));
            } else {
                opcodes.add(new OP_ROTATE((Register8) register, Direction.LEFT, true, false));
            }
        }

        // Initialize RRC instruction block (0x108 -> 0x10F).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_ROTATE((Register16) register, Direction.RIGHT, true));
            } else {
                opcodes.add(new OP_ROTATE((Register8) register, Direction.RIGHT, true, false));
            }
        }

        // Initialize RL instruction block (0x110 -> 0x117).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_ROTATE((Register16) register, Direction.LEFT, false));
            } else {
                opcodes.add(new OP_ROTATE((Register8) register, Direction.LEFT, false, false));
            }
        }

        // Initialize RR instruction block (0x118 -> 0x11F).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_ROTATE((Register16) register, Direction.RIGHT, false));
            } else {
                opcodes.add(new OP_ROTATE((Register8) register, Direction.RIGHT, false, false));
            }
        }

        // Initialize SLA instruction block (0x120 -> 0x127).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_SLA((Register16) register));

            } else {
                opcodes.add(new OP_SLA((Register8) register));
            }
        }

        // Initialize SRA instruction block (0x128 -> 0x12F).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_SRA((Register16) register));
            } else {
                opcodes.add(new OP_SRA((Register8) register));
            }
        }

        // Initialize SWAP instruction block (0x130 -> 0x137).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_SWAP((Register16) register));
            } else {
                opcodes.add(new OP_SWAP((Register8) register));
            }
        }

        // Initialize SRL instruction block (0x138 -> 0x13F).
        for (var register : registers) {
            if (register instanceof Register16) {
                opcodes.add(new OP_SRL((Register16) register));
            } else {
                opcodes.add(new OP_SRL((Register8) register));
            }
        }

        // Initialize BIT instruction block (0x140 -> 0x17F).
        for (var bit = 0; bit < 8; ++bit) {
            for (var register : registers) {
                if (register instanceof Register16) {
                    opcodes.add(new OP_BIT(bit, (Register16) register));
                } else {
                    opcodes.add(new OP_BIT(bit, (Register8) register));
                }
            }
        }

        // Initialize RES instruction block (0x180 -> 0x1BF).
        for (var bit = 0; bit < 8; ++bit) {
            for (var register : registers) {
                if (register instanceof Register16) {
                    opcodes.add(new OP_SET(bit, (Register16) register, false));
                } else {
                    opcodes.add(new OP_SET(bit, (Register8) register, false));
                }
            }
        }

        // Initialize SET instruction block (0x1C0 -> 0x1FF).
        for (var bit = 0; bit < 8; ++bit) {
            for (var register : registers) {
                if (register instanceof Register16) {
                    opcodes.add(new OP_SET(bit, (Register16) register, true));
                } else {
                    opcodes.add(new OP_SET(bit, (Register8) register, true));
                }
            }
        }
    }

    public Opcode get(int opcode) {
        return opcodes.get(opcode);
    }

}
