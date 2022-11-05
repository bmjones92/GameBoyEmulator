package org.guide.gameboy.processor.opcode.binding;


import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.Register;

import static java.util.Objects.requireNonNull;

/**
 * This binding allows for reading from and writing to a memory address that is stored in a register.
 *
 * @author Brendan Jones
 */
public class RegisterAddressBinding implements ReadWriteBinding {

    /**
     * The register containing the memory address.
     */
    private final Register register;

    /**
     * The memory instance being bound to.
     */
    private final Memory memory;

    /**
     * The base address of the binding.
     */
    private final int baseAddress;

    public RegisterAddressBinding(Register register, Memory memory, int baseAddress) {
        this.register = requireNonNull(register, "Register cannot be null.");
        this.memory = requireNonNull(memory, "Memory cannot be null.");
        this.baseAddress = baseAddress & 0xFFFF;
    }

    @Override
    public void write(int value) {
        final var address = readAddress();
        memory.write(address, value);
    }

    @Override
    public int read() {
        final var address = readAddress();
        return memory.readUnsigned(address);
    }

    /**
     * Calculates the target address.
     *
     * @return The target address.
     */
    private int readAddress() {
        return baseAddress + register.read();
    }

    @Override
    public String toString() {
        return "(" + register + ")";
    }

}
