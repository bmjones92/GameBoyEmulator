package org.guide.gameboy.video.queue;

import org.guide.gameboy.SerializableComponent;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

/**
 * Represents an element within the {@link PixelQueue}.
 *
 * @author Brendan Jones
 */
public class PixelQueueEntry implements SerializableComponent {

    /**
     * The source of the pixel.
     */
    private PixelSource source;

    /**
     * The color of the pixel.
     */
    private int color;

    /**
     * The pallete that produced the pixel.
     */
    private int palette;

    /**
     * The x position of the sprite that produced this pixel.
     */
    private int spriteX;

    /**
     * The id of the sprite that produced this pixel.
     */
    private int spriteID;

    /**
     * Whether this pixel has priority.
     */
    private boolean hasPriority;

    /**
     * Creates a new PixelQueueEntry instance.
     */
    public PixelQueueEntry() {
        this.source = null;
        this.color = 0;
        this.palette = 0;
        this.hasPriority = false;
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.put((byte) source.ordinal());
        out.put((byte) color);
        out.put((byte) palette);
        out.put((byte) (hasPriority ? 1 : 0));

        if (source == PixelSource.SPRITE) {
            out.put((byte) spriteX);
            out.put((byte) spriteID);
        }
    }

    @Override
    public void deserialize(ByteBuffer in) {
        this.source = PixelSource.values()[in.get() & 0xFF];
        this.color = in.get() & 0xFF;
        this.palette = in.get() & 0xFF;
        this.hasPriority = (in.get() & 0xFF) == 1;

        if (source == PixelSource.SPRITE) {
            this.spriteX = in.get() & 0xFF;
            this.spriteID = in.get() & 0xFF;
        }
    }

    /**
     * Sets the data for this entry.
     *
     * @param source      The pixel's source.
     * @param color       The pixel's color.
     * @param palette     The pixel's palette.
     * @param hasPriority The pixel's priority.
     */
    public void set(PixelSource source, int color, int palette, boolean hasPriority) {
        this.source = requireNonNull(source);
        this.color = color & 0x3;
        this.palette = palette & 0x7;
        this.hasPriority = hasPriority;
    }

    /**
     * Sets the data for this entry.
     *
     * @param color       The pixel's color.
     * @param palette     The pixel's palette.
     * @param hasPriority Whether the pixel has priority.
     * @param spriteX     The x coordinate of the sprite.
     * @param spriteID    The id of the sprite.
     */
    public void set(int color, int palette, boolean hasPriority, int spriteX, int spriteID) {
        set(PixelSource.SPRITE, color, palette, hasPriority);
        this.spriteX = spriteX;
        this.spriteID = spriteID;
    }

    /**
     * Gets the source for this pixel.
     *
     * @return The source.
     */
    public PixelSource getSource() {
        return source;
    }

    /**
     * Gets the color of this pixel.
     *
     * @return The color.
     */
    public int getColor() {
        return color;
    }

    /**
     * Gets the palette of the pixel.
     *
     * @return The palette.
     */
    public int getPalette() {
        return palette;
    }

    /**
     * Gets whether this pixel is high priority.
     *
     * @return The pixel priority.
     */
    public boolean hasPriority() {
        return hasPriority;
    }

    /**
     * Gets the x coordinate of the sprite that produced this pixel.
     *
     * @return The sprite's x coordinate.
     */
    public int getSpriteX() {
        return spriteX;
    }

    /**
     * Gets the id of the sprite that produced this pixel.
     *
     * @return The sprite ID.
     */
    public int getSpriteID() {
        return spriteID;
    }

}
