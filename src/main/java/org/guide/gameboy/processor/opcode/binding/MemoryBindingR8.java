package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * This binding allows for a signed 8-bit value to be read from memory. The target address is calculated from the
 * program counter's current position.
 * <p>
 * Note that each successive read or write using this binding advances the program counter by one byte.
 *
 * @author Brendan Jones
 */
public class MemoryBindingR8 extends MemoryBinding {

    public MemoryBindingR8(Memory memory, PointerRegister pc) {
        super(memory, pc);
    }

    @Override
    public int read() {
        final var address = pc.readAndMove(1);
        return memory.read(address);
    }

    @Override
    public String toString() {
        return "r8";
    }

}
