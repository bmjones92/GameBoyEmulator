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
import org.guide.util.BitUtils;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "SUB" instruction. This instruction subtracts the source value from the target.
 *
 * @author Brendan Jones
 */
public class OP_SUB extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new SUB instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param cycles How many cycles the instruction takes to complete.
     */
    private OP_SUB(ReadWriteBinding target, ReadBinding source, int cycles) {
        super("SUB " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new SUB instruction with the format {@code SUB Reg8,Reg8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_SUB(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new SUB instruction with the format {@code SUB Reg8,(Reg16)}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_SUB(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new SUB instruction with the format {@code SUB Reg8,d8}.
     *
     * @param target The target register.
     * @param memory The memory bindings.
     */
    public OP_SUB(Register8 target, MemoryBindings memory) {
        this(target.binding(), memory.bindingD8(), 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetValue = target.read();
        final var sourceValue = source.read();

        final var result = targetValue - sourceValue;
        target.write(result);

        final var borrowBits = targetValue ^ sourceValue ^ result;

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, true);
        flags.write(Flag.H, BitUtils.isSet(borrowBits, 4));
        flags.write(Flag.C, BitUtils.isSet(borrowBits, 8));
    }

}
