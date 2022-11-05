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
 * Implements the "OR" instruction. This instruction calculates the bitwise OR of the source and target bindings and
 * loads the result into the target binding.
 */
public class OP_OR extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new OR instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param cycles How many cycles the instruction takes.
     */
    private OP_OR(ReadWriteBinding target, ReadBinding source, int cycles) {
        super("OR " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new OR instruction with the format {@code OR Reg8,Reg8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_OR(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new OR instruction with the format {@code OR Reg8,(Reg16)}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_OR(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new OR instruction with the format {@code OR Reg8,d8}.
     *
     * @param target The target register.
     * @param source The source binding.
     */
    public OP_OR(Register8 target, MemoryBindings source) {
        this(target.binding(), source.bindingD8(), 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var result = target.read() | source.read();
        target.write(result);

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
        flags.write(Flag.C, false);
    }

}
