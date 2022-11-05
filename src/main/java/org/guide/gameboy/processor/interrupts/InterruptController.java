package org.guide.gameboy.processor.interrupts;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.util.AddressUtils;
import org.guide.util.BitUtils;
import org.guide.util.StringUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Address space that is responsible for the interrupt registers.
 *
 * @author Brendan Jones
 */
public class InterruptController extends AddressSpace implements SerializableComponent {

    /**
     * Whether the system is running in CGB mode.
     */
    private boolean isCGB;

    /**
     * The "Interrupt Master Enable" (IME) flag. This must be set for any interrupts to execute regardless of the state
     * of the IE register.
     */
    private boolean masterEnable;

    /**
     * The number of remaining cycles to wait before any interrupts can trigger.
     */
    private int masterEnableDelayCycles;

    /**
     * The number of remaining cycles to wait before specific interrupts can trigger.
     */
    private final int[] delayCycles = new int[Interrupt.values().length];

    /**
     * Creates a new InterruptController instance.
     *
     * @param memory The system memory.
     */
    public InterruptController(Memory memory) {
        super(memory);

        memory.setAddressSpace(this, AddressUtils.IF);
        memory.setAddressSpace(this, AddressUtils.IE);
    }

    @Override
    public void serialize(ByteBuffer out) {
        SerializableComponent.writeIntegrityCheck(out);

        out.put((byte) (masterEnable ? 1 : 0));
        out.put((byte) masterEnableDelayCycles);
        for (var delayCycle : delayCycles) {
            out.put((byte) delayCycle);
        }
    }

    @Override
    public void deserialize(ByteBuffer in) {
        SerializableComponent.verifyIntegrityCheck(in, "Interrupts::Start");

        this.masterEnable = (in.get() & 0xFF) == 1;
        this.masterEnableDelayCycles = in.get() & 0xFF;
        for (var i = 0; i < delayCycles.length; ++i) {
            delayCycles[i] = in.get() & 0xFF;
        }
    }

    /**
     * Emulate the controller for the specified number of elapsed cycles.
     *
     * @param elapsedCycles The number of cycles to emulate.
     */
    public void tick(int elapsedCycles) {
        // Update any interrupt delay timers.
        for (var i = 0; i < delayCycles.length; ++i) {
            if (delayCycles[i] > 0) {
                delayCycles[i] -= elapsedCycles;
            }
        }

        // Update the IME delay cycles.
        if (masterEnableDelayCycles > 0) {
            masterEnableDelayCycles -= elapsedCycles;
            masterEnableDelayCycles = Math.max(0, masterEnableDelayCycles);
        }
    }

    /**
     * Reset the interrupt controller to its default state.
     *
     * @param isCGB Whether the system is running in CGB mode.
     */
    public void reset(boolean isCGB) {
        this.isCGB = isCGB;

        this.masterEnable = false;
        this.masterEnableDelayCycles = 0;

        memory.set(AddressUtils.IF, 0x00);
        memory.set(AddressUtils.IE, 0x00);

        Arrays.fill(delayCycles, 0);
    }

    /**
     * Enables or disables the master enable flag with the specified delay.
     *
     * @param enable The value to set.
     * @param delay  The number of cycles to delay setting the flag.
     */
    public void setMasterEnable(boolean enable, int delay) {
        this.masterEnable = enable;
        this.masterEnableDelayCycles = delay;
    }

    /**
     * Sets whether the specified interrupt is enabled.
     *
     * @param interrupt The interrupt to set.
     * @param enabled   Whether the interrupt is enabled.
     */
    public void setEnabled(Interrupt interrupt, boolean enabled) {
        setRegister(AddressUtils.IE, interrupt, enabled);
    }

    /**
     * Sets whether the specified interrupt is requested.
     *
     * @param interrupt The interrupt to set.
     * @param requested Whether the interrupt is requested.
     */
    public void setRequested(Interrupt interrupt, boolean requested) {
        setRegister(AddressUtils.IF, interrupt, requested);
    }

    /**
     * Enables or disables the specified interrupt on the specified register.
     *
     * @param register  The register to update.
     * @param interrupt The interrupt to update.
     * @param enable    Whether the interrupt should be enabled.
     */
    private void setRegister(int register, Interrupt interrupt, boolean enable) {
        var result = memory.readUnsigned(register);
        final var bit = interrupt.ordinal();
        if (enable) {
            result = BitUtils.setBit(result, bit);
            delayCycles[bit] = interrupt.getDelay(isCGB);
        } else {
            result = BitUtils.clearBit(result, bit);
            delayCycles[bit] = 0;
        }

        memory.set(register, result);
    }

    /**
     * Gets the next pending interrupt that should be executed. An interrupt can be
     * executed only if the IME flag is set and the corresponding interrupt is
     * enabled.
     *
     * @return The next pending interrupt, or null if none are ready to execute.
     */
    public Interrupt getPendingInterrupt() {
        if (masterEnable && masterEnableDelayCycles == 0) {
            final var flags = memory.readUnsigned(AddressUtils.IF);
            final var enable = memory.readUnsigned(AddressUtils.IE);

            for (var interrupt : Interrupt.values()) {
                final var bit = interrupt.ordinal();
                final var mask = (1 << bit);
                if ((enable & flags & mask) != 0 && delayCycles[bit] <= 0) {
                    return interrupt;
                }
            }
        }

        // No interrupt is ready.
        return null;
    }

    /**
     * Gets whether an interrupt is currently pending.
     *
     * @return Whether an interrupt is pending.
     */
    public boolean isInterruptPending() {
        int flags = memory.readUnsigned(AddressUtils.IF);
        int enable = memory.readUnsigned(AddressUtils.IE);
        return (flags & enable & 0x1F) != 0;
    }

    /**
     * Gets whether the master enable flag is enabled.
     *
     * @return Whether the master enable flag is enabled.
     */
    public boolean getMasterEnabled() {
        return masterEnable;
    }

    @Override
    public String toString() {
        return "IME: " + masterEnable + ", " +
                "IE:  " + memory.readUnsigned(AddressUtils.IE) + ", " +
                "IF:  " + memory.readUnsigned(AddressUtils.IF);
    }

    @Override
    public void write(int address, int value) {
        memory.set(address, value);
    }

    @Override
    public int read(int address) {
        return switch (address) {
            case AddressUtils.IF -> 0xE0 | memory.get(AddressUtils.IF);
            case AddressUtils.IE -> memory.get(AddressUtils.IE);
            default ->
                    throw new UnsupportedOperationException("Not an interrupt register: " + StringUtils.getHex16(address));
        };
    }

}
