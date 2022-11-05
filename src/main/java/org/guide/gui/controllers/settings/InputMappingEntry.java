package org.guide.gui.controllers.settings;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

/**
 * Mappings for input bindings.
 *
 * @author Brendan Jones
 */
public class InputMappingEntry {

    /**
     * The entry to use for unbound keys.
     */
    public static final InputMappingEntry NULL = new InputMappingEntry(GLFW_KEY_UNKNOWN, "[UNBOUND]");

    /**
     * The id of the bound key.
     */
    private final int id;

    /**
     * The human-readable name of the bound key.
     */
    private final String name;

    /**
     * Creates a new input mapping entry.
     *
     * @param id   The key being bound.
     * @param name The human-readable name of the binding.
     */
    public InputMappingEntry(int id, String name) {
        this.id = id;
        this.name = requireNonNull(name);
    }

    /**
     * Gets the id of the bound key.
     *
     * @return The binding id.
     */
    public int getID() {
        return id;
    }

    /**
     * Gets the name of the bound key.
     *
     * @return The binding name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
