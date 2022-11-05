package org.guide.gameboy.processor.register;

import org.guide.gameboy.processor.interrupts.memory.Memory;

/**
 * Implementation for an 8-bit register.
 *
 * @author Brendan Jones
 */
public class Register8 extends Register {

    /**
     * The value currently stored in the register.
     */
    private byte value;

    /**
     * The mask of usable bits for this register.
     */
    private final int mask;

    /**
     * Creates a new Register8 instance.
     *
     * @param name   The human-readable name of this register.
     * @param memory The memory component to create bindings for.
     * @param mask   The mask of usable bits.
     */
    public Register8(char name, Memory memory, int mask) {
        super(String.valueOf(name), memory, 0xFF00);
        this.mask = mask;
    }

    /**
     * Creates a new Register8 instance.
     *
     * @param name   The human-readable name of this register.
     * @param memory The memory component to create bindings for.
     */
    public Register8(char name, Memory memory) {
        this(name, memory, 0xFF);
    }

    @Override
    public void write(int value) {
        this.value = (byte) (value & mask);
    }

    @Override
    public int read() {
        return value & mask;
    }

}
