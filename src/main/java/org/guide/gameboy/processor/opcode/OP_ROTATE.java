package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "ROTATE" instructions. These instructions rotate the bits in a binding.
 *
 * @author Brendan Jones
 */
public class OP_ROTATE extends Opcode {

    /**
     * The direction to rotate the bits.
     */
    public enum Direction {
        RIGHT, LEFT
    }

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * The direction to rotate.
     */
    private final Direction direction;

    /**
     * Whether to rotate the carry bit.
     */
    private final boolean carry;

    /**
     * Whether the zero flag should be cleared.
     */
    private final boolean clearZeroFlag;

    /**
     * Creates a new ROTATE instruction.
     *
     * @param target        The target binding.
     * @param direction     The direction to rotate.
     * @param carry         Whether the carry bit should be rotated.
     * @param clearZeroFlag Whether the zero flag should be cleared.
     * @param cycles        How many clock cycles the instruction takes to complete.
     */
    private OP_ROTATE(ReadWriteBinding target, Direction direction, boolean carry, boolean clearZeroFlag, int cycles) {
        super("R" + direction.name().charAt(0) + (carry ? "C " : " ") + target, cycles);
        this.target = requireNonNull(target);
        this.direction = requireNonNull(direction);
        this.carry = carry;
        this.clearZeroFlag = clearZeroFlag;
    }

    /**
     * Creates a new ROTATE instruction.
     *
     * @param target        The target register.
     * @param direction     The direction to rotate.
     * @param carry         Whether the carry bit should be rotated.
     * @param clearZeroFlag Whether the zero flag should be cleared.
     */
    public OP_ROTATE(Register8 target, Direction direction, boolean carry, boolean clearZeroFlag) {
        this(target.binding(), direction, carry, clearZeroFlag, clearZeroFlag ? 4 : 8);
    }

    /**
     * Creates a new ROTATE instruction.
     *
     * @param target    The target register.
     * @param direction The direction to rotate.
     * @param carry     Whether the carry bit should be rotated.
     */
    public OP_ROTATE(Register16 target, Direction direction, boolean carry) {
        this(target.addressBinding(), direction, carry, false, 16);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        var result = target.read();
        if (direction == Direction.LEFT) {
            final var msb = (result >> 7) & 0x1;
            result <<= 1;

            if (carry) {
                // Bit 7 -> CF | Bit 7 -> Bit 0
                flags.write(Flag.C, msb == 1);
                result |= msb;
            } else {
                // CF -> Bit 0 | Bit 7 -> CF
                result |= flags.read(Flag.C) ? 1 : 0;
                flags.write(Flag.C, msb == 1);
            }
        } else {
            final var lsb = result & 0x1;
            result >>= 1;

            if (carry) {
                // Bit 0 -> CF | Bit 0 -> Bit 7
                flags.write(Flag.C, lsb == 1);
                result |= (lsb << 7);
            } else {
                // CF -> Bit 7 | Bit 0 -> CF
                result |= flags.read(Flag.C) ? 0x80 : 0x0;
                flags.write(Flag.C, lsb == 1);
            }
        }

        target.write(result);

        flags.write(Flag.Z, !clearZeroFlag && target.read() == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
    }

}
