package org.guide.gameboy.cartridge;

import org.guide.gameboy.cartridge.hardware.Battery;
import org.guide.gameboy.cartridge.hardware.RealTimeClock;
import org.guide.gameboy.cartridge.hardware.Rumble;
import org.guide.gameboy.cartridge.mbc.*;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.util.AddressUtils;

import java.util.Objects;

/**
 * Represents a game pak that can be loaded into the system. A cartridge contains metadata about the game, and is
 * responsible for managing the hardware that is present in the cartridge.
 *
 * @author Brendan Jones
 */
public class Cartridge extends AddressSpace {

    /**
     * The header data of the loaded cartridge.
     */
    private CartridgeHeader header;

    /**
     * The memory bank controller hardware installed on the cartridge.
     */
    private MBC mbc;

    /**
     * The battery hardware that is installed on the cartridge
     */
    private Battery battery;

    /**
     * The real time clock hardware installed on the cartridge.
     */
    private RealTimeClock rtc;

    /**
     * The rumble hardware installed on the cartridge.
     */
    private Rumble rumble;

    /**
     * Creates a new Cartridge instance.
     *
     * @param memory The game boy's memory component.
     */
    public Cartridge(Memory memory) {
        super(memory);

        memory.setAddressSpace(this, AddressUtils.CROM_ADDRESS_START, AddressUtils.CROM_ADDRESS_END);
        memory.setAddressSpace(this, AddressUtils.CRAM_ADDRESS_START, AddressUtils.CRAM_ADDRESS_END);

        this.mbc = null;
        this.battery = null;
        this.rtc = null;
        this.rumble = null;
    }

    /**
     * Resets the cartridge to its default memory state.
     */
    public void reset() {
        if (isLoaded()) {
            mbc.reset();
        }
    }

    @Override
    public void write(int address, int value) {
        if (isLoaded()) {
            mbc.write(address, value);
        }
    }

    @Override
    public int read(int address) {
        if (!isLoaded()) {
            return 0xFF;
        }
        return mbc.read(address);
    }

    /**
     * Loads the cartridge with the specified RAM data. This will initialize any cartridge-specific  any specific cartridge hardware.
     *
     * @param data The cartridge ROM.
     * @throws Exception If {@code data} is not a valid cartridge ROM.
     */
    public void load(byte[] data) throws CartridgeException, NoSuchMethodException {
        Objects.requireNonNull(data, "Cartridge data cannot be null.");

        // Parse and extract the cartridge header.
        this.header = new CartridgeHeader(data);

        // Create the new memory bank controller.
        final var type = header.getMBCType();
        this.mbc = switch (type) {
            case None -> new MBCNone(memory, this, data);
            case Type1 -> new MBC1(memory, this, data);
            case Type2 -> new MBC2(memory, this, data);
            case Type3 -> new MBC3(memory, this, data);
            case Type5 -> new MBC5(memory, this, data);
        };

        // Create the hardware components of the cartridge.
        this.battery = header.hasBattery() ? new Battery() : null;
        this.rtc = header.hasRTC() ? new RealTimeClock() : null;
        this.rumble = header.hasRumble() ? new Rumble() : null;
    }

    /**
     * Checks whether the cartridge has been loaded with data.
     *
     * @return Whether the cartridge is loaded.
     */
    public boolean isLoaded() {
        return (header != null);
    }

    /**
     * Gets the header of the current cartridge.
     *
     * @return The cartridge header, or null if no cartridge is loaded.
     */
    public CartridgeHeader getHeader() {
        return header;
    }

    /**
     * Gets the memory bank controller installed on this cartridge.
     *
     * @return The memory bank controller, or null if no cartridge is loaded.
     */
    public MBC getMBC() {
        return mbc;
    }

    /**
     * Gets the battery installed on this cartridge. Not all cartridges have batteries.
     *
     * @return The battery if a cartridge is loaded and contains a battery component, otherwise null.
     */
    public Battery getBattery() {
        return battery;
    }

    /**
     * Gets the real time clock installed on this cartridge. Not all cartridges have clocks.
     *
     * @return The real time clock if a cartridge is loaded and supports a clock component, otherwise null.
     * component.
     */
    public RealTimeClock getRTC() {
        return rtc;
    }

    /**
     * Gets the rumble hardware installed on this cartridge.
     *
     * @return The rumble hardware if a cartridge is loaded and contains a rumble component, otherwise null.
     */
    public Rumble getRumble() {
        return rumble;
    }

}
