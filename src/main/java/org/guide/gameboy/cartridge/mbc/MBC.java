package org.guide.gameboy.cartridge.mbc;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.cartridge.CartridgeHeader;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.MemoryBank;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.util.AddressUtils;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A Memory Bank Controller is an optional piece of hardware present on game cartridges. It is responsible for swapping
 * sections of the cartridge into and out of system memory, and can support optional components such as batteries,
 * rumble motors, and real time clocks.
 *
 * @author Brendan Jones
 */
public abstract class MBC extends AddressSpace implements SerializableComponent {

    /**
     * The size of the ROM banks.
     */
    private static final int ROM_BANK_SIZE = 0x4000;

    /**
     * The size of the RAM banks.
     */
    private static final int RAM_BANK_SIZE = 0x2000;

    /**
     * The ROM banks for the cartridge. In real hardware, up to 512 banks
     * are supported.
     */
    protected final MemoryBank romBanks;

    /**
     * The RAM banks for the cartridge. In real hardware, up to 16 banks
     * are supported.
     */
    protected final MemoryBank ramBanks;

    /**
     * The ROM data being loaded.
     */
    private final byte[] rom;

    /**
     * Whether RAM access is currently enabled.
     */
    private boolean isRAMEnabled;

    /**
     * Creates a new controller instance.
     *
     * @param memory The memory component.
     * @param cart   The cartridge.
     * @param rom    The ROM data to manage.
     */
    public MBC(Memory memory, Cartridge cart, byte[] rom) {
        super(memory);
        this.rom = Objects.requireNonNull(rom);

        this.romBanks = new MemoryBank(memory, false, false, AddressUtils.CROM_ADDRESS_START, rom.length / ROM_BANK_SIZE, ROM_BANK_SIZE);
        this.ramBanks = createRamBanks(memory, cart.getHeader());
    }

    /**
     * Resets the controller to its initial state.
     */
    public void reset() {
        this.isRAMEnabled = false;

        romBanks.reset(rom);
        if (ramBanks != null) {
            ramBanks.reset(null);
        }
    }

    @Override
    public void write(int address, int value) {
        switch (address & 0xE000) {
            case 0x0000:
                if (ramBanks != null) {
                    this.isRAMEnabled = (value & 0xF) == 0xA;
                }
                break;
            case 0xA000:
                if (isRAMEnabled) {
                    memory.set(address, value);
                }
                break;
        }
    }

    @Override
    public int read(int address) {
        if (!isRAMEnabled && AddressUtils.isAddressCRAM(address)) {
            return 0xFF;
        }
        return memory.get(address);
    }

    @Override
    public void serialize(ByteBuffer out) {
        SerializableComponent.writeIntegrityCheck(out);

        out.put((byte) (isRAMEnabled ? 1 : 0));

        romBanks.serialize(out);
        if (ramBanks != null) {
            ramBanks.serialize(out);
        }
    }

    @Override
    public void deserialize(ByteBuffer in) {
        SerializableComponent.verifyIntegrityCheck(in, "MBC::Start");

        this.isRAMEnabled = (in.get() & 0xFF) == 1;

        romBanks.deserialize(in);
        if (ramBanks != null) {
            ramBanks.deserialize(in);
        }
    }

    /**
     * Creates a {@code MemoryBank} for the Cartridge RAM if any exists. Otherwise returns null.
     *
     * @param memory The {@code Memory} object to bind the banks to.
     * @param header The {@code CartridgeHeader} for this controller.
     * @return The newly created {@code MemoryBank}, or null if no RAM is installed on the cartridge.
     */
    protected MemoryBank createRamBanks(Memory memory, CartridgeHeader header) {
        int size = header.getRAMSize();
        if (!header.hasSRAM() || size == 0) {
            return null;
        }
        return new MemoryBank(memory, true, true, AddressUtils.CRAM_ADDRESS_START, size / RAM_BANK_SIZE, RAM_BANK_SIZE);
    }

    /**
     * Checks whether {@code RAM} is currently enabled for reading and writing.
     *
     * @return {@code true} if RAM is enabled, {@code false} otherwise.
     */
    protected boolean isRAMEnabled() {
        return isRAMEnabled;
    }

}
