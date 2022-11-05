package org.guide.gameboy.cartridge.mbc;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.processor.interrupts.memory.Memory;

import java.nio.ByteBuffer;

/**
 * Represents a "Type 5" memory controller. A specification of this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Memory_Bank_Controllers#MBC5_.28max_8MByte_ROM_and.2For_128KByte_RAM.29">
 * here
 * </a>
 *
 * @author Brendan Jones
 */
public class MBC5 extends MBC implements SerializableComponent {

    /**
     * Whether the cartridge has rumble support.
     */
    private final boolean hasRumble;

    /**
     * The current ROM bank. This is a 9-bit integer and requires two writes to set it.
     */
    private int romBankBits;

    /**
     * Creates a new Memory Bank Controller.
     *
     * @param memory The memory component.
     * @param cart   The cartridge.
     * @param rom    The rom data.
     */
    public MBC5(Memory memory, Cartridge cart, byte[] rom) {
        super(memory, cart, rom);
        this.hasRumble = cart.getHeader().hasRumble();
    }

    @Override
    public void reset() {
        super.reset();
        this.romBankBits = 0x01;
    }

    @Override
    public void write(int address, int value) {
        switch (address & 0xF000) {
            case 0x2000 -> onWriteROMBankLow(value);
            case 0x3000 -> onWriteROMBankHigh(value);
            case 0x4000, 0x5000 -> onWriteRamBankRumble(value);
            default -> super.write(address, value);
        }
    }

    @Override
    public void serialize(ByteBuffer out) {
        super.serialize(out);

        out.putShort((short) romBankBits);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        super.deserialize(in);

        this.romBankBits = in.getShort() & 0xFFFF;
    }

    /**
     * Writes the lower 8 bits of the selected ROM bank.
     *
     * @param value The lower 8-bits of the ROM bank.
     */
    private void onWriteROMBankLow(int value) {
        this.romBankBits = (romBankBits & 0x0100) | (value & 0xFF);
        romBanks.setActiveBank(romBankBits);
    }

    /**
     * Writes the upper bit of the ROM bank.
     *
     * @param value The upper bit of the ROM bank.
     */
    private void onWriteROMBankHigh(int value) {
        this.romBankBits = ((value & 0x01) << 8) | (romBankBits & 0xFF);
        romBanks.setActiveBank(romBankBits);
    }

    /**
     * Writes the RAM bank number. If the cartridge has rumble support, but 3 determines if rumble is enabled
     * or disabled.
     *
     * @param value The RAM bank number
     */
    private void onWriteRamBankRumble(int value) {
        var bankNumber = value & 0xF;
        if (hasRumble) {
            bankNumber = (value & 0x7);

            // TODO If bit 3 is set, then enable rumble. Otherwise disable rumble.
        }

        if (ramBanks != null) {
            ramBanks.setActiveBank(bankNumber);
        }
    }

}
