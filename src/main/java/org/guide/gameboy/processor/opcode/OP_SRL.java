package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "SRL" instruction. This instruction shifts a target's bits to the right. The most-significant bit
 * is set to 0, and the least-significant bit is pushed into the carry flag.
 *
 * @author Brendan Jones
 */
public class OP_SRL extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Creates a new SRL instruction.
     *
     * @param target The target binding.
     * @param cycles How many cycles the instruction takes to complete.
     */
    private OP_SRL(ReadWriteBinding target, int cycles) {
        super("SRL " + target, cycles);
        this.target = requireNonNull(target);
    }

    /**
     * Creates a new SRL instruction with the format {@code SRL Reg8}.
     *
     * @param target The target register.
     */
    public OP_SRL(Register8 target) {
        this(target.binding(), 4);
    }

    /**
     * Creates a new SRL instruction with the format {@code SRL (Reg16)}.
     *
     * @param target The target register.
     */
    public OP_SRL(Register16 target) {
        this(target.addressBinding(), 12);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetValue = target.read();

        final var result = targetValue >> 1;
        target.write(result);

        flags.write(Flag.Z, target.read() == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
        flags.write(Flag.C, (targetValue & 0x1) == 0x1);
    }

}
