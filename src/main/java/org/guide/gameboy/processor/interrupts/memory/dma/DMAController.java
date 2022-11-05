package org.guide.gameboy.processor.interrupts.memory.dma;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.util.AddressUtils;

import java.nio.ByteBuffer;

/**
 * Implements the DMA controller of the Game Boy. The specification for this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Video_Display#LCD_OAM_DMA_Transfers">
 * here
 * </a>
 *
 * @author Brendan Jones
 */
public class DMAController extends AddressSpace implements SerializableComponent {

    /**
     * The number of cycles required to complete a single transfer.
     */
    private static final int TRANSFER_CYCLES = 0xA1;

    /**
     * The memory address of the data source.
     */
    private int sourceAddress;

    /**
     * The current transfer cycle.
     */
    private int currentCycle;

    /**
     * The data value read in the previous cycle.
     */
    private int readValue;

    /**
     * Creates a new DMA controller that operates on the specified memory.
     *
     * @param memory The memory component.
     */
    public DMAController(Memory memory) {
        super(memory);

        memory.setAddressSpace(this, AddressUtils.DMA);
    }

    /**
     * Resets the controller to its default state on machine boot.
     */
    public void reset() {
        this.sourceAddress = TRANSFER_CYCLES;
        this.currentCycle = 0;
        this.readValue = 0;
    }

    @Override
    public void write(int address, int value) {
        super.write(address, value);

        this.sourceAddress = (value & 0xFF) << 8;
        this.currentCycle = 0;
        this.readValue = 0;
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.putShort((byte) sourceAddress);
        out.put((byte) currentCycle);
        out.put((byte) readValue);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        this.sourceAddress = in.getShort() & 0xFFFF;
        this.currentCycle = in.get() & 0xFF;
        this.readValue = in.get() & 0xFF;
    }

    /**
     * Emulates the DMA transfer for the specified number of clock cycles.
     *
     * @param elapsedCycles The number of cycles to emulated.
     */
    public void tick(int elapsedCycles) {
        while (elapsedCycles > 0 && currentCycle < TRANSFER_CYCLES) {
            if (currentCycle > 0) {
                memory.write(AddressUtils.OAM_ADDRESS_START + currentCycle - 1, readValue);
            }

            if (currentCycle < TRANSFER_CYCLES) {
                readValue = memory.read(sourceAddress + currentCycle);
            }

            // Every DMA cycle takes 4 processor cycles.
            elapsedCycles -= 4;
            currentCycle++;
        }
    }

}
