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
 * Implements the "SWAP" instruction. This instruction swaps the upper and lower nybbles of the target.
 *
 * @author Brendan Jones
 */
public class OP_SWAP extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Creates a new SWAP instruction.
     *
     * @param target The target binding.
     * @param cycles How many cycles the instruction takes to complete.
     */
    private OP_SWAP(ReadWriteBinding target, int cycles) {
        super("SWAP " + target, cycles);
        this.target = requireNonNull(target);
    }

    /**
     * Creates a new SWAP instruction with the format {@code SWAP Reg8}.
     *
     * @param target The target register.
     */
    public OP_SWAP(Register8 target) {
        this(target.binding(), 4);
    }

    /**
     * Creates a new SWAP instruction with the format {@code SWAP (Reg16)}.
     *
     * @param target The target register.
     */
    public OP_SWAP(Register16 target) {
        this(target.addressBinding(), 12);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var value = target.read();

        final var result = ((value >> 4) & 0xF) | ((value & 0xF) << 4);
        target.write(result);

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
        flags.write(Flag.C, false);
    }

}
