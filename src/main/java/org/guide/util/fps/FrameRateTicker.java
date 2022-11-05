package org.guide.util.fps;

import org.guide.util.delegate.EventDispatcher;
import org.guide.util.delegate.EventDispatcherHandle;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Tracks average frame times over the lifetime of the ticker. As frames are submitted, the ticker periodically
 * emits {@link FrameRateEvent}s to notify registered listeners of frame rate changes.
 *
 * @author Brendan Jones
 */
public class FrameRateTicker {

    /**
     * The dispatcher for frame rate events.
     */
    private final EventDispatcher<FrameRateEvent> frameRateEvent = new EventDispatcher<>();

    /**
     * The most recent frame times tracked by this ticker.
     */
    private final long[] frameTimes;

    /**
     * The index of next frame time to push.
     */
    private int frameIndex = 0;

    /**
     * The last time a frame rate event was emitted.
     */
    private long lastBroadcastTime = 0;

    /**
     * The frequency (in milliseconds) to emit events.
     */
    private final long eventFrequency;

    /**
     * Creates a new frame rate ticker.
     *
     * @param numFramesToTrack The number of frame times to track before older frame times are discarded.
     * @param eventFrequency   The frequency to emit events (in milliseconds).
     */
    public FrameRateTicker(int numFramesToTrack, long eventFrequency) {
        this.frameTimes = new long[numFramesToTrack];
        this.eventFrequency = eventFrequency;
    }

    /**
     * Pushes a frame time to the ticker. This will cause the ticker to emit a {@link FrameRateEvent} if
     * enough time has elapsed since the last event was emitted.
     *
     * @param millis The frame time in milliseconds.
     */
    public void pushFrameTime(long millis) {
        frameTimes[frameIndex] = millis;
        frameIndex = (frameIndex + 1) % frameTimes.length;

        final var timeSinceLastBroadcast = System.currentTimeMillis() - lastBroadcastTime;
        if (timeSinceLastBroadcast > eventFrequency) {
            broadcastFrameRateEvent();
        }
    }

    /**
     * Broadcasts a frame rate event to all registered listeners.
     */
    private void broadcastFrameRateEvent() {
        Arrays.stream(frameTimes).average().ifPresent(avg -> {
            if (avg == 0.0) {
                avg = 1.0;
            }

            final var fps = (int) Math.round(1000.0 / avg);
            frameRateEvent.broadcast(new FrameRateEvent(this, fps));

            lastBroadcastTime = System.currentTimeMillis();
        });
    }

    /**
     * @param callback
     * @return
     */
    public EventDispatcherHandle<FrameRateEvent> bindFrameRateEvent(Consumer<FrameRateEvent> callback) {
        return frameRateEvent.bind(callback);
    }

}