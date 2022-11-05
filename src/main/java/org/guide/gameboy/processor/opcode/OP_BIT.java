package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import java.util.Objects;

/**
 * Implements the "BIT" instruction.
 *
 * @author Brendan Jones
 */
public class OP_BIT extends Opcode {

    /**
     * The mask of the bit being tested.
     */
    private final int mask;

    /**
     * The binding to test.
     */
    private final ReadBinding target;

    /**
     * Creates a new BIT instruction.
     *
     * @param bit    The bit being tested.
     * @param target The data binding containing the value to check.
     * @param cycles The number of cycles this instruction takes to complete.
     */
    private OP_BIT(int bit, ReadBinding target, int cycles) {
        super("BIT " + bit + "," + target, cycles);
        this.mask = 1 << bit;
        this.target = Objects.requireNonNull(target, "Target binding cannot be null.");
    }

    /**
     * Creates a nit BIT instruction with format {@code BIT bit,Reg8}.
     *
     * @param bit    The bit being tested.
     * @param target The register containing the value to check.
     */
    public OP_BIT(int bit, Register8 target) {
        this(bit, target.binding(), 8);
    }

    /**
     * Creates a new BIT instruction with format {@code BIT bit,(Reg16)}.
     *
     * @param bit    The bit being tested.
     * @param target The register containing the value to check.
     */
    public OP_BIT(int bit, Register16 target) {
        this(bit, target.addressBinding(), 16);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var result = target.read() & mask;

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, true);
    }

}
