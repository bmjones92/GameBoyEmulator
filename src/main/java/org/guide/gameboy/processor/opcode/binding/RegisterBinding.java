package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.register.Register;

import static java.util.Objects.requireNonNull;

/**
 * This binding allows for reading from and writing to a Register.
 *
 * @author Brendan Jones
 */
public class RegisterBinding implements ReadWriteBinding {

    /**
     * The register to bind to.
     */
    private final Register register;

    /**
     * Creates a new register binding.
     *
     * @param register The register to bind to.
     */
    public RegisterBinding(Register register) {
        this.register = requireNonNull(register, "The register cannot be null!");
    }

    @Override
    public void write(int value) {
        register.write(value);
    }

    @Override
    public int read() {
        return register.read();
    }

    @Override
    public String toString() {
        return register.toString();
    }

}
