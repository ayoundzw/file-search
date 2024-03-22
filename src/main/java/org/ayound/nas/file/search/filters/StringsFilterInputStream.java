package org.ayound.nas.file.search.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;

public class StringsFilterInputStream extends FilterInputStream {
    private static final Logger log = LogManager.getLogger(StringsFilterInputStream.class);

    private static final int EOF = -1;
    static final char SPACE = (char) 32;

    static final char[] BYTE_MAP = {
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, //  0 -  9
            SPACE, SPACE, SPACE, SPACE,	SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, // 10 - 19
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, // 20 - 29
            SPACE, SPACE, SPACE,                                                  // 30 - 32
            // ! " # $ % & ' ( ) *
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, // 33 - 42
            // + , - . /
            '+', SPACE, '-', '.', SPACE,                                          // 43 - 47
            // 0 1 2 3 4 5 6 7 8 9
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',                     // 48 - 57
            // : ; < = > ? @
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, '@',                        // 58 - 64
            // A - Z   (replaced by lower case variants)
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',                     // 65 - 90
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            // [ \ ] ^ _ `
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,                             // 91 - 96
            // a - z
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',                     // 97 - 122
            'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            // { | } ~ ...
            SPACE, SPACE, SPACE, SPACE, SPACE, '\u20AC', SPACE, SPACE, SPACE, SPACE, // 123 - 195
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
            SPACE, SPACE, SPACE, SPACE, '\u00A7', SPACE, SPACE, SPACE, SPACE, SPACE,
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
            SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE,
            SPACE, SPACE, SPACE,
            // Ä Å Æ    (replaced by lower case variants)
            '\u00E4', '\u00E5', '\u00E6',                                          // 196 - 198
            // accent E's
            SPACE, '\u00E8', '\u00E9', SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, // 199 - 213
            SPACE, SPACE, SPACE, SPACE, SPACE,
            // Ö .. Ø ... (replaced by lower case variants)
            '\u00F6', SPACE, '\u00F8',                                             // 214 - 216
            // .. Ü .. (replaced by lower case variant)
            SPACE, SPACE, SPACE, '\u00FC', SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, // 217 -  227
            SPACE,
            // ä å æ
            '\u00E4', '\u00E5', '\u00E6',                                          // 228 - 230
            // accent e's ...
            SPACE, '\u00E8', '\u00E9', SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, SPACE, // 231 - 245
            SPACE, SPACE, SPACE, SPACE, SPACE,
            // ö .. ø
            '\u00F6', SPACE, '\u00F8',                                             // 246 - 248
            // .. ü ..
            SPACE, SPACE, SPACE, '\u00FC', SPACE, SPACE, SPACE                     // 249 - 255
    };

    // Parameters
    private int longestToken;
    private int shortestToken;

    // Internals
    private CharBuffer map = null;
    private CharBuffer buffer = null;
    private boolean withinLongWord = false;

    StringsFilterInputStream(
            InputStream inputStream,
            int shortestToken,
            int longestToken) throws IOException {

        super(inputStream);

        this.shortestToken = Math.max(1, shortestToken); // at least one byte
        this.longestToken = Math.max(3, longestToken); // minimally three bytes

        // Initiate internal work buffer
        buffer = CharBuffer.allocate(this.longestToken + /* algorithm slack */ 2);
        buffer.limit(0); // initially

        // Generate fast byte map
        map = CharBuffer.allocate(BYTE_MAP.length);
        for (int i = 0; i < BYTE_MAP.length; i++) {
            map.put(i, BYTE_MAP[i]);
        }
    }

    public int available() throws IOException {
        return buffer.remaining() + in.available();
    }

    public void close() throws IOException {
        in.close();
    }

    public int read() throws IOException {
        // We can be in these states:
        //  1. No bytes in buffer, in which case we will read from the stream
        //  2. Bytes in the buffer - maximally longestToken many
        if (buffer.hasRemaining()) {
            // We have bytes in the buffer, return next byte...
            return buffer.get();

        } else {
            // No bytes in the buffer, read up to longest token many
            buffer.clear();

            while (buffer.position() < buffer.capacity()) {
                int b = in.read();

                if (EOF == (byte) b) {
                    // No more bytes to read from the input stream and the
                    // current "word" fits the length constraints.
                    // Prepare buffer for read and (possibly) return first byte.

                    if (buffer.position() >= shortestToken) {
                        buffer.flip();
                        return buffer.get();

                    } else {
                        buffer.limit(0);
                        return EOF;
                    }
                }

                // Map read byte (if possible)
                char mappedByte =  map.get(b);

                if (buffer.position() > longestToken) {
                    // We have an additional character (interesting or not) which will
                    // effectively exceed the longestToken count for the current "word".
                    // Skip this "word", i.e. this buffer.
                    buffer.clear();
                    withinLongWord = true;
                    continue;
                }


                if (/* uninteresting byte */ SPACE == mappedByte) {
                    withinLongWord = false;

                    if (buffer.position() < shortestToken) {
                        // We found an uninteresting character in the
                        // stream before reaching shortestToken length.
                        // Start all over again...
                        buffer.clear();
                        continue;

                    } else {
                        // Add single SPACE to buffer and start reading
                        buffer.put(SPACE); // increments position
                        buffer.flip();
                        return buffer.get();
                    }
                }

                if (withinLongWord) {
                    // We have an additional character (interesting or not) which will
                    // effectively exceed the longestToken count for the current "word".
                    // Skip this "word", i.e. this buffer.
                    buffer.clear();
                    continue;
                }

                // Store interesting byte in the buffer
                buffer.put(mappedByte); // increments position
            }
            buffer.flip();
            return buffer.get();
        }
    }

    public int read(byte[] bytes, int off, int len) throws IOException {
        if (off < 0) {
            throw new ArrayIndexOutOfBoundsException("Illegal offset " + off);
        }
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException("Illegal length " + len);
        }
        if (off + len > bytes.length) {
            throw new ArrayIndexOutOfBoundsException("Illegal offset " + off + " and length " + len);
        }

        int i;
        for (i = 0; i < len; i++) {
            int readByte = read();
            if (EOF == readByte) {
                break;
            }
            bytes[off++] = (byte) readByte;
        }
        return i;
    }

    public long skip(long length) throws IOException {
        while (buffer.hasRemaining() && length-- > 0) {
            buffer.position(buffer.position() + 1);
        }
        return in.skip(length);
    }

    public int read(byte[] bytes) throws IOException {
        return read(bytes, /* offset */ 0, bytes.length);
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readlimit) {
        /* not supported */
    }

    public void reset() {
        /* do nothing */
    }
}
