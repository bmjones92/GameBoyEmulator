package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.PointerRegister;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "INC" instruction.
 *
 * @author Brendan Jones
 */
public class OP_INC extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Whether to update the flag register.
     */
    private final boolean updateFlags;

    private OP_INC(ReadWriteBinding target, boolean updateFlags, int cycles) {
        super("INC " + target, cycles);
        this.target = requireNonNull(target);
        this.updateFlags = updateFlags;
    }

    /**
     * Creates a new INC instruction with the format {@code INC Reg8}.
     *
     * @param target The target register.
     */
    public OP_INC(Register8 target) {
        this(target.binding(), true, 4);
    }

    /**
     * Creates a new INC instruction with the format {@code INC Reg16} or {@code INC (Reg16)}.
     *
     * @param target      The target register.
     * @param bindAddress Whether to bind to the register's address instead of the register itself.
     */
    public OP_INC(Register16 target, boolean bindAddress) {
        this(bindAddress ? target.addressBinding() : target.binding(), bindAddress, bindAddress ? 12 : 8);
    }

    /**
     * Creates a new INC instruction with the format {@code INC RegPtr}.
     *
     * @param target The target register.
     */
    public OP_INC(PointerRegister target) {
        this(target.binding(), false, 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        target.write(target.read() + 1);

        if (updateFlags) {
            final var result = target.read();
            flags.write(Flag.Z, result == 0);
            flags.write(Flag.N, false);
            flags.write(Flag.H, (result & 0x0F) == 0);
        }
    }

}
