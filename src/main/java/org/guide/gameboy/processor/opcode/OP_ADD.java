package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.PointerRegister;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;
import org.guide.util.BitUtils;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "ADD" instruction.
 *
 * @author Brendan Jones
 */
public class OP_ADD extends Opcode {

    /**
     * Indicates which flags need to be updated after the instruction completes.
     */
    private enum UpdateFlags {
        REG_16, REG_8, POINTER
    }

    /**
     * The binding to write the result to.
     */
    private final ReadWriteBinding target;

    /**
     * The binding to read from.
     */
    private final ReadBinding source;

    /**
     * The flags to update.
     */
    private final UpdateFlags updateFlags;

    /**
     * Creates a new ADD instruction.
     *
     * @param target      The target data binding.
     * @param source      The source data binding.
     * @param updateFlags The type of flags to update.
     * @param cycles      The number of cycles the instruction takes to complete.
     */
    private OP_ADD(ReadWriteBinding target, ReadBinding source, UpdateFlags updateFlags, int cycles) {
        super("ADD " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
        this.updateFlags = requireNonNull(updateFlags);
    }

    /**
     * Creates a new ADD instruction with the format: {@code ADD Reg16,Reg16}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_ADD(Register16 target, Register16 source) {
        this(target.binding(), source.binding(), UpdateFlags.REG_16, 8);
    }

    /**
     * Creates a new ADD instruction with the format {@code ADD Reg16,PointerRegister}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_ADD(Register16 target, PointerRegister source) {
        this(target.binding(), source.binding(), UpdateFlags.REG_16, 8);
    }

    /**
     * Creates a new ADD instruction with the format: {@code ADD Reg8,Reg8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_ADD(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), UpdateFlags.REG_8, 4);
    }

    /**
     * Creates a new ADD instruction with the format: {@code ADD Reg8,(Reg16)}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_ADD(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), UpdateFlags.REG_8, 8);
    }

    /**
     * Creates a new ADD instruction with the format: {@code ADD Reg8,d8}).
     *
     * @param target The target register.
     * @param source The source memory bindings.
     */
    public OP_ADD(Register8 target, MemoryBindings source) {
        this(target.binding(), source.bindingD8(), UpdateFlags.REG_8, 8);
    }

    /**
     * Creates a new ADD instruction with the format: {@code Reg16,r8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_ADD(PointerRegister target, MemoryBindings source) {
        this(target.binding(), source.bindingR8(), UpdateFlags.POINTER, 16);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetValue = target.read();
        final var sourceValue = source.read();

        final var result = targetValue + sourceValue;
        target.write(result);

        final var carryBits = targetValue ^ sourceValue ^ result;

        final var isZero = target.read() == 0;
        switch (updateFlags) {
            case REG_16 -> {
                flags.write(Flag.N, false);
                flags.write(Flag.H, BitUtils.isSet(carryBits, 12));
                flags.write(Flag.C, BitUtils.isSet(carryBits, 16));
            }
            case REG_8 -> {
                flags.write(Flag.Z, isZero);
                flags.write(Flag.N, false);
                flags.write(Flag.H, BitUtils.isSet(carryBits, 4));
                flags.write(Flag.C, BitUtils.isSet(carryBits, 8));
            }
            case POINTER -> {
                flags.write(Flag.Z, false);
                flags.write(Flag.N, false);
                flags.write(Flag.H, BitUtils.isSet(carryBits, 4));
                flags.write(Flag.C, BitUtils.isSet(carryBits, 8));
            }
        }
    }
}
