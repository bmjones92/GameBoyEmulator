package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "CP" instruction.
 *
 * @author Brendan Jones
 */
public class OP_CP extends Opcode {

    /**
     * The target binding.
     */
    private final ReadBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new CP instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param cycles The number of cycles the instruction takes to complete.
     */
    private OP_CP(ReadBinding target, ReadBinding source, int cycles) {
        super("CP " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new CP instruction with the format {@code CP Reg8,Reg8}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_CP(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new CP instruction with the format {@code CP Reg8,(Reg16)}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_CP(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new CP instruction with the format {@code CP Reg8,d8}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_CP(Register8 target, MemoryBindings source) {
        this(target.binding(), source.bindingD8(), 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetValue = target.read();
        final var sourceValue = source.read();

        final var result = targetValue - sourceValue;

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, true);
        flags.write(Flag.H, (result & 0xF) > (targetValue & 0xF));
        flags.write(Flag.C, result < 0);
    }

}
