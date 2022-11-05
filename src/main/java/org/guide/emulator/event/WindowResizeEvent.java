package org.guide.emulator.event;

import org.guide.emulator.Window;

/**
 * An event that fires whenever a window is resized.
 *
 * @param window The window.
 * @param width  The window's new width.
 * @param height The window's new height.
 * @author Brendan Jones
 */
public record WindowResizeEvent(Window window, int width, int height) {
}
