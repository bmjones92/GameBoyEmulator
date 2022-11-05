package org.guide.emulator.event;

import org.guide.emulator.Window;

/**
 * An event that fires whenever a window needs to be refreshed.
 *
 * @param window The window.
 * @author Brendan Jones
 */
public record WindowRefreshEvent(Window window) {
}
