package org.guide.gameboy.cartridge;

import org.guide.gameboy.cartridge.mbc.MBCType;
import org.guide.util.BitUtils;

/**
 * A Cartridge Header contains information about a loaded cartridge.
 *
 * @author Brendan Jones
 */
public class CartridgeHeader {

    /**
     * The human-readable title. This can have a maximum length of 16 characters on a DMG cartridge, and 11 characters
     * on a CGB-compatible cartridge.
     */
    private final String title;

    /**
     * The CGB support code.
     */
    private final CGBSupportCode cgbSupport;

    /**
     * The manufacturer code for this cartridge.
     */
    private final String manufacturerCode;

    /**
     * Whether the game supports SGB functionality.
     */
    private final boolean sgbSupport;

    /**
     * Specifies which hardware exists on the cartridge. This determines which memory bank controller to use, and
     * whether certain optional hardware such as batteries, RAM, rumble, and real time clocks are present.
     */
    private final int type;

    /**
     * The type of memory bank controller the cartridge uses.
     */
    private MBCType mbcType;

    /**
     * Whether the cartridge has a real time clock.
     */
    private boolean hasRTC;

    /**
     * Whether the cartridge has rumble motors.
     */
    private boolean hasRumble;

    /**
     * Whether the cartridge has RAM.
     */
    private boolean hasSRAM;

    /**
     * Whether the cartridge has a battery.
     */
    private boolean hasBattery;

    /**
     * The size of the cartridge ROM.
     */
    private final int romSize;

    /**
     * The size of the cartridge RAM.
     */
    private final int ramSize;

    /**
     * The destination code.
     */
    private final DestinationCode destination;

    /**
     * The game version number.
     */
    private final int version;

    /**
     * 8-bit checksum for the header segment of the cartridge ROM.
     */
    private final int headerChecksum;

    /**
     * 16-bit checksum for the entire cartridge ROM.
     */
    private final int globalChecksum;

    /**
     * Creates a new cartridge header from the specified data.
     *
     * @param data The cartridge data
     * @throws CartridgeException If the provided data does not contain a valid cartridge header.
     */
    public CartridgeHeader(byte[] data) throws CartridgeException {
        this.cgbSupport = readCGBSupportCode(data);
        this.sgbSupport = readSGBSupportCode(data);
        this.title = readTitle(data);
        this.manufacturerCode = "";
        this.type = readCartridgeType(data);
        this.romSize = readROMSize(data);
        this.ramSize = readRAMSize(data);
        this.destination = readDestinationCode(data);
        this.version = data[0x14C] & 0xFF;
        this.headerChecksum = data[0x14D] & 0xFF;
        this.globalChecksum = ((data[0x14E] << 8) | data[0x14F]) & 0xFFFF;
    }

    /**
     * Reads the CGB support code.
     *
     * @param data The header data.
     * @return The support code.
     * @throws CartridgeException If the header has an unrecognized support code.
     */
    private CGBSupportCode readCGBSupportCode(byte[] data) throws CartridgeException {
        final var code = data[0x143] & 0xFF;
        return switch (code) {
            case 0x00 -> CGBSupportCode.INCOMPATIBLE;
            case 0x80 -> CGBSupportCode.COMPATIBLE;
            case 0xC0 -> CGBSupportCode.EXCLUSIVE;
            default -> throw CartridgeException.format("Cartridge has invalid CGB support code: 0x%02X", code);
        };
    }

    /**
     * Reads the SGB support code.
     *
     * @param data The header data.
     * @return The support code.
     * @throws CartridgeException If the header has an unrecognized support code.
     */
    private boolean readSGBSupportCode(byte[] data) throws CartridgeException {
        final var code = data[0x146] & 0xFF;
        return switch (code) {
            case 0x00 -> false;
            case 0x03 -> true;
            default -> throw CartridgeException.format("Cartridge has invalid SGB support code: 0x%02X", code);
        };
    }

    /**
     * Reads the title of the game.
     *
     * @param data The header data.
     * @return The game title.
     */
    private String readTitle(byte[] data) {
        final var length = (cgbSupport != CGBSupportCode.INCOMPATIBLE) ? 11 : 16;

        final var b = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            char ch = (char) (data[0x134 + i] & 0xFF);
            if (ch != 0) {
                b.append(ch);
            }
        }
        return b.toString();
    }

    /**
     * Reads the cartridge type and sets which types of hardware the cartridge contains.
     *
     * @param data The header data.
     * @return The cartridge type.
     * @throws CartridgeException If the header has an unrecognized type.
     */
    private int readCartridgeType(byte[] data) throws CartridgeException {
        final var type = data[0x147] & 0xFF;
        switch (type) {
            case 0x00 -> this.mbcType = MBCType.None;
            case 0x01 -> this.mbcType = MBCType.Type1;
            case 0x02 -> {
                this.mbcType = MBCType.Type1;
                this.hasSRAM = true;
            }
            case 0x03 -> {
                this.mbcType = MBCType.Type1;
                this.hasSRAM = true;
                this.hasBattery = true;
            }
            case 0x05 -> this.mbcType = MBCType.Type2;
            case 0x06 -> {
                this.mbcType = MBCType.Type2;
                this.hasBattery = true;
            }
            case 0x08 -> {
                this.mbcType = MBCType.None;
                this.hasSRAM = true;
            }
            case 0x09 -> {
                this.mbcType = MBCType.None;
                this.hasSRAM = true;
                this.hasBattery = true;
            }
            case 0x0F -> {
                this.mbcType = MBCType.Type3;
                this.hasRTC = true;
                this.hasBattery = true;
            }
            case 0x10 -> {
                this.mbcType = MBCType.Type3;
                this.hasRTC = true;
                this.hasSRAM = true;
                this.hasBattery = true;
            }
            case 0x11 -> this.mbcType = MBCType.Type3;
            case 0x12 -> {
                this.mbcType = MBCType.Type3;
                this.hasSRAM = true;
            }
            case 0x13 -> {
                this.mbcType = MBCType.Type3;
                this.hasSRAM = true;
                this.hasBattery = true;
            }
            case 0x19 -> this.mbcType = MBCType.Type5;
            case 0x1A -> {
                this.mbcType = MBCType.Type5;
                this.hasSRAM = true;
            }
            case 0x1B -> {
                this.mbcType = MBCType.Type5;
                this.hasSRAM = true;
                this.hasBattery = true;
            }
            case 0x1C -> {
                this.mbcType = MBCType.Type5;
                this.hasRumble = true;
            }
            case 0x1D -> {
                this.mbcType = MBCType.Type5;
                this.hasRumble = true;
                this.hasSRAM = true;
            }
            case 0x1E -> {
                this.mbcType = MBCType.Type5;
                this.hasRumble = true;
                this.hasSRAM = true;
                this.hasBattery = true;
            }
            default -> throw CartridgeException.format("Unsupported cartridge type: 0x%02X", type);
        }
        return type;
    }

    /**
     * Reads the size of the ROM.
     *
     * @param data The header data.
     * @return The ROM size.
     * @throws CartridgeException If the header has an unrecognized ROM size.
     */
    private int readROMSize(byte[] data) throws CartridgeException {
        int size = data[0x148] & 0xFF;
        return switch (size) {
            case 0x00 -> BitUtils.fromKBits(256);
            case 0x01 -> BitUtils.fromKBits(512);
            case 0x02 -> BitUtils.fromMBits(1);
            case 0x03 -> BitUtils.fromMBits(2);
            case 0x04 -> BitUtils.fromMBits(4);
            case 0x05 -> BitUtils.fromMBits(8);
            case 0x06 -> BitUtils.fromMBits(16);
            case 0x07 -> BitUtils.fromMBits(32);
            case 0x08 -> BitUtils.fromMBits(64);
            default -> throw CartridgeException.format("Cartridge has unsupported ROM size: 0x%02X", size);
        };
    }

    /**
     * Reads the amount of RAM installed on the cartridge.
     *
     * @param data The header data.
     * @return The RAM size.
     * @throws CartridgeException If the header has an unrecognized RAM size.
     */
    private int readRAMSize(byte[] data) throws CartridgeException {
        final var size = data[0x149] & 0xFF;
        return switch (size) {
            case 0x00 -> 0;
            case 0x01 -> BitUtils.fromKBits(16);
            case 0x02 -> BitUtils.fromKBits(64);
            case 0x03 -> BitUtils.fromKBits(256);
            case 0x04 -> BitUtils.fromMBits(1);
            default -> throw CartridgeException.format("Cartridge has unsupported RAM size: 0x%02X", size);
        };
    }

    /**
     * Reads the destination code.
     *
     * @param data The header data.
     * @return The destination code.
     * @throws CartridgeException If the header has an unrecognized destination code.
     */
    private DestinationCode readDestinationCode(byte[] data) throws CartridgeException {
        int code = data[0x14A] & 0xFF;
        return switch (code) {
            case 0x00 -> DestinationCode.JAPAN;
            case 0x01 -> DestinationCode.WORLDWIDE;
            default -> throw CartridgeException.format("Cartridge has invalid destination code: 0x%02X", code);
        };
    }

    /**
     * Gets the title of the cartridge.
     *
     * @return The cartridge title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets whether the cartridge uses CGB functionality.
     *
     * @return Whether the cartridge uses CGB functionality.
     */
    public boolean isCGB() {
        return cgbSupport.supportsCGB();
    }

    /**
     * Gets the CGB support code.
     *
     * @return The CGB support code.
     */
    public CGBSupportCode getCGBSupportCode() {
        return cgbSupport;
    }

    /**
     * Gets whether the cartridge uses SGB functionality.
     *
     * @return Whether the cartridge uses SGB functionality.
     */
    public boolean getSGBSupport() {
        return sgbSupport;
    }

    /**
     * Gets the manufacturer code for the cartridge.
     *
     * @return The manufacturor code.
     */
    public String getManufacturerCode() {
        return manufacturerCode;
    }

    /**
     * Gets the cartridge type.
     *
     * @return The cartridge type.
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the type of memory bank controller installed on the cartridge.
     *
     * @return The memory bank controller.
     */
    public MBCType getMBCType() {
        return mbcType;
    }

    /**
     * Gets whether a real time clock is installed on the cartridge.
     *
     * @return Whether the cartridge has a real time clock.
     */
    public boolean hasRTC() {
        return hasRTC;
    }

    /**
     * Gets whether a rumble motor is installed on the cartridge.
     *
     * @return Whether a rumble motor is installed.
     */
    public boolean hasRumble() {
        return hasRumble;
    }

    /**
     * Gets whether RAM is installed on the cartridge.
     *
     * @return Whether RAM is installed.
     */
    public boolean hasSRAM() {
        return hasSRAM;
    }

    /**
     * Gets whether a battery is installed on the cartridge.
     *
     * @return Whether a battery is installed.
     */
    public boolean hasBattery() {
        return hasBattery;
    }

    /**
     * Gets the amount of ROM installed on the cartridge.
     *
     * @return The ROM size.
     */
    public int getROMSize() {
        return romSize;
    }

    /**
     * Gets the amount of RAM available on the cartridge.
     *
     * @return The RAM size.
     */
    public int getRAMSize() {
        return ramSize;
    }

    /**
     * Gets the destination code for the cartridge.
     *
     * @return The destination code.
     */
    public DestinationCode getDestination() {
        return destination;
    }

    /**
     * Gets the version of the cartridge.
     *
     * @return The version.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Gets the checksum for the header section of the cartridge.
     *
     * @return The header checksum.
     */
    public int getHeaderChecksum() {
        return headerChecksum;
    }

    /**
     * Gets the checksum for the entirety of the cartridge ROM (excluding the checksum bits).
     *
     * @return The global checksum.
     */
    public int getGlobalChecksum() {
        return globalChecksum;
    }

}
