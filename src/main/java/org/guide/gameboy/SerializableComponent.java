package org.guide.gameboy;

import java.nio.ByteBuffer;

/**
 * A serializable component can be written to and read from a {@link ByteBuffer}.
 *
 * @author Brendan Jones
 */
public interface SerializableComponent {

    /**
     * The magic number for marking discrete sections of memory. This is useful when many components are serialized
     * together to ensure that the serialization and deserialization code matches.
     */
    int SECTION_MAGIC_NUMBER = 0xFEEDBEEF;

    /**
     * Serializes this component and writes its data to the provided buffer.
     *
     * @param out The buffer to write to.
     */
    void serialize(ByteBuffer out);

    /**
     * Deserializes this component and reads its data from the provided buffer.
     *
     * @param in
     */
    void deserialize(ByteBuffer in);

    /**
     * Writes a special 4-byte magic number to the buffer. This can be used in conjunction with
     * {@link SerializableComponent#verifyIntegrityCheck(ByteBuffer, String)} to ensure the serialization and
     * deserialization logic of a component matches.
     *
     * @param out The buffer to write to.
     */
    static void writeIntegrityCheck(ByteBuffer out) {
        out.putInt(SECTION_MAGIC_NUMBER);
    }

    /**
     * Checks to see if the next 4-bytes in the buffer match the special 4-byte magic number. This can be used in
     * conjunction with {@link SerializableComponent#writeIntegrityCheck(ByteBuffer)} to ensure the serialization and
     * deserialization logic of a component matches.
     *
     * @param in           The buffer to read from.
     * @param errorMessage The message to display if the integrity check fails.
     */
    static void verifyIntegrityCheck(ByteBuffer in, String errorMessage) {
        final var number = in.getInt();
        if (number != SECTION_MAGIC_NUMBER) {
            throw new IllegalStateException("Failed integrity check: " + errorMessage);
        }
    }

}
