package org.guide.util;

/**
 * Collection of utilities for working with addresses.
 *
 * @author Brendan Jones
 */
public class AddressUtils {

    /**
     * The size of the memory map.
     */
    public static final int MEMORY_MAP_SIZE = 0xFFFF;

    /**
     * The starting address for the OAM section of memory.
     */
    public static final int OAM_ADDRESS_START = 0xFE00;

    /**
     * The ending address for the OAM section of memory.
     */
    public static final int OAM_ADDRESS_END = 0xFE9F;

    /**
     * The starting address for the VRAM section of memory.
     */
    public static final int VRAM_ADDRESS_START = 0x8000;

    /**
     * The ending address for the VRAM section of memory.
     */
    public static final int VRAM_ADDRESS_END = 0x9FFF;

    /**
     * The starting address for the HRAM section of memory.
     */
    public static final int HRAM_ADDRESS_START = 0xFF80;

    /**
     * The ending address for the HRAM section of memory.
     */
    public static final int HRAM_ADDRESS_END = 0xFFFE;

    /**
     * The starting address for the IO section of memory.
     */
    public static final int IO_ADDRESS_START = 0xFF00;

    /**
     * The ending address for the IO section of memory.
     */
    public static final int IO_ADDRESS_END = 0xFF7F;

    /**
     * The starting address for the CROM section of memory.
     */
    public static final int CROM_ADDRESS_START = 0x0000;

    /**
     * The ending address for the CROM section of memory.
     */
    public static final int CROM_ADDRESS_END = 0x7FFF;

    /**
     * The starting address for the CRAM section of memory.
     */
    public static final int CRAM_ADDRESS_START = 0xA000;

    /**
     * The ending address for the CRAM section of memory.
     */
    public static final int CRAM_ADDRESS_END = 0xBFFF;

    /**
     * The address of the <i>controller input state</i> register.
     */
    public static final int P1 = 0xFF00;

    /**
     * The address of the <i>serial transfer</i> register.
     */
    public static final int SB = 0xFF01;

    /**
     * The address of the <i>serial control</i> register.
     */
    public static final int SC = 0xFF02;

    /**
     * The address of the <i>divider</i> register.
     */
    public static final int DIV = 0xFF04;

    /**
     * The address of the <i>timer</i> register.
     */
    public static final int TIMA = 0xFF05;

    /**
     * The address of the <i>timer modulo</i> register.
     */
    public static final int TMA = 0xFF06;

    /**
     * The address of the <i>timer control</i> register.
     */
    public static final int TAC = 0xFF07;

    /**
     * The address of the <i>interrupt request</i> register.
     */
    public static final int IF = 0xFF0F;

    /**
     * The address of the <i>NR10</i> register.
     */
    public static final int NR10 = 0xFF10;

    /**
     * The address of the <i>NR11</i> register.
     */
    public static final int NR11 = 0xFF11;

    /**
     * The address of the <i>NR12</i> register.
     */
    public static final int NR12 = 0xFF12;

    /**
     * The address of the <i>NR13</i> register.
     */
    public static final int NR13 = 0xFF13;

    /**
     * The address of the <i>NR14</i> register.
     */
    public static final int NR14 = 0xFF14;

    /**
     * The address of the <i>NR21</i> register.
     */
    public static final int NR21 = 0xFF16;

    /**
     * The address of the <i>NR22</i> register.
     */
    public static final int NR22 = 0xFF17;

    /**
     * The address of the <i>NR23</i> register.
     */
    public static final int NR23 = 0xFF18;

    /**
     * The address of the <i>NR24</i> register.
     */
    public static final int NR24 = 0xFF19;

    /**
     * The address of the <i>NR30</i> register.
     */
    public static final int NR30 = 0xFF1A;

    /**
     * The address of the <i>NR31</i> register.
     */
    public static final int NR31 = 0xFF1B;

    /**
     * The address of the <i>NR32</i> register.
     */
    public static final int NR32 = 0xFF1C;

    /**
     * The address of the <i>NR33</i> register.
     */
    public static final int NR33 = 0xFF1D;

    /**
     * The address of the <i>NR34</i> register.
     */
    public static final int NR34 = 0xFF1E;

    /**
     * The address of the <i>NR41</i> register.
     */
    public static final int NR41 = 0xFF20;

    /**
     * The address of the <i>NR42</i> register.
     */
    public static final int NR42 = 0xFF21;

    /**
     * The address of the <i>NR43</i> register.
     */
    public static final int NR43 = 0xFF22;

    /**
     * The address of the <i>NR44</i> register.
     */
    public static final int NR44 = 0xFF23;

    /**
     * The address of the <i>NR50</i> register.
     */
    public static final int NR50 = 0xFF24;

    /**
     * The address of the <i>NR51</i> register.
     */
    public static final int NR51 = 0xFF25;

    /**
     * The address of the <i>NR52</i> register.
     */
    public static final int NR52 = 0xFF26;

    /**
     * The address of the <i>LCD Control</i> register.
     */
    public static final int LCDC = 0xFF40;

    /**
     * The address of the <i>LCD Status</i> register.
     */
    public static final int STAT = 0xFF41;

    /**
     * The address of the <i>Scroll Y</i> register.
     */
    public static final int SCY = 0xFF42;

    /**
     * The address of the <i>Scroll X</i> register.
     */
    public static final int SCX = 0xFF43;

    /**
     * The address of the <i>LCDC y-coordinate</i> register.
     */
    public static final int LY = 0xFF44;

    /**
     * The address of the <i>LY Compare</i> register.
     */
    public static final int LYC = 0xFF45;

    /**
     * The address of the <i>DMA Transfer</i> register.
     */
    public static final int DMA = 0xFF46;

    /**
     * The address of the <i>BG Palette Data</i> register.
     */
    public static final int BGP = 0xFF47;

    /**
     * The address of the <i>Object Palette Data 0</i> register.
     */
    public static final int OBP0 = 0xFF48;

    /**
     * The address of the <i>Object Palette Data 1</i> register.
     */
    public static final int OBP1 = 0xFF49;

    /**
     * The address of the <i>Window y-coordinate</i> register.
     */
    public static final int WY = 0xFF4A;

    /**
     * The address of the <i>Window x-coordinate</i> register.
     */
    public static final int WX = 0xFF4B;

    /**
     * The address of the <i>CPU speed switching</i> register.
     */
    public static final int KEY1 = 0xFF4D;

    /**
     * The address of the <i>VRAM bank specification</i> register.
     */
    public static final int VBK = 0xFF4F;

    /**
     * The address of the <i>HDMA higher-order source</i> register.
     */
    public static final int HDMA1 = 0xFF51;

    /**
     * The address of the <i>HDMA lower-order source</i> register.
     */
    public static final int HDMA2 = 0xFF52;

    /**
     * The address of the <i>HDMA higher-order destination</i> register.
     */
    public static final int HDMA3 = 0xFF53;

    /**
     * The address of the <i>HDMA lower-order destination</i> register.
     */
    public static final int HDMA4 = 0xFF54;

    /**
     * The address of the <i>HDMA control</i> register.
     */
    public static final int HDMA5 = 0xFF55;

    /**
     * The address of the <i>infrared communication port</i> register.
     */
    public static final int RP = 0xFF56;

    /**
     * The address of the <i>BG color palette specification</i> register.
     */
    public static final int BCPS = 0xFF68;

    /**
     * The address of the <i>BG color palette data</i> register.
     */
    public static final int BCPD = 0xFF69;

    /**
     * The address of the <i>OBJ color palette specification</i> register.
     */
    public static final int OCPS = 0xFF6A;

    /**
     * The address of the <i>OBJ color palette data</i> register.
     */
    public static final int OCPD = 0xFF6B;

    /**
     * The address of the <i>WRAM bank specification</i> register.
     */
    public static final int SVBK = 0xFF70;

    /**
     * The address of the <i>interrupt enable</i> register.
     */
    public static final int IE = 0xFFFF;

    /**
     * Checks whether the specified address exists in the OAM section of memory.
     *
     * @param address The address to check.
     * @return Whether the address is the OAM section of memory.
     */
    public static boolean isAddressOAM(int address) {
        return isAddressInRange(address, OAM_ADDRESS_START, OAM_ADDRESS_END);
    }

    /**
     * Checks whether the specified address exists in the VRAM section of memory.
     *
     * @param address The address to check.
     * @return Whether the address is the VRAM section of memory.
     */
    public static boolean isAddressVRAM(int address) {
        return isAddressInRange(address, VRAM_ADDRESS_START, VRAM_ADDRESS_END);
    }

    /**
     * Checks whether the specified address exists in the HRAM section of memory.
     *
     * @param address The address to check.
     * @return Whether the address is the HRAM section of memory.
     */
    public static boolean isAddressHRAM(int address) {
        return isAddressInRange(address, HRAM_ADDRESS_START, HRAM_ADDRESS_END);
    }

    /**
     * Checks whether the specified address exists in the IO section of memory.
     *
     * @param address The address to check.
     * @return Whether the address is the IO section of memory.
     */
    public static boolean isAddressIO(int address) {
        return isAddressInRange(address, IO_ADDRESS_START, IO_ADDRESS_END);
    }

    /**
     * Checks whether the specified address exists in the CROM section of memory.
     *
     * @param address The address to check.
     * @return Whether the address is the CROM section of memory.
     */
    public static boolean isAddressCROM(int address) {
        return isAddressInRange(address, CROM_ADDRESS_START, CROM_ADDRESS_END);
    }

    /**
     * Checks whether the specified address exists in the CRAM section of memory.
     *
     * @param address The address to check.
     * @return Whether the address is the CRAM section of memory.
     */
    public static boolean isAddressCRAM(int address) {
        return isAddressInRange(address, CRAM_ADDRESS_START, CRAM_ADDRESS_END);
    }

    /**
     * Checks whether the address is in the specified range.
     *
     * @param address The address to check.
     * @param low     The low end of the range.
     * @param high    The high end of the range.
     * @return Whether the address is in the range.
     */
    private static boolean isAddressInRange(int address, int low, int high) {
        address &= 0xFFFF;
        return (address >= low && address <= high);
    }

    /**
     * Gets the address of a specified sprite.
     *
     * @param spriteIndex The sprite index.
     * @return The sprite's base address.
     */
    public static int getSpriteAddress(int spriteIndex) {
        return OAM_ADDRESS_START + (spriteIndex * 4);
    }

    /**
     * Checks if the specified address exists in a list of addresses.
     *
     * @param address The address to check for.
     * @param others  The list to search.
     * @return Whether the list contains the address.
     */
    public static boolean is(int address, int... others) {
        for (var other : others) {
            if (address == other) {
                return true;
            }
        }
        return false;
    }

    /**
     * Not used since this is a utility class.
     */
    private AddressUtils() {
    }

}
