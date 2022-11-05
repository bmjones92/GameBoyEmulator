package org.guide.gameboy.processor.interrupts.memory.dma;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.gameboy.video.LCDMode;
import org.guide.gameboy.video.Video;
import org.guide.gameboy.video.event.VideoModeEvent;
import org.guide.util.AddressUtils;
import org.guide.util.BitUtils;

import java.nio.ByteBuffer;

/**
 * Implements the HDMA controller of the Game Boy. This functionality is only present on the Color Game Boy systems. The
 * specification for this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Video_Display#LCD_VRAM_DMA_Transfers_.28CGB_only.29">
 * here
 * </a>.
 *
 * @author Brendan Jones
 */
public class HDMAController extends AddressSpace implements SerializableComponent {

    /**
     * The number of bytes to transfer per block.
     */
    public static final int BLOCK_SIZE = 0x10;

    /**
     * Whether the hardware is currently operating in CGB mode.
     */
    private boolean isCGB;

    /**
     * Sixteen-bit register that specifies the address of the data source. The lower
     * four bits are zeroed out (transfers occur in blocks of 16-bytes, and each block
     * must be aligned). Valid source addresses are [$0000-$7FFF] and [$A000-$DFFF].
     */
    private int sourceAddress;

    /**
     * Sixteen-bit register that specifies the destination address of an HDMA transfer. The
     * upper three bits and lower four bits are zeroed out. Valid destination addresses are
     * [$8000-$9FFF].
     */
    private int destinationAddress;

    /**
     * The number of blocks that need to be transferred still.
     */
    private int numBlocksRemaining;

    /**
     * The current transfer mode.
     */
    private HDMAMode transferMode;

    /**
     * Creates a new HDMA controller instance.
     *
     * @param memory The memory component.
     * @param video  The video component.
     */
    public HDMAController(Memory memory, Video video) {
        super(memory);

        this.transferMode = HDMAMode.NONE;
        this.numBlocksRemaining = 0;

        video.bindVideoModeEvent(this::onVideoModeChanged);

        memory.setAddressSpace(this, AddressUtils.HDMA1);
        memory.setAddressSpace(this, AddressUtils.HDMA2);
        memory.setAddressSpace(this, AddressUtils.HDMA3);
        memory.setAddressSpace(this, AddressUtils.HDMA4);
        memory.setAddressSpace(this, AddressUtils.HDMA5);
    }

    /**
     * Resets the controller to its default state.
     *
     * @param isCGB Whether the system is running in CGB mode.
     */
    public void reset(boolean isCGB) {
        this.isCGB = isCGB;
    }

    @Override
    public void write(int address, int value) {
        if (address == AddressUtils.HDMA5) {
            switch (transferMode) {
                case NONE -> {
                    startTransfer(value);
                    value &= 0x7F;
                }
                case HDMA -> {
                    if (BitUtils.isSet(value, 7)) {
                        // Attempting to start HDMA while an HDMA transfer is in progress will restart the transfer.
                        numBlocksRemaining = (value & 0x3F) + 1;
                    } else {
                        // Attempting to start GDMA while an HDMA transfer is in progress will stop the transfer.
                        value |= 0x80;
                        numBlocksRemaining = 0;
                        transferMode = HDMAMode.NONE;
                    }
                }
                case GDMA -> {
                    // TODO Implement GDMA
                }
            }
        }

        super.write(address, value);
    }

    @Override
    public int read(int address) {
        if (address != AddressUtils.HDMA5) {
            // HDMA1-4 are read only
            // TODO Review this as a possible bug. The comment seems irrelevant, maybe this logic was meant to be for
            //      writes rather than reads? Can't find documentation showing that these should return 0xFF.
            return 0xFF;
        }

        return super.read(address);
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.putShort((short) sourceAddress);
        out.putShort((short) destinationAddress);
        out.put((byte) numBlocksRemaining);
        out.put((byte) transferMode.ordinal());
    }

    @Override
    public void deserialize(ByteBuffer in) {
        this.sourceAddress = in.getShort() & 0xFFFF;
        this.destinationAddress = in.getShort() & 0xFFFF;
        this.numBlocksRemaining = in.get() & 0xFF;
        this.transferMode = HDMAMode.values()[in.get() & 0xFF];
    }

    private void onVideoModeChanged(VideoModeEvent e) {
        if (e.mode() == LCDMode.HBLANK && transferMode == HDMAMode.HDMA) {
            transferDataBlock();
        }
    }

    private void startTransfer(int hdma5) {
        // The source address registers.
        final var hdma1 = memory.getUnsigned(AddressUtils.HDMA1);
        final var hdma2 = memory.getUnsigned(AddressUtils.HDMA2);

        // The destination address registers.
        final var hdma3 = memory.getUnsigned(AddressUtils.HDMA3);
        final var hdma4 = memory.getUnsigned(AddressUtils.HDMA4);

        // Calculate the transfer source and destination addresses.
        this.sourceAddress = (hdma1 << 8) | (hdma2 & 0xF0);
        this.destinationAddress = 0x8000 | ((hdma3 & 0x1F) << 8) | (hdma4 & 0xF0);

        // Calculate the type of transfer being done.
        this.transferMode = BitUtils.isSet(hdma5, 7) ? HDMAMode.HDMA : HDMAMode.GDMA;
        this.numBlocksRemaining = (hdma5 & 0x7F) + 1;

        if (transferMode == HDMAMode.HDMA) {
            // A single block is immediately transferred in HDMA mode if the LCD is in H_BLANK mode.
            final var mode = LCDMode.get(memory.getUnsigned(AddressUtils.STAT));
            if (mode == LCDMode.HBLANK) {
                transferDataBlock();
            }
        } else {
            // The entire transfer is processed in one go.
            final var data = memory.getMemoryMap();
            memory.setBytes(destinationAddress, data, sourceAddress, numBlocksRemaining * BLOCK_SIZE);

            // Transfer is complete.
            numBlocksRemaining = 0;
            transferMode = HDMAMode.NONE;
        }
    }

    /**
     * Transfers a single block of data from the source address to the destination address.
     */
    private void transferDataBlock() {
        if (numBlocksRemaining == 0) {
            throw new IllegalStateException("Attempted to transfer a block while transfer is inactive.");
        }

        // Copy a single block of memory.
        final var data = memory.getMemoryMap();
        System.arraycopy(data, sourceAddress, data, destinationAddress, BLOCK_SIZE);

        sourceAddress += BLOCK_SIZE;
        destinationAddress += BLOCK_SIZE;

        if (--numBlocksRemaining == 0) {
            transferMode = HDMAMode.NONE;
        }
    }

}
