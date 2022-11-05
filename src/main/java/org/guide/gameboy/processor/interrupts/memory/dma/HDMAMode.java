package org.guide.gameboy.processor.interrupts.memory.dma;

/**
 * Represents possible transfer modes for HDMA operations.
 *
 * @author Brendan Jones
 */
public enum HDMAMode {

    /**
     * Standard HDMA transfer mode.
     */
    HDMA,

    /**
     * General purpose DMA transfer mode. This transfer mode halts program execution until the transfer is completed.
     */
    GDMA,

    /**
     * No transfer is in progress.
     */
    NONE
}
