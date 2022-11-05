package org.guide.gameboy.processor.register;

import org.guide.gameboy.processor.interrupts.memory.Memory;

/**
 * Implementation for a 16-bit register.
 *
 * @author Brendan Jones
 */
public class Register16 extends Register {

    /**
     * The 8-bit register representing the high byte.
     */
    private final Register8 high;

    /**
     * The 8-bit register representing the low byte.
     */
    private final Register8 low;

    /**
     * Creates a new 16-bit register.
     *
     * @param name   The human-readable name of the register.
     * @param memory The memory component to create bindings for.
     */
    public Register16(String name, Memory memory) {
        super(name, memory, 0x0000);

        this.high = new Register8(name.charAt(0), memory);
        this.low = new Register8(name.charAt(1), memory);
    }

    /**
     * Creates a new 16-bit register.
     *
     * @param name     The human-readable name of the register.
     * @param memory   The memory component to create bindings for.
     * @param highMask The mask for the high-byte register.
     * @param lowMask  The mask for the low-byte register.
     */
    public Register16(String name, Memory memory, int highMask, int lowMask) {
        super(name, memory, 0x0000);

        this.high = new Register8(name.charAt(0), memory, highMask);
        this.low = new Register8(name.charAt(1), memory, lowMask);
    }

    @Override
    public void write(int value) {
        high.write((value & 0xFF00) >> 8);
        low.write(value & 0xFF);
    }

    @Override
    public int read() {
        return high.read() << 8 | low.read();
    }

    /**
     * Gets the 8-bit register representing the high bits.
     *
     * @return The high register.
     */
    public Register8 high() {
        return high;
    }

    /**
     * Gets the 8-bit register containing the low bits.
     *
     * @return The low register.
     */
    public Register8 low() {
        return low;
    }

}
