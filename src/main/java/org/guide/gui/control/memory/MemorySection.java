package org.guide.gui.control.memory;

import org.guide.util.StringUtils;

/**
 * Different sections of the Game Boy's memory map.
 *
 * @author Brendan Jones
 */
public enum MemorySection {

    /**
     * The static cartridge ROM bank 0 section.
     */
    CARTRIDGE_ROM_0("Cartridge ROM Bank 0", "cartridge-rom-0", 0x0000, 0x3FFF),

    /**
     * The switchable Cartridge ROM bank section.
     */
    CARTRIDGE_ROM_N("Cartridge ROM Bank N", "cartridge-rom-n", 0x4000, 0x7FFF),

    /**
     * The VRAM section.
     */
    VIDEO_RAM("Video RAM", "video-ram", 0x8000, 0x9FFF),

    /**
     * The cartridge RAM section.
     */
    CARTRIDGE_RAM("Cartridge RAM", "cartridge-ram", 0xA000, 0xBFFF),

    /**
     * The static WRAM bank 0 section.
     */
    WORK_RAM_0("Work RAM Bank 0", "work-ram-0", 0xC000, 0xCFFF),

    /**
     * The Switchable WRAM bank section.
     */
    WORK_RAM_N("Work RAM Bank N", "work-ram-n", 0xD000, 0xDFFF),

    /**
     * The Shadow RAM section.
     */
    SHADOW_RAM("Shadow RAM", "disabled", 0xE000, 0xFDFF),

    /**
     * The OAM section.
     */
    OBJECT_ATTRIBUTE_MEMORY("Object Attribute Memory", "oam", 0xFE00, 0xFE9F),

    /**
     * The unused section.
     */
    UNUSED("Unused", "disabled", 0xFEA0, 0xFEFF),

    /**
     * The IO registers section.
     */
    IO_REGISTERS("IO Registers", "io-registers", 0xFF00, 0xFF7F),

    /**
     * The HRAM section.
     */
    HIGH_RAM("High RAM", "high-ram", 0xFF80, 0xFFFE),

    /**
     * The interrupt registers section.
     */
    INTERRUPT_REGISTER("Interrupt Register", "interrupt-register", 0xFFFF, 0xFFFF);

    /**
     * The title of the section.
     */
    private final String title;

    /**
     * The css style class to apply to the section.
     */
    private final String styleClass;

    /**
     * The start address of the section.
     */
    private final int start;

    /**
     * The end address of the section.
     */
    private final int end;

    MemorySection(String title, String styleClass, int start, int end) {
        this.title = title;
        this.styleClass = "memory-block-" + styleClass;
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the title of this section.
     *
     * @return The title.
     */
    public String title() {
        return title;
    }

    /**
     * Gets the CSS style class of this section.
     *
     * @return The style class.
     */
    public String styleClass() {
        return styleClass;
    }

    /**
     * Gets the start address of this section.
     *
     * @return The start address.
     */
    public int startAddress() {
        return start;
    }

    /**
     * Gets the end address of this section.
     *
     * @return The end address.
     */
    public int endAddress() {
        return end;
    }

    @Override
    public String toString() {
        return title + " (" + StringUtils.getHex16(start) + '-' + StringUtils.getHex16(end) + ')';
    }

    /**
     * Gets the section corresponding to the specified address.
     *
     * @param address The address.
     * @return The section.
     */
    public static MemorySection getSectionForAddress(int address) {
        for (final var section : MemorySection.values()) {
            if (address >= section.start && address <= section.end) {
                return section;
            }
        }
        throw new IllegalArgumentException("Address is outside of valid address space: " + address);
    }

}