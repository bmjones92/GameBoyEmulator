package org.guide.gameboy.video;

/**
 * Represents all possible modes that the LCD hardware can be in.
 *
 * @author Brendan Jones
 */
public enum LCDMode {

    /**
     * The <i>horizontal blank</i> mode indicates that the LCD controller has finished rendering a scanline and is in
     * the H-blank period. During this time, the LCD controller is idle and the CPU may access VRAM and OAM freely.
     */
    HBLANK(true, true, true),

    /**
     * The <i>vertical blank</i> mode indicates that the LCD controller has finished rendering a frame and is in the
     * H-blank period. During this time, the LCD controller is idle and the CPU may access VRAM and OAM freely.
     */
    VBLANK(true, true, true),

    /**
     * The <i>OAM Search</i> mode indicates that the LCD controller has started a new scanline and is currently
     */
    SEARCH(true, false, true),

    /**
     * Indicates that the LCD controller is transferring scanline data from memory to the LCD screen. During this time,
     * both OAM and VRAM cannot
     */
    TRANSFER(false, false, false);

    /**
     * Whether the VRAM address space is accessible while the LCD Controller is operating in this mode.
     */
    private final boolean isVRAMAccessible;

    /**
     * Whether the OAM address space is accessible while the LCD Controller is operating in this mode.
     */
    private final boolean isOAMAccessible;

    /**
     * Whether palette RAM can be accessed while the LCD controller is operating in this mode.
     */
    private final boolean isPaletteAccessible;

    /**
     * Creates a new LCDMode instance.
     *
     * @param isVRAMAccessible    Whether VRAM is accessible while in this mode.
     * @param isOAMAccessible     Whether OAM is accessible while in this mode.
     * @param isPaletteAccessible Whether palette memory is accessible while in this mode.
     */
    LCDMode(boolean isVRAMAccessible, boolean isOAMAccessible, boolean isPaletteAccessible) {
        this.isVRAMAccessible = isVRAMAccessible;
        this.isOAMAccessible = isOAMAccessible;
        this.isPaletteAccessible = isPaletteAccessible;
    }

    /**
     * Gets the interrupt bit for this mode.
     *
     * @return The interrupt bit.
     */
    public int getInterruptBit() {
        return ordinal() + 3;
    }

    /**
     * Gets whether VRAM is accessible while in this mode.
     *
     * @return Whether VRAM is accessible.
     */
    public boolean isVRAMAccessible() {
        return isVRAMAccessible;
    }

    /**
     * Gets whether OAM is accessible while in this mode.
     *
     * @return Whether OAM is accessible.
     */
    public boolean isOAMAccessible() {
        return isOAMAccessible;
    }

    /**
     * Gets whether palette memory is accessible while in this mode.
     *
     * @return Whether palette memory is accessible.
     */
    public boolean isPaletteAccessible() {
        return isPaletteAccessible;
    }

    /**
     * Gets the LCD mode from the STAT register.
     *
     * @param stat The STAT register value.
     * @return The LCD mode.
     */
    public static LCDMode get(int stat) {
        return LCDMode.values()[stat & 0x3];
    }

}
