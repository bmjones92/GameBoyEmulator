package org.guide.emulator.input;

/**
 * Represents the various input actions that are known to the emulator. Hardware specific inputs such as key presses
 * and button presses are mapped to specific actions.
 */
public enum InputAction {

    /**
     * The "UP" button on the Game Boy's dpad.
     */
    GAMEBOY_UP(false),

    /**
     * The "RIGHT" button on the Game Boy's dpad.
     */
    GAMEBOY_RIGHT(false),

    /**
     * The "DOWN" button on the Game Boy's dpad.
     */
    GAMEBOY_DOWN(false),

    /**
     * The "LEFT" button on the Game Boy's dpad.
     */
    GAMEBOY_LEFT(false),

    /**
     * The "START" button on the Game Boy.
     */
    GAMEBOY_START(false),

    /**
     * The "SELECT" button on the Game Boy.
     */
    GAMEBOY_SELECT(false),

    /**
     * The "B" button on the Game Boy.
     */
    GAMEBOY_B(false),

    /**
     * The "A" button on the Game Boy.
     */
    GAMEBOY_A(false),

    /**
     * The "quick save" action. This is not present on the Game Boy hardware, but is used by the emulator
     * to save the game to the current quick save slot.
     */
    SYSTEM_QUICK_SAVE(false),

    /**
     * The "quick load" action. This is not present on the Game Boy hardware, but is used by the emulator
     * to load the game from the current quick save slot.
     */
    SYSTEM_QUICK_LOAD(false),

    /**
     * The "previous quick save slot" action. This is not present on the Game Boy hardware, but is used by the
     * emulator to select the previous slot for quick saves and quick loads.
     */
    SYSTEM_QUICK_PREV(false),

    /**
     * The "next quick save slot" action. This is not present on the Game Boy hardware, but is used by the
     * emulator to select the next slot for quick saves and quick loads.
     */
    SYSTEM_QUICK_NEXT(false),

    /**
     * The "pause and resume" action. This is not present on the Game Boy hardware, but is used by the emulator
     * to pause and unpause the emulator.
     */
    SYSTEM_PAUSE_RESUME(false),

    /**
     * The "step forward" action. This is not present on the Game Boy hardware, but is used by the emulator
     * during debugging to step forward one CPU cycle.
     */
    SYSTEM_STEP_FORWARD(true),

    /**
     * The "step frame" action. This is not present on the Game Boy hardware, but is used by the emulator
     * during debugging to step forward to the next frame.
     */
    SYSTEM_STEP_FRAME(true);

    /**
     * Whether this input action allows for repeat events.
     */
    private final boolean allowRepeatEvents;

    /**
     * Creates a new InputAction.
     *
     * @param allowRepeatEvents Whether this action allows repeat events.
     */
    InputAction(boolean allowRepeatEvents) {
        this.allowRepeatEvents = allowRepeatEvents;
    }

    /**
     * Get whether this action triggers as a result of repeat events which occur when a key is held.
     *
     * @return Whether repeat events are allowed.
     */
    public boolean allowRepeatEvents() {
        return allowRepeatEvents;
    }

}
