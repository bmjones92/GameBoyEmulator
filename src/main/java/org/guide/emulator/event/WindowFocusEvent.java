package org.guide.emulator.event;

import org.guide.emulator.Window;

/**
 * An event that fires whenever a window gains or loses focus.
 *
 * @param window  The window.
 * @param focused Whether the window is focused.
 * @author Brendan Jones
 */
public record WindowFocusEvent(Window window, boolean focused) {
}
