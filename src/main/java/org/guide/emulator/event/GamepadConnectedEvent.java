package org.guide.emulator.event;

import org.guide.emulator.input.Gamepad;

/**
 * An event that fires whenever a gamepad is connected or disconnected from the system.
 *
 * @param gamepad   The gamepad.
 * @param connected Whether the gamepad was connected or disconnected.
 * @author Brendan Jones
 */
public record GamepadConnectedEvent(Gamepad gamepad, boolean connected) {
}
