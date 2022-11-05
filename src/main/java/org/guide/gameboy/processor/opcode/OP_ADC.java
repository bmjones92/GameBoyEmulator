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
 * Implements the "ADC" instruction.
 *
 * @author Brendan Jones
 */
public class OP_ADC extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new ADC instruction.
     *
     * @param target The binding to write to.
     * @param source The binding to read from.
     * @param cycles The number of cycles this instruction takes.
     */
    private OP_ADC(ReadWriteBinding target, ReadBinding source, int cycles) {
        super("ADC " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new ADC instruction with format {@code ADC Reg8,(Reg16)}.
     *
     * @param target The register to write to.
     * @param source The register to read from.
     */
    public OP_ADC(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new ADC instruction with format {@code ADC Reg8,Reg8}.
     *
     * @param target The register to write to.
     * @param source The register to read from.
     */
    public OP_ADC(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new ADC instruction with format {@code ADC Reg8,d8}.
     *
     * @param target The target to write to.
     * @param source The memory bindings to bind to.
     */
    public OP_ADC(Register8 target, MemoryBindings source) {
        this(target.binding(), source.bindingD8(), 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetValue = target.read();
        final var sourceValue = source.read();

        final var carry = flags.read(Flag.C) ? 1 : 0;

        final var result = targetValue + sourceValue + carry;
        target.write(result);

        flags.write(Flag.Z, target.read() == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, (sourceValue & 0xF) + (targetValue & 0xF) + carry > 0xF);
        flags.write(Flag.C, result > 0xFF);
    }

}
