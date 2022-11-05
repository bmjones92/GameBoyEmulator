package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation for all instructions.
 *
 * @author Brendan Jones
 */
public abstract class Opcode {

    /**
     * The human-readable representation of the instruction.
     */
    private final String instruction;

    /**
     * The number of CPU cycles this instruction takes to complete.
     */
    private final int cycles;

    /**
     * Creates a new Opcode.
     *
     * @param instruction The human-readable representation of the instruction.
     */
    public Opcode(String instruction, int cycles) {
        this.instruction = requireNonNull(instruction, "Instruction cannot be null.");
        this.cycles = cycles;
    }

    /**
     * Executes this instruction once.
     *
     * @param cpu    The system's processor.
     * @param memory The system's memory component.
     * @param flags  The system's flag register.
     */
    public abstract void execute(Processor cpu, Memory memory, FlagRegister flags);

    /**
     * Gets the number of cycles this instruction takes to execute.
     *
     * @return The number of cycles.
     */
    public int getExecutionCycles() {
        return cycles;
    }

    @Override
    public String toString() {
        return instruction;
    }

}
