package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import java.util.Objects;

/**
 * Implements the "DA" instruction.
 *
 * @author Brendan Jones
 */
public class OP_DA extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Creates a new DA instruction.
     *
     * @param target The target binding.
     */
    private OP_DA(ReadWriteBinding target) {
        super("DA " + target, 4);
        this.target = Objects.requireNonNull(target);
    }

    /**
     * Creates a new DA instruction.
     *
     * @param target The target binding.
     */
    public OP_DA(Register8 target) {
        this(target.binding());
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        var value = target.read();
        if (!flags.read(Flag.N)) {
            // Handle addition operations.
            if (flags.read(Flag.H) || (value & 0xF) > 0x9) {
                value += 0x6;
            }

            if (flags.read(Flag.C) || value > 0x9F) {
                value += 0x60;
            }
        } else {
            // Handle subtraction operations.
            if (flags.read(Flag.H)) {
                value = (value - 6) & 0xFF;
            }

            if (flags.read(Flag.C)) {
                value -= 0x60;
            }
        }

        if ((value & 0x100) == 0x100) {
            flags.write(Flag.C, true);
        }

        target.write(value);
        value = target.read();

        flags.write(Flag.Z, value == 0);
        flags.write(Flag.H, false);
    }

}
