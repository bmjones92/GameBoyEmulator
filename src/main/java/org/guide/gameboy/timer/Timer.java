package org.guide.gameboy.timer;

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
 * Implements the Game Boy's timer hardware.
 *
 * @author Brendan Jones
 */
public class Timer extends AddressSpace implements SerializableComponent {

    /**
     * The bits that must flip for the timer interrupt for fire for the different timer frequencies.
     */
    private static final int[] TIMER_INCREMENT_FREQUENCY_BITS = {9, 3, 5, 7};

    /**
     * The interrupt controller.
     */
    private final InterruptController interrupts;

    /**
     * The current clock value.
     */
    private int clock;

    /**
     * The number of cycles to delay the interrupt once the timer overflows.
     */
    private int interruptDelayCycles;

    /**
     * The timer increment signal. The timer is only incremented when
     * this signal goes from high to low.
     */
    private boolean timerSignal;

    /**
     * Creates a new Timer instance.
     *
     * @param memory     The system's memory component.
     * @param interrupts The interrupt controller.
     */
    public Timer(Memory memory, InterruptController interrupts) {
        super(memory);
        this.interrupts = requireNonNull(interrupts);

        memory.setAddressSpace(this, AddressUtils.DIV);
        memory.setAddressSpace(this, AddressUtils.TIMA);
        memory.setAddressSpace(this, AddressUtils.TMA);
        memory.setAddressSpace(this, AddressUtils.TAC);
    }

    @Override
    public void write(int address, int value) {
        memory.set(address, value);

        switch (address) {
            case AddressUtils.DIV -> {
                this.clock = 0;
                updateTimerSignal();
            }
            case AddressUtils.TAC -> updateTimerSignal();
            case AddressUtils.TIMA -> this.interruptDelayCycles = 0;
        }
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.putShort((short) clock);
        out.put((byte) interruptDelayCycles);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        this.clock = in.getShort() & 0xFFFF;
        this.interruptDelayCycles = in.get() & 0xFF;
    }

    public void reset(boolean isCGB) {
        this.clock = isCGB ? 0x1EA0 : 0x267C;
        memory.set(AddressUtils.DIV, clock >> 8);

        this.timerSignal = false;
        this.interruptDelayCycles = 0;

        memory.set(AddressUtils.TIMA, 0x00);
        memory.set(AddressUtils.TMA, 0x00);
        memory.set(AddressUtils.TAC, 0x00);
    }

    /**
     * Ticks the timer hardware.
     *
     * @param elapsedCycles The number of cycles to emulate.
     */
    public void tick(int elapsedCycles) {
        // There is small delay after the timer overflows before TMA is loaded into TIMA and the timer interrupt is
        // requested.
        if (interruptDelayCycles > 0) {
            interruptDelayCycles -= elapsedCycles;
            if (interruptDelayCycles <= 0) {
                interruptDelayCycles = 0;

                // Request interrupt and update memory.
                memory.set(AddressUtils.TIMA, memory.getUnsigned(AddressUtils.TMA));
                interrupts.setRequested(Interrupt.TIMER, true);
            }
        }

        // Update the internal clock and DIV register.
        for (var i = 0; i < elapsedCycles; i += 4) {
            clock = (clock + 4) & 0xFFFF;
            memory.set(AddressUtils.DIV, clock >> 8);
            updateTimerSignal();
        }
    }

    /**
     * Updates the timer signal based on the current register values.
     */
    private void updateTimerSignal() {
        final var oldSignal = timerSignal;

        final var tac = memory.getUnsigned(AddressUtils.TAC);

        this.timerSignal = BitUtils.isSet(tac, 2) && BitUtils.isSet(clock, TIMER_INCREMENT_FREQUENCY_BITS[tac & 0x3]);
        if (oldSignal && !timerSignal) {
            final var tima = (memory.getUnsigned(AddressUtils.TIMA) + 1) & 0xFF;
            if (tima == 0) {
                interruptDelayCycles = 4;
            }
            memory.set(AddressUtils.TIMA, tima);
        }
    }

}