package org.guide.emulator.event;

import org.guide.emulator.Window;

/**
 * An event that fires whenever a key is pressed or released.
 *
 * @param window The window that was focused when the event occurred.
 * @param key    The key.
 * @param state  The state of the key.
 * @author Brendan Jones
 */
public record KeyStateEvent(Window window, int key, int state) {
}
