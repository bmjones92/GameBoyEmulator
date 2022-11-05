package org.guide.emulator.input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

/**
 * An emulated axis transforms an axis value to button states.
 *
 * @author Brendan Jones
 */
public record EmulatedAxis(int axis, int negativeButton, int positiveButton) {

    /**
     * Gets the emulated button for this axis based on its current state.
     *
     * @param value    The value of the axis.
     * @param deadzone The deadzone.
     * @return The emulated button, or GLFW_KEY_UNKNOWN if the axis is not active.
     */
    public int chooseButtonForState(float value, float deadzone) {
        final var isInactive = Math.abs(value) < deadzone;
        if (isInactive) {
            return GLFW_KEY_UNKNOWN;
        }

        return value < 0 ? negativeButton : positiveButton;
    }

}
