package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * This binding allows for an unsigned 8-bit value to be read from and written to memory. The target address is
 * calculated by reading a 16-bit address from memory at the program counter's current location.
 * <p>
 * Note that each successive read or write using this binding advances the program counter by two bytes.
 *
 * @author Brendan Jones
 */
public class MemoryBindingA16Address extends MemoryBinding implements WriteBinding {

    /**
     * Creates a new memory binding.
     *
     * @param memory The memory component.
     * @param pc     The program counter.
     */
    public MemoryBindingA16Address(Memory memory, PointerRegister pc) {
        super(memory, pc);
    }

    @Override
    public int read() {
        final var address = readAddress();
        return memory.readUnsigned(address);
    }

    @Override
    public void write(int value) {
        final var address = readAddress();
        memory.write(address, value);
    }

    @Override
    public String toString() {
        return "(a16)";
    }

    /**
     * Calculate the target address.
     *
     * @return The target address.
     */
    private int readAddress() {
        final var address = pc.readAndMove(2);
        return memory.readUnsignedShort(address);
    }

}
