package org.guide.emulator.event;

import org.guide.emulator.Window;

/**
 * An event that fires whenever a window attempts to close.
 *
 * @param window The window.
 * @author Brendan Jones
 */
public record WindowCloseEvent(Window window) {
}
