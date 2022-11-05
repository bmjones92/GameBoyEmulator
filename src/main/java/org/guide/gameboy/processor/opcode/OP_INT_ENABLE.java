package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "DI" and "EI" instructions. These instructions disable and enable interrupts.
 */
public class OP_INT_ENABLE extends Opcode {

    /**
     * Whether to enable interrupts.
     */
    private final boolean enabled;

    /**
     * Creates a new "DI" or "EI" instruction.
     *
     * @param enabled Whether the instruction should enable or disable interrupts.
     */
    public OP_INT_ENABLE(boolean enabled) {
        super(enabled ? "EI" : "DI", 4);
        this.enabled = enabled;
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        cpu.getInterrupts().setMasterEnable(enabled, enabled ? 4 : 0);
    }

}
