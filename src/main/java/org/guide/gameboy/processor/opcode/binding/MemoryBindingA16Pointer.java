package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * This binding allows for a 16-bit value to be written to memory. The target address is calculated by reading a 16-bit
 * address from memory at the program counter's current location. This binding cannot be read from and a value of
 * {@code 0} will always be read.
 * <p>
 * Note that each successive write using this binding advances the program counter by two bytes.
 *
 * @author Brendan Jones
 */
public class MemoryBindingA16Pointer extends MemoryBinding implements WriteBinding {

    public MemoryBindingA16Pointer(Memory memory, PointerRegister pc) {
        super(memory, pc);
    }

    @Override
    public int read() {
        return 0;
    }

    @Override
    public void write(int value) {
        final var address = readAddress();
        memory.writeShort(address, value);
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
