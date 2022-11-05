package org.guide.emulator.event;

import org.guide.emulator.input.Gamepad;

/**
 * An event that fires when a gamepad button is pressed or released.
 *
 * @param gamepad The gamepad.
 * @param button  The button.
 * @param pressed Whether the button is pressed or released.
 * @author Brendan Jones
 */
public record GamepadButtonEvent(Gamepad gamepad, int button, boolean pressed) {
}
