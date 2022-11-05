package org.guide.util.delegate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An event dispatcher is responsible for broadcasting an event to registered callbacks.
 *
 * @param <T> The type of data being broadcast.
 * @author Brendan Jones
 */
public class EventDispatcher<T> {

    /**
     * The list of callbacks to broadcast to.
     */
    private final List<EventDispatcherHandle<T>> handles = new ArrayList<>();

    /**
     * Creates a new Event Dispatcher.
     */
    public EventDispatcher() {
    }

    /**
     * Broadcasts an event to all registered callbacks.
     *
     * @param data The data to broadcast.
     */
    public void broadcast(T data) {
        handles.forEach(handle -> handle.callback().accept(data));
    }

    /**
     * Broadcasts an event to all registered callbacks. The supplier will only be evaluated if one or more callbacks
     * are registered to this event dispatcher.
     *
     * @param supplier The data supplier.
     */
    public void broadcast(Supplier<T> supplier) {
        if (!handles.isEmpty()) {
            final var data = supplier.get();
            broadcast(data);
        }
    }

    /**
     * Registers a new callback to this dispatcher.
     *
     * @param callback The callback to register.
     * @return A dispatcher handle for the registered callback.
     */
    public EventDispatcherHandle<T> bind(Consumer<T> callback) {
        final var handle = new EventDispatcherHandle<>(this, callback);
        handles.add(handle);
        return handle;
    }

    /**
     * Unregisters a callback from the dispatcher.
     *
     * @param handle The callback to unregister.
     */
    void unbind(EventDispatcherHandle<T> handle) {
        handles.remove(handle);
    }

}
