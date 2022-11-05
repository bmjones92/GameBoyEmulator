package org.guide.emulator.input;

import org.guide.emulator.event.GamepadButtonEvent;
import org.guide.emulator.event.GamepadConnectedEvent;
import org.guide.util.delegate.EventDispatcher;
import org.guide.util.delegate.EventDispatcherHandle;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Manages event processing and dispatching for gamepads.
 *
 * @author Brendan Jones
 */
public class GamepadManager {

    /**
     * The emulated axes to process.
     */
    private static final EmulatedAxis[] EMULATED_AXES = {
            new EmulatedAxis(GLFW_GAMEPAD_AXIS_LEFT_X, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT),
            new EmulatedAxis(GLFW_GAMEPAD_AXIS_LEFT_Y, GLFW_GAMEPAD_BUTTON_DPAD_UP, GLFW_GAMEPAD_BUTTON_DPAD_DOWN)
    };

    /**
     * Gamepads that are currently connected to the machine.
     */
    private static final Map<Integer, Gamepad> gamepads = new HashMap<>();

    /**
     * Event delegate that fires whenever a gamepad is connected to the machine.
     */
    private static final EventDispatcher<GamepadConnectedEvent> gamepadConnectedEvent = new EventDispatcher<>();

    /**
     * Event that fires whenever a gamepad button is pressed or released.
     */
    private static final EventDispatcher<GamepadButtonEvent> gamepadButtonEvent = new EventDispatcher<>();

    private static volatile boolean isInitialized = false;

    public static void initialize() {
        if (isInitialized) {
            throw new RuntimeException("Gamepad Manager is already initialized.");
        }
        isInitialized = true;

        // Initialize connected gamepads.
        for (var id = 0; id <= GLFW_JOYSTICK_LAST; id++) {
            if (glfwJoystickPresent(id)) {
                onGamepadConnected(id, GLFW_CONNECTED);
            }
        }

        // Register the gamepad connection callback.
        glfwSetJoystickCallback(GamepadManager::onGamepadConnected);
    }

    /**
     * Called whenever a gamepad is connected or disconnected.
     *
     * @param jid   The id of the gamepad.
     * @param event The connection event type.
     */
    private static void onGamepadConnected(int jid, int event) {
        final var isConnected = event == GLFW_CONNECTED;

        Gamepad gamepad;
        if (isConnected) {
            gamepad = Gamepad.create(jid);
            gamepads.put(jid, gamepad);
        } else {
            gamepad = gamepads.remove(jid);
        }

        if (gamepad != null) {
            gamepadConnectedEvent.broadcast(new GamepadConnectedEvent(gamepad, isConnected));
        }
    }

    /**
     * Polls the input state for all connected gamepads and dispatches any input events that occurred.
     *
     * @param deadzone The deadzone for axis events.
     */
    public static void pollEvents(float deadzone) {
        // Defer event dispatching until all gamepads have been updated.
        final var events = new ArrayList<GamepadButtonEvent>();

        try (final var stack = MemoryStack.stackPush()) {
            final var state = GLFWGamepadState.malloc(stack);
            for (var gamepad : gamepads.values()) {
                glfwGetGamepadState(gamepad.id(), state);

                final var oldState = gamepad.state();

                // Generate any events for changes in button state.
                for (var button = 0; button <= GLFW_GAMEPAD_BUTTON_LAST; button++) {
                    final var isPressed = state.buttons(button) == GLFW_TRUE;
                    final var wasPressed = oldState.buttons(button) == GLFW_TRUE;
                    if (isPressed != wasPressed) {
                        events.add(new GamepadButtonEvent(gamepad, button, isPressed));
                    }
                }

                // Generate any events for changes in axis state.
                for (var axis : EMULATED_AXES) {
                    final var newValue = state.axes(axis.axis());
                    final var oldValue = oldState.axes(axis.axis());

                    // Some axes can emulate multiple buttons (e.g. D-pad left and D-pad right).
                    final var newButton = axis.chooseButtonForState(newValue, deadzone);
                    final var oldButton = axis.chooseButtonForState(oldValue, deadzone);

                    // Update the gamepad state if the old and new button states differ.
                    if (newButton != oldButton) {
                        // The old button state needs to be "released".
                        if (oldButton != GLFW_KEY_UNKNOWN) {
                            events.add(new GamepadButtonEvent(gamepad, oldButton, false));
                        }
                        // The new button state needs to be "pressed".
                        if (newButton != GLFW_KEY_UNKNOWN) {
                            events.add(new GamepadButtonEvent(gamepad, newButton, true));
                        }
                    }
                }

                // Update the gamepad state.
                oldState.set(state);
            }
        }

        // Now that all gamepads are updated, we can safely dispatch events.
        events.forEach(gamepadButtonEvent::broadcast);
    }

    /**
     * Registers a callback to be executed whenever a gamepad is connected or disconnected from the machine.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public static EventDispatcherHandle<GamepadConnectedEvent> bindGamepadConnectedEvent(Consumer<GamepadConnectedEvent> callback) {
        return gamepadConnectedEvent.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a gamepad button state is changed.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public static EventDispatcherHandle<GamepadButtonEvent> bindGamepadButtonEvent(Consumer<GamepadButtonEvent> callback) {
        return gamepadButtonEvent.bind(callback);
    }

    private GamepadManager() {
    }

}
