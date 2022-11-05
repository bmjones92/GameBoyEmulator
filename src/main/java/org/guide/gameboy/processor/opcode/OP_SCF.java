package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "SCF" instruction. This instruction sets the carry flag.
 *
 * @author Brendan Jones
 */
public class OP_SCF extends Opcode {

    /**
     * Creates a new SCF instruction.
     */
    public OP_SCF() {
        super("SCF", 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
        flags.write(Flag.C, true);
    }

}
