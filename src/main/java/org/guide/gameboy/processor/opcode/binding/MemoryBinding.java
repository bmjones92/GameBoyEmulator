package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * A memory binding is a readable binding that reads data from memory.
 *
 * @author Brendan Jones
 */
public abstract class MemoryBinding implements ReadBinding {

    /**
     * The system's Memory component.
     */
    protected final Memory memory;

    /**
     * The program counter.
     */
    protected final PointerRegister pc;

    /**
     * Creates a new memory binding.
     *
     * @param memory The memory component to bind to.
     * @param pc     The program counter.
     */
    public MemoryBinding(Memory memory, PointerRegister pc) {
        this.memory = memory;
        this.pc = pc;
    }

}
