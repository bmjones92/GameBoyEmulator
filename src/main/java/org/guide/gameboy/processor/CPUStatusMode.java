package org.guide.gameboy.processor;

/**
 * Represents the possible status modes of the CPU.
 *
 * @author Brendan Jones
 */
public enum CPUStatusMode {

    /**
     * The processor is running normally.
     */
    RUNNING,

    /**
     * The processor is stopped. When in this mode, the processor and LCD are disabled until an input button is pressed.
     */
    STOPPED,

    /**
     * The processor is halted. When in this mode, the processor does not function until a system interrupt occurs.
     */
    HALTED

}
