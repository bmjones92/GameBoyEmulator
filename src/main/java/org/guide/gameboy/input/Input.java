package org.guide.gameboy.input;

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
 * Implements the input functionality of the Game Boy.
 *
 * @author Brendan Jones
 */
public class Input extends AddressSpace implements SerializableComponent {

    /**
     * The Game Boy's interrupt controller.
     */
    private final InterruptController interrupts;

    /**
     * The state of the 8 Game Boy buttons. A bit value of 0 indicates that the corresponding button is pressed.
     */
    private int inputState;

    /**
     * The internal interrupt signal. An interrupt is generated when on this signal's falling edge.
     */
    private boolean irqSignal;

    /**
     * Creates a new Input instance.
     *
     * @param memory     The memory component.
     * @param interrupts The interrupt controller.
     */
    public Input(Memory memory, InterruptController interrupts) {
        super(memory);
        this.interrupts = requireNonNull(interrupts);

        memory.setAddressSpace(this, AddressUtils.P1);
    }

    /**
     * Resets the input component to its default state.
     */
    public void reset() {
        this.irqSignal = true;
        this.inputState = 0xFF;
        memory.set(AddressUtils.P1, 0xFF);
    }

    @Override
    public void write(int address, int value) {
        super.write(address, value);
    }

    @Override
    public void serialize(ByteBuffer out) {
        // Probably no point in serializing the input state.
    }

    @Override
    public void deserialize(ByteBuffer in) {
        // Probably no point in serializing the input state.
    }

    /**
     * Sets whether a button is pressed or released.
     *
     * @param button  The button to set.
     * @param pressed Whether the button is pressed or released.
     */
    public void setButton(GameboyButton button, boolean pressed) {
        if (pressed) {
            inputState = BitUtils.clearBit(inputState, button.ordinal()) & 0xFF;
        } else {
            inputState = BitUtils.setBit(inputState, button.ordinal());
        }
    }

    public void tick() {
        // TODO The signal should only be triggered when the input line changes.
        boolean oldSignal = irqSignal;

        final var p1 = memory.getUnsigned(AddressUtils.P1);

        var newP1 = p1 & 0xF0; //Clear the lower nybble.
        switch (p1 & 0x30) {
            case 0x10 -> { // Selected Line: 4
                irqSignal = (inputState & 0xF) == 0xF;
                newP1 |= inputState & 0xF;
            }
            case 0x20 -> { // Selected Line: 5
                irqSignal = ((inputState >> 4) & 0xF) == 0xF;
                newP1 |= (inputState >> 4) & 0xF;
            }
            case 0x30 -> { // Selected Lines: 4 & 5
                irqSignal = (inputState & 0xFF) == 0xFF;
                newP1 |= ((inputState >> 4) & 0xF) | (inputState & 0xF);
            }
        }

        memory.set(AddressUtils.P1, newP1);

        // Interrupt signals are generated when a falling edge is detected on the internal signal.
        if (oldSignal && !irqSignal) {
            interrupts.setRequested(Interrupt.INPUT, true);
        }
    }

}
