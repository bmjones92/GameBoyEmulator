package org.guide.gameboy.cartridge;

/**
 * Specifies the types of system a cartridge will run on based on the functionality they use.
 *
 * @author Brendan Jones
 */
public enum CGBSupportCode {

    /**
     * Indicates that a {@link Cartridge} does not use CGB functions, but operates
     * on both CGB and DMG hardware.
     */
    INCOMPATIBLE(true, false),

    /**
     * Indicates that a {@link Cartridge} uses CGB functions, and operates with both
     * CGB and DMG hardware.
     */
    COMPATIBLE(true, true),

    /**
     * Indications that a {@link Cartridge} uses CGB functions, but will only
     * operate on Game Boy Color hardware. If a user attempts to play this on a Game
     * Boy, a screen must be displayed telling the user that the game must be played
     * on a Game Boy Color.
     */
    EXCLUSIVE(false, true);

    /**
     * Whether the cartridge will run on DMG hardware.
     */
    private final boolean supportsDMG;

    /**
     * Whether the cartridge will run on CGB hardware.
     */
    private final boolean supportsCGB;

    /**
     * Creates a new CGBSupportCode instance.
     *
     * @param supportDMG Whether the cartridge can run on DMG hardware.
     * @param supportCGB Whether the cartridge can run on CGB hardware.
     */
    CGBSupportCode(boolean supportDMG, boolean supportCGB) {
        this.supportsDMG = supportDMG;
        this.supportsCGB = supportCGB;
    }

    /**
     * Gets whether the cartridge is compatible with DMG hardware.
     *
     * @return Whether the cartridge is compatible with DMG hardware.
     */
    public boolean supportsDMG() {
        return supportsDMG;
    }

    /**
     * Gets whether the cartridge is compatible with CGB hardware.
     *
     * @return Whether the cartridge is compatible with CGB hardware.
     */
    public boolean supportsCGB() {
        return supportsCGB;
    }

}
