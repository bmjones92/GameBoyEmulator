package org.guide.gameboy.processor.interrupts.memory;

/**
 * An event that fires whenever a system's memory changes.
 *
 * @param memory  The memory that fired the event.
 * @param address The starting address of the memory range that was modified.
 * @param length  The length of the range that was modified.
 * @author Brendan Jones
 */
public record MemoryChangedEvent(Memory memory, int address, int length) {
}
