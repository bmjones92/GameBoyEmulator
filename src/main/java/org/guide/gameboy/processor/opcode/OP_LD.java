package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.opcode.binding.WriteBinding;
import org.guide.gameboy.processor.register.PointerRegister;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;
import org.guide.util.BitUtils;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "LD" instruction. This instruction copies data from a source to a target.
 *
 * @author Brendan Jones
 */
public class OP_LD extends Opcode {

    /**
     * The target binding.
     */
    private final WriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * The offset binding.
     */
    private final ReadBinding offset;

    /**
     * Creates a new LD instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param offset The offset binding.
     */
    private OP_LD(WriteBinding target, ReadBinding source, ReadBinding offset) {
        super("LD " + target + "," + source + "+" + offset, 12);
        this.target = requireNonNull(target, "Target cannot be null.");
        this.source = requireNonNull(source, "Source cannot be null.");
        this.offset = requireNonNull(offset, "Offset cannot be null.");
    }

    /**
     * Creates a new LD instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param cycles The number of cycles the instruction takes.
     */
    private OP_LD(WriteBinding target, ReadBinding source, int cycles) {
        super("LD " + target + "," + source, cycles);
        this.target = requireNonNull(target, "Target cannot be null.");
        this.source = requireNonNull(source, "Source cannot be null.");
        this.offset = null;
    }

    /**
     * Creates a new LD instruction with the format {@code LD Reg16,d16} or {@code LD (Reg16), d8}.
     *
     * @param target      The target binding.
     * @param source      The source binding.
     * @param bindAddress Whether the source binding is an address or immediate value.
     */
    public OP_LD(Register16 target, MemoryBindings source, boolean bindAddress) {
        this(bindAddress ? target.addressBinding() : target.binding(),
                bindAddress ? source.bindingD8() : source.bindingD16(), 12);
    }

    /**
     * Creates a new LD instruction with the format {@code LD RegPtr,d16}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LD(PointerRegister target, MemoryBindings source) {
        this(target.binding(), source.bindingD16(), 12);
    }

    /**
     * Creates a new LD instruction with the format {@code LD (Reg16),Reg8}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LD(Register16 target, Register8 source) {
        this(target.addressBinding(), source.binding(), 8);
    }

    /**
     * Creates a new LD instruction with the format {@code LD Reg8,(a8)} or {@code LD Reg8,d8}.
     *
     * @param target      The target binding.
     * @param source      The source binding.
     * @param bindAddress Whether to bind to the source address or its immediate value.
     */
    public OP_LD(Register8 target, MemoryBindings source, boolean bindAddress) {
        this(target.binding(), bindAddress ? source.bindingA8Address() : source.bindingD8(), bindAddress ? 12 : 8);
    }

    /**
     * Creates a new LD instruction with the format {@code LD (a16),Reg8} or {@code LD (a8), Reg8}.
     *
     * @param target        The target binding.
     * @param source        The source binding.
     * @param bindAddress16 Whether to bind to a 16-bit address or 8-bit address.
     */
    public OP_LD(MemoryBindings target, Register8 source, boolean bindAddress16) {
        this(bindAddress16 ? target.bindingA16Address() : target.bindingA8Address(), source.binding(),
                bindAddress16 ? 16 : 12);
    }

    /**
     * Creates a new LD instruction with the format {@code LD Reg8,(Reg16)}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LD(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new LD instruction with the format {@code LD Reg8,Reg8}, {@code LD Reg8,(Reg8)},
     * {@code LD (Reg8),Reg8}, or {@code LD (Reg8),(Reg8)}.
     *
     * @param target            The target binding.
     * @param bindTargetAddress Whether to bind to the target's address or immediate value.
     * @param source            The source binding.
     * @param bindSourceAddress Whether to bind to the source's address or immediate value.
     */
    public OP_LD(Register8 target, boolean bindTargetAddress, Register8 source, boolean bindSourceAddress) {
        this(bindTargetAddress ? target.addressBinding() : target.binding(),
                bindSourceAddress ? source.addressBinding() : source.binding(),
                (bindTargetAddress || bindSourceAddress) ? 8 : 4);
    }

    /**
     * Creates a new LD instruction with the format {@code Reg16,RegPtr+r8}.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param memory The offset binding.
     */
    public OP_LD(Register16 target, PointerRegister source, MemoryBindings memory) {
        this(target.binding(), source.binding(), memory.bindingR8());
    }

    /**
     * Creates a new LD instruction with the format {@code LD RegPtr,Reg16}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LD(PointerRegister target, Register16 source) {
        this(target.binding(), source.binding(), 8);
    }

    /**
     * Creates a new LD instruction with the format {@code LD Reg8,(a16)}.
     *
     * @param target The target binding.
     * @param memory The source binding.
     */
    public OP_LD(Register8 target, MemoryBindings memory) {
        this(target.binding(), memory.bindingA16Address(), 16);
    }

    /**
     * Creates a new LD instruction with the format {@code LD (a16),RegPtr}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LD(MemoryBindings target, PointerRegister source) {
        this(target.bindingA16Pointer(), source.binding(), 20);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var sourceValue = source.read();

        var result = sourceValue;
        if (offset != null) {
            final var offsetValue = offset.read();
            result += offsetValue;

            final var mask = sourceValue ^ offsetValue ^ result;

            flags.write(Flag.Z, false);
            flags.write(Flag.N, false);
            flags.write(Flag.H, BitUtils.isSet(mask, 4));
            flags.write(Flag.C, BitUtils.isSet(mask, 8));
        }

        target.write(result);
    }

}
