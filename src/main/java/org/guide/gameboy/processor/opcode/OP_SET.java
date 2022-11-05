package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the "SET" and "RES" instructions. These instructions set or clear a bit in the target register.
 *
 * @author Brendan Jones
 */
public class OP_SET extends Opcode {

    /**
     * The mask of the bit to set.
     */
    private final int mask;

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Whether the bit should be set or unset.
     */
    public final boolean setBit;

    /**
     * Creates a new SET instruction.
     *
     * @param bit    The bit to set.
     * @param target The target
     * @param setBit Whether the bit should be set or cleared.
     * @param cycles How many processor cycles the instruction takes to complete.
     */
    private OP_SET(int bit, ReadWriteBinding target, boolean setBit, int cycles) {
        super((setBit ? "SET " : "RES ") + target, cycles);
        this.mask = (1 << bit);
        this.target = target;
        this.setBit = setBit;
    }

    /**
     * Creates a new SET or RES instruction with the format {@code SET bit,Reg8} or {@code RES bit,Reg8}.
     *
     * @param bit    The bit to set or clear.
     * @param target The target register.
     * @param value  Whether the bit should be set or cleared.
     */
    public OP_SET(int bit, Register8 target, boolean value) {
        this(bit, target.binding(), value, 8);
    }

    /**
     * Creates a new SET or RES instruction with the format {@code SET bit,(Reg16)} or {@code RES bit,(Reg16)}.
     *
     * @param bit    The bit to set or clear.
     * @param target The target register.
     * @param value  Whether the bit should be set or cleared.
     */
    public OP_SET(int bit, Register16 target, boolean value) {
        this(bit, target.addressBinding(), value, 16);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        var result = target.read();
        if (setBit) {
            result |= mask;
        } else {
            result &= ~mask;
        }
        target.write(result);
    }

}
