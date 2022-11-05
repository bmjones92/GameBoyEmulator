package org.guide.emulator.input;

import org.lwjgl.glfw.GLFWGamepadState;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.glfw.GLFW.glfwGetGamepadName;

/**
 * Represents a gamepad that is connected to the machine.
 *
 * @author Brendan Jones
 */
public class Gamepad {

    /**
     * The id of the gamepad.
     */
    private final int id;

    /**
     * The human-readable name of the gamepad.
     */
    private final String name;

    /**
     * The current state of the gamepad.
     */
    private final GLFWGamepadState state;

    /**
     * Creates a new Gamepad.
     *
     * @param id   The id of the gamepad.
     * @param name The name of the gamepad.
     */
    private Gamepad(int id, String name) {
        this.id = id;
        this.name = requireNonNull(name);
        this.state = GLFWGamepadState.create();
    }

    /**
     * Get the id of the gamepad.
     *
     * @return The id.
     */
    public int id() {
        return id;
    }

    /**
     * Get the human-readable name of the gamepad.
     *
     * @return The name.
     */
    public String name() {
        return name;
    }

    /**
     * Get the current state of the gamepad.
     *
     * @return The gamepad state.
     */
    public GLFWGamepadState state() {
        return state;
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * Creates a new Gamepad instance.
     *
     * @param id The gamepad id.
     * @return The newly created gamepad.
     */
    public static Gamepad create(int id) {
        final var name = glfwGetGamepadName(id);
        if (name == null) {
            throw new RuntimeException("Could not create gamepad.");
        }

        return new Gamepad(id, name);
    }

}
