package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "NOP" instruction. This instruction does nothing.
 */
public class OP_NOP extends Opcode {

    /**
     * Creates a new NOP instruction.
     */
    public OP_NOP() {
        super("NOP", 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        // Nothing to do here.
    }

}
