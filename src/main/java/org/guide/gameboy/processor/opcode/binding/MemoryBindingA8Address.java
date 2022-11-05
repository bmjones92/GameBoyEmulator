package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * This binding allows for an unsigned 8-bit value to be read from and written to memory. The target address is
 * calculated by reading an 8-bit address from memory at the program counter's current location and adding it to
 * {@code $FF00}. The range of possible memory addresses corresponds to the HRAM (High RAM) section of the memory map.
 * <p>
 * Note that each successive read or write using this binding advances the program counter by one byte.
 *
 * @author Brendan Jones
 */
public class MemoryBindingA8Address extends MemoryBinding implements WriteBinding {

    /**
     * The base address for reads using this binding.
     */
    private static final int BASE_ADDRESS = 0xFF00;

    /**
     * Creates a new memory binding.
     *
     * @param memory The memory component.
     * @param pc     The program counter.
     */
    public MemoryBindingA8Address(Memory memory, PointerRegister pc) {
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

    /**
     * Reads the target address from memory.
     *
     * @return The target address.
     */
    private int readAddress() {
        final var address = pc.readAndMove(1);
        return BASE_ADDRESS + memory.readUnsigned(address);
    }

    @Override
    public String toString() {
        return "(a8)";
    }

}
