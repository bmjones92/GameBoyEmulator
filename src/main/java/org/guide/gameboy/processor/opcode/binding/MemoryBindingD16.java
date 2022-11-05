package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * This binding allows for an unsigned 16-bit value to be read from memory. The target address is calculated from the
 * program counter's current location.
 * <p>
 * Note that each successive read or write using this binding advances the program counter by two bytes.
 *
 * @author Brendan Jones
 */
public class MemoryBindingD16 extends MemoryBinding {

    public MemoryBindingD16(Memory memory, PointerRegister pc) {
        super(memory, pc);
    }

    @Override
    public int read() {
        final var address = pc.readAndMove(2);
        return memory.readUnsignedShort(address);
    }

    @Override
    public String toString() {
        return "d16";
    }

}
