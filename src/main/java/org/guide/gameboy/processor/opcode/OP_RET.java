package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "RET" and "RETI" instructions. This instruction exits the current function and returns execution to
 * the caller and optionally enables interrupts.
 */
public class OP_RET extends JumpOpcode {

    /**
     * Whether to enable interrupts.
     */
    private boolean enableInterrupts;

    /**
     * Creates a new RET instruction with the format {@code RET flag}.
     *
     * @param flag  The conditional flag to test when deciding to return, or null to return unconditionally.
     * @param value Whether the conditional flag should be set or not.
     */
    public OP_RET(Flag flag, boolean value) {
        super("RET " + (flag == null ? "" : flag.toString(value)), flag, value, 20, 8);
    }

    /**
     * Creates a new RET or RETI instruction.
     *
     * @param enableInterrupts Whether the instruction should enable interrupts.
     */
    public OP_RET(boolean enableInterrupts) {
        super(enableInterrupts ? "RETI" : "RET", null, false, 16, 16);
        this.enableInterrupts = enableInterrupts;
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        if (checkJumped(flags)) {
            cpu.getPC().write(cpu.popFromStack());
            if (enableInterrupts) {
                cpu.getInterrupts().setMasterEnable(true, 0);
            }
        }
    }

}
