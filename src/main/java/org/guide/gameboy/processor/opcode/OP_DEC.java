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
 * Creates a new "DEC" instruction.
 *
 * @author Brendan Jones
 */
public class OP_DEC extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Whether to update flags.
     */
    private final boolean updateFlags;

    /**
     * Creates a new instruction.
     *
     * @param target      The binding target.
     * @param updateFlags Whether to update the flag register.
     * @param cycles      The number of cycles the instruction takes to complete.
     */
    private OP_DEC(ReadWriteBinding target, boolean updateFlags, int cycles) {
        super("DEC " + target, cycles);
        this.target = requireNonNull(target);
        this.updateFlags = updateFlags;
    }

    /**
     * Creates a new instruction with the format {@code DEC Reg8}.
     *
     * @param target The target register.
     */
    public OP_DEC(Register8 target) {
        this(target.binding(), true, 4);
    }

    /**
     * Creates a new instruction with the format {@code DEC Reg16} or {@code DEC (Reg16)}.
     *
     * @param target      The target register.
     * @param bindAddress Whether to bind to the register's address instead of the register itself.
     */
    public OP_DEC(Register16 target, boolean bindAddress) {
        this(bindAddress ? target.addressBinding() : target.binding(), bindAddress, bindAddress ? 12 : 8);
    }

    /**
     * Creates a new instruction with the format {@code DEC PtrRegister}.
     *
     * @param target The target register.
     */
    public OP_DEC(PointerRegister target) {
        this(target.binding(), false, 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var result = target.read() - 1;
        target.write(result);

        if (updateFlags) {
            flags.write(Flag.Z, result == 0);
            flags.write(Flag.N, true);
            flags.write(Flag.H, (result & 0xF) == 0xF);
        }
    }

}
