package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "SBC" instruction. This instruction subtracts the source from the target and stores the carry flag.
 *
 * @author Brendan Jones
 */
public class OP_SBC extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new SBC instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param cycles How many cycles the instruction takes to complete.
     */
    private OP_SBC(ReadWriteBinding target, ReadBinding source, int cycles) {
        super("SBC " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new SBC instruction with the format {@code SBC Reg8,(Reg16)}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_SBC(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new SBC instruction with the format {@code SBC Reg8,Reg8}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_SBC(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new SBC instruction with the format {@code SBC Reg8,d8}.
     *
     * @param target The target binding.
     * @param memory The source binding.
     */
    public OP_SBC(Register8 target, MemoryBindings memory) {
        this(target.binding(), memory.bindingD8(), 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetValue = target.read();
        final var sourceValue = source.read();

        final var carry = flags.read(Flag.C) ? 1 : 0;

        final var result = targetValue - sourceValue - carry;
        target.write(result);

        flags.write(Flag.Z, target.read() == 0);
        flags.write(Flag.N, true);
        flags.write(Flag.H, (targetValue & 0xF) - (sourceValue & 0xF) - carry < 0);
        flags.write(Flag.C, result < 0);
    }

}
