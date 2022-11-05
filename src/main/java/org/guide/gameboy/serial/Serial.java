package org.guide.gameboy.serial;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.Interrupt;
import org.guide.gameboy.processor.interrupts.InterruptController;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.util.AddressUtils;
import org.guide.util.BitUtils;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

/**
 * Implements the Game Boy's serial hardware.
 *
 * @author Brendan Jones
 */
public class Serial extends AddressSpace implements SerializableComponent {

    /**
     * The interrupt controller.
     */
    private final InterruptController interrupts;

    /**
     * The number of cycles so far in the current transfer.
     */
    private int serialCycles;

    /**
     * The current bit being transferred.
     */
    private int serialBit;

    public Serial(Memory memory, InterruptController interrupts) {
        super(memory);
        this.interrupts = requireNonNull(interrupts);

        memory.setAddressSpace(this, AddressUtils.SB);
        memory.setAddressSpace(this, AddressUtils.SC);
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.putShort((short) serialCycles);
        out.put((byte) serialBit);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        this.serialCycles = in.getShort() & 0xFFFF;
        this.serialBit = in.get() & 0xFF;
    }

    public void reset() {
        // TODO Reset registers to their default values.
    }

    /**
     * Ticks the serial hardware.
     *
     * @param elapsedCycles The number of cycles to emulate.
     */
    public void tick(int elapsedCycles) {
        final var sc = memory.get(AddressUtils.SC);

        // Relies on external clock and is not currently transferring.
        if (!BitUtils.isSet(sc, 0) || !BitUtils.isSet(sc, 7)) {
            return;
        }

        serialCycles += elapsedCycles;
        if (serialBit < 0) {
            serialBit = 0;
            serialCycles = 0;
            return;
        }

        if (serialCycles > 512) {
            if (serialBit > 7) {
                memory.set(AddressUtils.SC, sc & 0x7F);

                interrupts.setEnabled(Interrupt.SERIAL, true);
                serialBit = -1;
                return;
            }

            final var sb = memory.read(AddressUtils.SB);
            memory.set(AddressUtils.SB, (sb << 1) | 0x1);

            serialCycles -= 512;
            serialBit++;
        }
    }

}
