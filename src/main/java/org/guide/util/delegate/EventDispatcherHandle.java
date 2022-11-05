package org.guide.util.delegate;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A handle for working with a registered callback.
 *
 * @param <T> The type of data that is broadcast by the delegate.
 * @author Brendan Jones
 */
public class EventDispatcherHandle<T> {

    /**
     * The dispatcher this handle is registered to.
     */
    private final EventDispatcher<T> dispatcher;

    /**
     * The callback to execute when the dispatcher broadcasts.
     */
    private final Consumer<T> callback;

    /**
     * Whether this handle has been destroyed.
     */
    private boolean isDestroyed = false;

    /**
     * Creates a new EventDispatcherHandle.
     *
     * @param dispatcher The dispatcher that this handle is registered to.
     * @param callback   The callback to execute when the delegate broadcasts.
     */
    EventDispatcherHandle(EventDispatcher<T> dispatcher, Consumer<T> callback) {
        this.dispatcher = requireNonNull(dispatcher);
        this.callback = requireNonNull(callback);
    }

    /**
     * Unregisters this handle from the dispatcher and destroys it.
     */
    public void destroy() {
        dispatcher.unbind(this);
        this.isDestroyed = true;
    }

    /**
     * Get whether the handle has been destroyed and is no longer valid.
     *
     * @return Whether the handle is destroyed.
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /**
     * Get the callback to execute when the dispatcher broadcasts.
     *
     * @return The callback.
     */
    public Consumer<T> callback() {
        return callback;
    }

}
