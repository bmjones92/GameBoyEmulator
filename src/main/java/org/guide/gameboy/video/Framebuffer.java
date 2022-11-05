package org.guide.gameboy.video;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

/**
 * Implements a simple framebuffer that manages the pixel data for a frame.
 *
 * @author Brendan Jones
 */
public class Framebuffer {

    /**
     * The width of the framebuffer.
     */
    private final int width;

    /**
     * The height of the framebuffer.
     */
    private final int height;

    /**
     * The pixel data.
     */
    private final ByteBuffer pixels;

    /**
     * Creates a new Framebuffer instance.
     *
     * @param width  The width of the framebuffer.
     * @param height The height of the framebuffer.
     */
    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = BufferUtils.createByteBuffer(width * height * 3);
    }

    /**
     * Sets the color of all pixels.
     *
     * @param rgb The color to fill.
     */
    public void fill(int rgb) {
        final var r = (byte) ((rgb >> 16) & 0xFF);
        final var g = (byte) ((rgb >> 8) & 0xFF);
        final var b = (byte) (rgb & 0xFF);

        for (var i = 0; i < pixels.capacity(); i += 3) {
            pixels.put(i, r);
            pixels.put(i + 1, g);
            pixels.put(i + 2, b);
        }
    }

    /**
     * Sets the color of a single pixel.
     *
     * @param x   The x coordinate of the pixel.
     * @param y   The y coordinate of the pixel.
     * @param rgb The color to set the pixel to.
     */
    public void setPixel(int x, int y, int rgb) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new RuntimeException("Invalid pixel: " + x + ", " + y);
        }

        final var index = ((y * width) + x) * 3;
        pixels.put(index, (byte) ((rgb >> 16) & 0xFF));
        pixels.put(index + 1, (byte) ((rgb >> 8) & 0xFF));
        pixels.put(index + 2, (byte) (rgb & 0xFF));
    }

    /**
     * Sets the color of a single pixel.
     *
     * @param x     The x coordinate of the pixel.
     * @param y     The y coordinate of the pixel.
     * @param color The color to set the pixel to.
     */
    public void setPixel(int x, int y, PaletteColor color) {
        setPixel(x, y, color.getColor24());
    }

    /**
     * Gets the width of the framebuffer.
     *
     * @return The width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the framebuffer.
     *
     * @return The height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the pixel data.
     *
     * @return The pixel data.
     */
    public ByteBuffer getPixels() {
        return pixels;
    }

}
