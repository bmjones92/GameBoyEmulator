package org.guide.gameboy.video.queue;

import org.guide.gameboy.SerializableComponent;

import java.nio.ByteBuffer;

/**
 * Implements the pixel queue for the LCD hardware.
 *
 * @author Brendan JOnes
 */
public class PixelQueue implements SerializableComponent {

    /**
     * The capacity of the queue.
     */
    private static final int CAPACITY = 16;

    /**
     * The queue elements.
     */
    private final PixelQueueEntry[] queue = new PixelQueueEntry[CAPACITY];

    /**
     * The position of the next element to read.
     */
    private int front;

    /**
     * The position of the next element to write.
     */
    private int rear;

    /**
     * The current size of the queue.
     */
    private int size;

    /**
     * Creates a new PixelQueue instance.
     */
    public PixelQueue() {
        for (int i = 0; i < CAPACITY; ++i) {
            queue[i] = new PixelQueueEntry();
        }

        clear();
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.put((byte) size);
        for (var i = 0; i < size; ++i) {
            get(i).serialize(out);
        }
    }

    @Override
    public void deserialize(ByteBuffer in) {
        this.size = in.get() & 0xFF;
        for (var i = 0; i < size; ++i) {
            get(i).deserialize(in);
        }
    }

    /**
     * Clears the queue and resets it to its default state.
     */
    public void clear() {
        this.front = 0;
        this.rear = 0;
        this.size = 0;
    }

    /**
     * Pushes a new pixel to the queue.
     *
     * @param source      The pixel source.
     * @param color       The color of the pixel.
     * @param palette     The palette the pixel belongs to.
     * @param hasPriority Whether the pixel has priority.
     */
    public void push(PixelSource source, int color, int palette, boolean hasPriority) {
        queue[rear].set(source, color, palette, hasPriority);
        rear = (rear + 1) % queue.length;
        size++;
    }

    /**
     * Pops a pixel from the queue.
     *
     * @return The pixel that was popped.
     */
    public PixelQueueEntry pop() {
        final var entry = queue[front];

        front = (front + 1) % queue.length;
        size--;

        return entry;
    }

    /**
     * Gets a specific element from the queue.
     *
     * @param index The index of the element to retrieve.
     * @return The retrieved element.
     */
    public PixelQueueEntry get(int index) {
        return queue[(front + index) % queue.length];
    }

    /**
     * Gets the size of the queue.
     *
     * @return The number of elements in the queue.
     */
    public int size() {
        return size;
    }

}
