package org.guide.gameboy.processor.register;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.RegisterAddressBinding;
import org.guide.gameboy.processor.opcode.binding.RegisterBinding;

import static java.util.Objects.requireNonNull;

/**
 * Base class for all register implementations.
 *
 * @author Brendan Jones
 */
public abstract class Register {

    /**
     * The human-readable name of the register.
     */
    private final String name;

    /**
     * The binding for reading and writing the value of the register.
     */
    private final RegisterBinding binding;

    /**
     * The binding for reading from and writing to the memory address stored in this register.
     */
    private final RegisterAddressBinding addressBinding;

    /**
     * Creates a new Register.
     *
     * @param name        The human-readable name of the Register.
     * @param memory      The memory component to creating bindings for.
     * @param baseAddress The base address for this register.
     */
    public Register(String name, Memory memory, int baseAddress) {
        this.name = requireNonNull(name);
        this.binding = new RegisterBinding(this);
        this.addressBinding = new RegisterAddressBinding(this, memory, baseAddress);
    }

    /**
     * Writes a value to this Register.
     *
     * @param value The value to write.
     */
    public abstract void write(int value);

    /**
     * Reads the value from this Register.
     *
     * @return The value that was read.
     */
    public abstract int read();

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the value binding for this register.
     *
     * @return The value binding.
     */
    public RegisterBinding binding() {
        return binding;
    }

    /**
     * Gets the address binding for this register.
     *
     * @return The address binding.
     */
    public RegisterAddressBinding addressBinding() {
        return addressBinding;
    }

}