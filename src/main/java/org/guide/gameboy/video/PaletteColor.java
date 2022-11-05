package org.guide.gameboy.video;

/**
 * Represents a color that can be displayed by the Game Boy's LCD. The native hardware uses 15-bit colors, so this class
 * provides an efficient way of converting colors to native 24-bit colors.
 *
 * @author Brendan Jones
 */
public class PaletteColor {

    /**
     * Lookup table for converting 5-bit color channels to 8-bit color channels.
     */
    private static final int[] CGB_TO_RGB = {
            0, 8, 16, 25, 33, 41, 49, 58, 66, 74, 82, 90, 99, 107, 115, 123,
            132, 140, 148, 156, 165, 173, 181, 189, 197, 206, 214, 222, 230, 239, 247, 255
    };

    /**
     * The default color palette.
     */
    public static final PaletteColor[] DEFAULT_PALETTE = {
            new PaletteColor(28, 28, 28),
            new PaletteColor(19, 19, 19),
            new PaletteColor(11, 11, 11),
            new PaletteColor(2, 2, 2),
    };

    /**
     * The 15-bit representation of the color.
     */
    private int color15;

    /**
     * The 24-bit representation of the color.
     */
    private int color24;

    /**
     * Whether the cached 24-bit representation of the color is stale and needs to be recalculated.
     */
    private boolean needCalculateColor24;

    /**
     * Creates a new PaletteColor.
     *
     * @param r The red channel (5-bits).
     * @param g The green channel (5-bits).
     * @param b The blue channel (5-bits).
     */
    public PaletteColor(int r, int g, int b) {
        this.color15 = (b << 10) | (g << 5) | r;
        this.needCalculateColor24 = true;
    }

    /**
     * Creates a new PaletteColor.
     *
     * @param color The source color.
     */
    public PaletteColor(PaletteColor color) {
        set(color);
    }

    /**
     * Sets this palette color to another color.
     *
     * @param color The color to set.
     */
    public void set(PaletteColor color) {
        this.color15 = color.color15;
        this.color24 = color.color24;
        this.needCalculateColor24 = color.needCalculateColor24;
    }

    /**
     * Sets this color to the specified 15-bit color.
     *
     * @param data The color data. Only the lowest 15 bits will be used.
     */
    public void set(int data) {
        this.color15 = 0x8000 | (data & 0xFFFF);
        this.needCalculateColor24 = true;
    }

    /**
     * Sets the high byte of the 15-bit color.
     *
     * @param data The color data. Only the lowest 7 bits will be used.
     */
    public void setHigh(int data) {
        this.color15 = 0x8000 | (data << 8) | (color15 & 0xFF);
        this.needCalculateColor24 = true;
    }

    /**
     * Sets the low byte of the 15-bit color.
     *
     * @param data The color data. Only the lowest 8 bits will be used.
     */
    public void setLow(int data) {
        this.color15 = (color15 & 0xFF00) | (data & 0xFF);
        this.needCalculateColor24 = true;
    }

    /**
     * Gets the 15-bit representation of the color.
     *
     * @return The 15-bit color.
     */
    public int getColor15() {
        return color15;
    }

    /**
     * Gets the 24-bit representation of the color.
     *
     * @return The 24-bit color.
     */
    public int getColor24() {
        // The 24-bit color is cached to improve performance.
        if (needCalculateColor24) {
            this.needCalculateColor24 = false;

            final var r = color15 & 0x1F;
            final var g = (color15 >> 5) & 0x1F;
            final var b = (color15 >> 10) & 0x1F;

            this.color24 = (CGB_TO_RGB[r] << 16) | (CGB_TO_RGB[g] << 8) | CGB_TO_RGB[b];
        }

        return color24;
    }

}
