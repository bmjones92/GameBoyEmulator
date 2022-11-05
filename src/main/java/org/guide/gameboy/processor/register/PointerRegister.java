package org.guide.gameboy.processor.register;

import org.guide.gameboy.processor.interrupts.memory.Memory;

/**
 * A pointer register is a type of register that contains a 16-bit address.
 *
 * @author Brendan Jones
 */
public class PointerRegister extends Register {

    /**
     * The current value stored in the register.
     */
    private short value;

    /**
     * Creates a new pointer register.
     *
     * @param name   The register name.
     * @param memory The memory component.
     */
    public PointerRegister(String name, Memory memory) {
        super(name, memory, 0x0000);
    }

    @Override
    public void write(int value) {
        this.value = (short) (value & 0xFFFF);
    }

    @Override
    public int read() {
        return value & 0xFFFF;
    }

    /**
     * Advances the register and then reads the register value.
     *
     * @param amount The amount to advance the register.
     * @return The result after the register has advanced.
     */
    public int moveAndRead(int amount) {
        write(value + amount);
        return read();
    }

    /**
     * Reads the register value and then advances the register.
     *
     * @param amount The amount to advance the register.
     * @return The result before the register has advanced.
     */
    public int readAndMove(int amount) {
        final var position = read();
        write(value + amount);
        return position;
    }

}
