package org.guide.gameboy.processor.register.flag;

import org.guide.gameboy.processor.register.Register8;

import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Provides utility functions for performing common operations on the flag register.
 *
 * @author Brendan Jones
 */
public class FlagRegister {

    /**
     * The underlying register.
     */
    private final Register8 register;

    /**
     * Creates a new flag register.
     *
     * @param register The underlying register.
     */
    public FlagRegister(Register8 register) {
        this.register = requireNonNull(register);
    }

    /**
     * Sets or clears a flag.
     *
     * @param flag  The flag to set or unset.
     * @param value Whether the flag should be set or unset.
     */
    public void write(Flag flag, boolean value) {
        var mask = register.read();
        if (value) {
            mask |= flag.mask();
        } else {
            mask &= ~flag.mask();
        }
        register.write(mask);
    }

    /**
     * Toggles the value of a flag.
     *
     * @param flag The flag to toggle.
     */
    public void toggle(Flag flag) {
        write(flag, !read(flag));
    }

    /**
     * Reads the value of a flag.
     *
     * @param flag The flag.
     * @return Whether the flag is set or unset.
     */
    public boolean read(Flag flag) {
        return (register.read() & flag.mask()) != 0;
    }

    @Override
    public String toString() {
        return Arrays.stream(Flag.values())
                .map(flag -> flag.toString(read(flag)))
                .collect(Collectors.joining(","));
    }

}
