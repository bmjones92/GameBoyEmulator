package org.guide.gameboy.cartridge.mbc;

import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.processor.interrupts.memory.Memory;

import java.nio.ByteBuffer;

/**
 * Represents a "Type 1" memory controller. A specification of this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Memory_Bank_Controllers#MBC1_.28max_2MByte_ROM_and.2For_32KByte_RAM.29">
 * here
 * </a>
 *
 * @author Brendan Jones
 */
public class MBC1 extends MBC {

    private enum BankMode {
        ROM, RAM
    }

    /**
     * The current bank mode.
     */
    private BankMode bankMode;

    /**
     * This 7-bit register stores the current RAM/ROM bank bits.
     */
    private int currentBankRegister;

    public MBC1(Memory memory, Cartridge cart, byte[] rom) {
        super(memory, cart, rom);
    }

    @Override
    public void reset() {
        super.reset();

        this.bankMode = BankMode.ROM;
        this.currentBankRegister = 0x01;
    }

    @Override
    public void write(int address, int value) {
        switch (address & 0xE000) {
            case 0x2000 -> onWriteROMBank(value);
            case 0x4000 -> onWriteMemoryBank(value);
            case 0x6000 -> onWriteMemoryMode(value);
            default -> super.write(address, value);
        }
    }

    @Override
    public void serialize(ByteBuffer out) {
        super.serialize(out);

        out.put((byte) bankMode.ordinal());
        out.put((byte) currentBankRegister);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        super.deserialize(in);

        this.bankMode = BankMode.values()[in.get() & 0xFF];
        this.currentBankRegister = in.get() & 0xFF;
    }

    /**
     * Selects the lower 5-bits of the ROM bank number. Writing a value of 0x00 to
     * this register will force it to 0x01.
     *
     * @param value The value to write to the register.
     */
    private void onWriteROMBank(int value) {
        value = Math.max(1, value & 0x1F);

        this.currentBankRegister = (currentBankRegister & 0x60) | value;

        switch (bankMode) {
            case ROM -> romBanks.setActiveBank(currentBankRegister);
            case RAM -> romBanks.setActiveBank(value);
        }
    }

    /**
     * Selects the upper 2-bits of the ROM/RAM bank numbers depending on the current
     * bank mode.
     *
     * @param value The value to write to the register.
     */
    private void onWriteMemoryBank(int value) {
        value &= 0x3;

        this.currentBankRegister = (value << 5) | (currentBankRegister & 0x1F);

        switch (bankMode) {
            case ROM -> romBanks.setActiveBank(currentBankRegister);
            case RAM -> ramBanks.setActiveBank(value);
        }
    }

    /**
     * Selects the current memory mode.
     *
     * @param value The value to write to the register.
     */
    private void onWriteMemoryMode(int value) {
        final var newMode = switch (value & 0x1) {
            case 0 -> BankMode.ROM;
            case 1 -> BankMode.RAM;
            default -> null;
        };

        if (newMode != bankMode) {
            this.bankMode = newMode;

            switch (bankMode) {
                case ROM -> {
                    romBanks.setActiveBank(currentBankRegister);
                    ramBanks.setActiveBank(0);
                }
                case RAM -> {
                    romBanks.setActiveBank(currentBankRegister & 0x1F);
                    ramBanks.setActiveBank((currentBankRegister >> 5) & 0x3);
                }
            }
        }
    }

}
