package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "CCF" instruction.
 *
 * @author Brendan Jones
 */
public class OP_CCF extends Opcode {

    /**
     * Creates a new CCF instruction.
     */
    public OP_CCF() {
        super("CCF", 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
        flags.write(Flag.C, !flags.read(Flag.C));
    }

}
