package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Base implementation for jump instructions.
 *
 * @author Brendan Jones
 */
public abstract class JumpOpcode extends Opcode {

    /**
     * The number of cycles the instruction takes when the jump condition passes.
     */
    private final int jumpCycles;

    /**
     * The number of cycles the instruction takes when the jump condition fails.
     */
    private final int noJumpCycles;

    /**
     * The flag to compare to.
     */
    private final Flag flag;

    /**
     * Whether the flag should be set for the jump condition to pass.
     */
    private final boolean value;

    /**
     * Whether the jump condition succeeded.
     */
    private boolean jumped;

    /**
     * Creates a new jump instruction.
     *
     * @param instruction  The instruction name.
     * @param flag         The flag to compare to, or null if the jump should always succeed.
     * @param value        Whether the flag should be set for the jump condition to pass.
     * @param jumpCycles   The number of cycles when the jump passes.
     * @param noJumpCycles The number of cycles when the jump fails.
     */
    public JumpOpcode(String instruction, Flag flag, boolean value, int jumpCycles, int noJumpCycles) {
        super(instruction, -1);
        this.flag = flag;
        this.value = value;
        this.jumpCycles = jumpCycles;
        this.noJumpCycles = noJumpCycles;
    }

    /**
     * Checks the flag register to see if the jump should pass or fail.
     *
     * @param flags The flag register.
     * @return Whether the jump passed.
     */
    public boolean checkJumped(FlagRegister flags) {
        this.jumped = (flag == null || flags.read(flag) == value);
        return jumped;
    }

    @Override
    public int getExecutionCycles() {
        if (jumped) {
            return jumpCycles;
        } else {
            return noJumpCycles;
        }
    }

}
