package org.guide.gameboy.processor.interrupts.memory;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.gameboy.processor.interrupts.memory.space.UnusedAddressSpace;
import org.guide.gameboy.processor.interrupts.memory.space.WRAMAddressSpace;
import org.guide.util.AddressUtils;
import org.guide.util.delegate.EventDispatcher;
import org.guide.util.delegate.EventDispatcherHandle;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Represents the system memory of the emulated Game Boy.
 */
public class Memory implements SerializableComponent {

    /**
     * The amount of system memory available.
     */
    public static final int MEMORY_MAP_SIZE = 0x10000;

    /**
     * The dispatcher for memory changed events.
     */
    private final EventDispatcher<MemoryChangedEvent> memoryChangedEvent = new EventDispatcher<>();


    /**
     * The raw memory managed by this component.
     */
    private final byte[] memory = new byte[MEMORY_MAP_SIZE];

    /**
     * Maps memory addresses to the address spaces that manage them.
     */
    private final AddressSpace[] spaces = new AddressSpace[MEMORY_MAP_SIZE];

    /**
     * The Work RAM address space.
     */
    private final WRAMAddressSpace wram;

    /**
     * The Unused address space.
     */
    private final UnusedAddressSpace unused;

    /**
     * Masks for the unused bits in each of the I/O registers. These are forcibly set to 1.
     */
    private static final int[] IO_UNUSABLE_BIT_MASKS = {
            // 0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
            0xC0, 0x00, 0x7C, 0xFF, 0x00, 0x00, 0x00, 0xF8, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xE0, // 00
            0x80, 0x00, 0x00, 0x00, 0x38, 0xFF, 0x00, 0x00, 0x00, 0x00, 0x7F, 0x00, 0x9F, 0x00, 0x00, 0xFF, // 10
            0xC0, 0x00, 0x00, 0x3F, 0x00, 0x00, 0x70, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 20
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // 30
            0x00, 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xFF, 0x7E, 0xFF, 0xFE, // 40
            0xFF, 0x00, 0x0F, 0xE0, 0x0F, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 50
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x40, 0x00, 0x40, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, // 60
            0xF8, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 70
    };

    /**
     * Masks for the writable bits in each of the I/O registers.
     */
    private static final int[] IO_WRITABLE_BIT_MASKS = {
            // 0     1     2     3     4     5     6     7     8     9     A     B     C     D     E     F
            0x30, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 00
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 10
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 20
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 30
            0xFF, 0xF8, 0xFF, 0xFF, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x01, 0xFF, 0x01, // 40
            0xFF, 0xFF, 0xF0, 0x1F, 0xF0, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 50
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 60
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 70
    };

    /**
     * Creates a new Memory instance.
     */
    public Memory() {
        this.wram = new WRAMAddressSpace(this);
        this.unused = new UnusedAddressSpace(this);

        // OAM and HRAM requires no special functionality, so use a plain address space for it.
        final var defaultSpace = new AddressSpace(this);
        setAddressSpace(defaultSpace, AddressUtils.OAM_ADDRESS_START, AddressUtils.OAM_ADDRESS_END);
        setAddressSpace(defaultSpace, AddressUtils.HRAM_ADDRESS_START, AddressUtils.HRAM_ADDRESS_END);
    }

    /**
     * Resets the memory component to its default state.
     *
     * @param isCGB Whether the machine is running in CGB mode.
     */
    public void reset(boolean isCGB) {
        // Reset all memory values to their defaults.
        for (var i = 0; i < AddressUtils.IO_ADDRESS_START; ++i) {
            if (AddressUtils.isAddressIO(i)) {
                memory[i] = (byte) IO_UNUSABLE_BIT_MASKS[i];
            } else {
                memory[i] = 0x00;
            }
        }

        memoryChangedEvent.broadcast(new MemoryChangedEvent(this, 0, MEMORY_MAP_SIZE));

        wram.reset(isCGB);
        unused.reset();
    }

    @Override
    public void serialize(ByteBuffer out) {
        wram.serialize(out);
        out.put(memory, AddressUtils.OAM_ADDRESS_START, 0x200);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        wram.deserialize(in);
        in.get(memory, AddressUtils.OAM_ADDRESS_START, 0x200);
    }

    /**
     * Registers a callback to be executed whenever system memory is changed.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<MemoryChangedEvent> bindMemoryChangedEvent(Consumer<MemoryChangedEvent> callback) {
        return memoryChangedEvent.bind(callback);
    }

    /**
     * Forces a memory changed event to be broadcast which includes the entire memory map.
     */
    public void broadcastGlobalMemoryChangedEvent() {
        memoryChangedEvent.broadcast(new MemoryChangedEvent(this, 0, MEMORY_MAP_SIZE));
    }

    /**
     * Sets the address space for a specified address range. If any address spaces are already bound to any of the
     * addresses in the range, they will be replaced with the new address space.
     *
     * @param space      The address space to bind to the range.
     * @param rangeStart The start address in the range (inclusive).
     * @param rangeEnd   The end address in the range (inclusive).
     */
    public void setAddressSpace(AddressSpace space, int rangeStart, int rangeEnd) {
        requireNonNull(space, "Attempted to register a null address space.");

        for (var i = rangeStart; i <= rangeEnd; ++i) {
            spaces[i] = space;
        }
    }

    /**
     * Sets the address space for a specified address. If an address space is already bound to the address,
     * it will be replaced with the new address space.
     *
     * @param space   The address space to bind to the address.
     * @param address The address to bind to.
     */
    public void setAddressSpace(AddressSpace space, int address) {
        setAddressSpace(space, address, address);
    }

    /**
     * Applies the IO "write" for the specified address to the provided value. If the address is not an I/O register,
     * no mask will be applied and the original value will be returned.
     *
     * @param address The address to apply the mask of.
     * @param value   The value to apply the mask to.
     * @return The masked value.
     */
    private int applyWriteMask(int address, int value) {
        if (AddressUtils.isAddressIO(address)) {
            int index = address - AddressUtils.IO_ADDRESS_START;

            // Clear any non-writable bits to 0.
            value &= IO_WRITABLE_BIT_MASKS[index];
            // Force all unused bits to 1.
            value |= IO_UNUSABLE_BIT_MASKS[index];
            // Blit current read-only bits onto value.
            value |= getUnsigned(address) & (~IO_WRITABLE_BIT_MASKS[index] & 0xFF);
        }
        return value;
    }

    /**
     * Writes an 8-bit value to the specified address in memory. This method does not directly modify the underlying
     * memory map, but instead passes the request to the appropriate address space to give it an opportunity to perform
     * custom logic before memory is updated. If no address space is bound to the target address, then the write request
     * will be discarded.
     * <p>
     * This is intended to be used by the CPU when processing instructions from the loaded cartridge. Hardware
     * components should instead use {@link Memory#set(int, int)} to modify system memory directly.
     *
     * @param address The address to write to.
     * @param value   The value to write.
     */
    public void write(int address, int value) {
        address &= 0xFFFF;
        if (spaces[address] != null) {
            spaces[address].write(address, applyWriteMask(address, value));
        }
    }

    /**
     * Writes a 16-bit value to the specified memory address. This method does not directly modify the underlying
     * memory map, but instead passes the request to the appropriate address space to give it an opportunity to perform
     * custom logic before memory is updated. If no address space is bound to the target address, then the write request
     * will be discarded.
     * <p>
     * This is intended to be used by the CPU when processing instructions from the loaded cartridge. Hardware
     * components should instead use {@link Memory#setShort(int, int)} to modify system memory directly.
     *
     * @param address The address to write to.
     * @param value   The value to write.
     */
    public void writeShort(int address, int value) {
        final var low = value & 0xFF;
        final var high = (value >> 8) & 0xFF;

        write(address, low);
        write(address + 1, high);
    }

    /**
     * Reads a signed 8-bit value from the specified memory address. This method does not directly read from the
     * underlying memory map, but instead passes the request to the appropriate address space bound to the target
     * address to give it an opportunity to perform custom logic before returning the result. If no address space
     * is bound to the target address, then {@code $FF} is returned.
     * <p>
     * This is intended to be used by the CPU when processing instructions from the loaded cartridge. Hardware
     * components should instead use {@link Memory#get(int)} to read system memory directly.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int read(int address) {
        address &= 0xFFFF;

        var value = 0xFF;
        if (spaces[address] != null) {
            value = spaces[address].read(address);
        }
        return (byte) value;
    }

    /**
     * Reads an unsigned 8-bit value from the specified memory address. This method does not directly read from the
     * underlying memory map, but instead passes the request to the appropriate address space bound to the target
     * address to give it an opportunity to perform custom logic before returning the result. If no address space
     * is bound to the target address, then {@code $FF} is returned.
     * <p>
     * This is intended to be used by the CPU when processing instructions from the loaded cartridge. Hardware
     * components should instead use {@link Memory#getUnsigned(int)} to read system memory directly.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int readUnsigned(int address) {
        return read(address) & 0xFF;
    }

    /**
     * Reads a signed 16-bit value from the specified memory address. This method does not directly read from the
     * underlying memory map, but instead passes the request to the address space bound to the target address to
     * give it an opportunity to perform custom logic before returning the result. If no address space is bound to
     * either of the addresses, then {@code $FF} is returned for that byte.
     * <p>
     * This is intended to be used by the CPU when processing instructions from a loaded cartridge. Hardware
     * components should instead use {@link Memory#getShort(int)} to read system memory directly.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int readShort(int address) {
        final var low = readUnsigned(address);
        final var high = readUnsigned(address + 1);
        return (high << 8) | low;
    }

    /**
     * Reads an unsigned 16-bit value from the specified memory address. This method does not directly read from the
     * underlying memory map, but instead passes the request to the address space bound to the target address to
     * give it an opportunity to perform custom logic before returning the result. If no address space is bound to
     * either of the addresses, then {@code $FF} is returned for that byte.
     * <p>
     * This is intended to be used by the CPU when processing instructions from a loaded cartridge. Hardware
     * components should instead use {@link Memory#getShort(int)} to read system memory directly.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int readUnsignedShort(int address) {
        return readShort(address) & 0xFFFF;
    }

    /**
     * Sets the 8-bit value at the specified memory address. This method modifies the underlying memory map directly
     * and notifies all registered memory event listeners of the change.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#write(int, int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to write to.
     * @param value   The value to write.
     */
    public void set(int address, int value) {
        int normalizedAddress = address & 0xFFFF;

        memory[normalizedAddress] = (byte) value;
        memoryChangedEvent.broadcast(new MemoryChangedEvent(this, normalizedAddress, 1));
    }

    /**
     * Sets the 16-bit value at the specified memory address. This method modifies the underlying memory map directly
     * and notifies all registered memory event listeners of the change.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#write(int, int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to write to.
     * @param value   The value to write.
     */
    public void setShort(int address, int value) {
        final var normalizedAddress = address & 0xFFFF;

        final var low = value & 0xFF;
        final var high = (value >> 8) & 0xFF;

        memory[normalizedAddress] = (byte) low;
        memory[normalizedAddress + 1] = (byte) high;

        memoryChangedEvent.broadcast(new MemoryChangedEvent(this, normalizedAddress, 2));
    }

    /**
     * Sets a range of values starting at the specified memory address. This method modifies the underlying memory map
     * directly and notifies all registered memory event listeners of the change.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#write(int, int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The starting address of the destination range.
     * @param values  The data source.
     * @param offset  The index of the first element in the data source to copy.
     * @param length  The number of elements to copy from the data source.
     */
    public void setBytes(int address, byte[] values, int offset, int length) {
        final var normalizedAddress = address & 0xFFFF;

        System.arraycopy(values, offset, memory, normalizedAddress, length);

        memoryChangedEvent.broadcast(new MemoryChangedEvent(this, normalizedAddress, length));
    }

    /**
     * Sets a range of values starting at the specified memory address. This method modifies the memory map directly
     * and notifies all registered memory event listeners of the change.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#write(int, int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to start writing data to.
     * @param values  The data source.
     */
    public void setBytes(int address, byte[] values) {
        setBytes(address, values, 0, values.length);
    }

    /**
     * Gets a signed 8-bit value from the specified memory address. This method reads directly from the memory map
     * and bypasses any processing that would otherwise be done by the bound {@link AddressSpace}.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#read(int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int get(int address) {
        address &= 0xFFFF;
        return memory[address];
    }

    /**
     * Gets an unsigned 8-bit value from the specified memory address. This method reads directly from the memory map
     * and bypasses any processing that would otherwise be done by the bound {@link AddressSpace}.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#read(int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int getUnsigned(int address) {
        return get(address) & 0xFF;
    }

    /**
     * Gets a signed 16-bit value from the specified memory address. This method reads directly from the memory map
     * and bypasses any processing that would otherwise be done by the bound {@link AddressSpace}.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#read(int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int getShort(int address) {
        int low = getUnsigned(address);
        int high = getUnsigned(address + 1);
        return (high << 8) | low;
    }

    /**
     * Gets an unsigned 16-bit value from the specified memory address. This method reads directly from the memory map
     * and bypasses any processing that would otherwise be done by the bound {@link AddressSpace}.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory. CPU instructions
     * should instead use {@link Memory#read(int)} to allow bound address spaces to intercept the request and
     * perform any necessary custom logic.
     *
     * @param address The address to read from.
     * @return The value stored at the address.
     */
    public int getUnsignedShort(int address) {
        return getShort(address) & 0xFFFF;
    }

    /**
     * Gets a block of data starting at the specified memory address. This method reads directly from the memory map
     * and bypasses any processing that would otherwise be done by the bound {@link AddressSpace}.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory.
     *
     * @param baseAddress The address to start reading from.
     * @param data        The buffer to copy data into.
     * @param offset      The position within the buffer to start writing to.
     * @param length      The number of bytes to copy.
     */
    public void getBytes(int baseAddress, byte[] data, int offset, int length) {
        System.arraycopy(memory, baseAddress, data, offset, length);
    }

    /**
     * Gets a block of data starting at the specified memory address. This method reads directly from the memory map
     * and bypasses any processing that would otherwise be done by the bound {@link AddressSpace}.
     * <p>
     * This is intended to be used by hardware components that need direct access to system memory.
     *
     * @param baseAddress The address to start reading from.
     * @param data        The buffer to copy data into.
     */
    public void getBytes(int baseAddress, byte[] data) {
        getBytes(baseAddress, data, 0, data.length);
    }

    /**
     * Gets the underlying memory map managed by this memory component.
     *
     * @return The underlying memory map.
     */
    public byte[] getMemoryMap() {
        return memory;
    }

}
