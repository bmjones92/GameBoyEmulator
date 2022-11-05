package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.CPUStatusMode;
import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "STOP" instruction. This instruction halts the CPU and LCD until a button is pressed.
 *
 * @author Brendan Jones
 */
public class OP_STOP extends Opcode {

    /**
     * Creates a new STOP instruction.
     */
    public OP_STOP() {
        super("STOP", 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        cpu.setStatus(CPUStatusMode.STOPPED);
    }

}
