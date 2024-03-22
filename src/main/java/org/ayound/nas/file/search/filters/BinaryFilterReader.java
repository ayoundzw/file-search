package org.ayound.nas.file.search.filters;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * Wraps a Reader in order to filter characters into an indexable stream. Will
 * replace all special characters with spaces and only retain a-z, 0-9 and
 * the nordic characters {�, �, �, �, �}. Upper case variants of these are
 * replaced by their lower cased counterparts.
 * <p/>
 * By sticking to the (UTF-8) character subset 0-255, we may jump from
 * characters to bytes and vice verca.
 */
public class BinaryFilterReader extends FilterReader {
    private static final Logger log = LogManager.getLogger(BinaryFilterReader.class);

    private static final char SPACE = StringsFilterInputStream.SPACE;
    private static final char[] BYTE_MAP = StringsFilterInputStream.BYTE_MAP;

    // Internals
    private CharBuffer map = null;

    /**
     * Create a new filtered reader.
     *
     * @param in a Reader object providing the underlying stream.
     * @throws NullPointerException if <code>in</code> is <code>null</code>
     */
    public BinaryFilterReader(Reader in) {
        super(in);

        // Generate fast byte map
        map = CharBuffer.allocate(BYTE_MAP.length);
        for (int i = 0; i < BYTE_MAP.length; i++) {
            map.put(i, BYTE_MAP[i]);
        }
    }


    /**
     * Read a single character.
     *
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        int c = in.read();
        return (c >= 0 && c < 256) ? map.get(c) : SPACE;
    }


    /**
     * Read characters into a portion of an array.
     *
     * @throws IOException If an I/O error occurs
     */
    public int read(char cbuf[]) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }


    /**
     * Read characters into a portion of an array.
     *
     * @throws IOException If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        int num = in.read(cbuf, off, len);
        for (int i = 0; i < num; i++) {  // also guarantees num > 0
            int c = (int) cbuf[off + i];
            char mapped = (c >= 0 && c < 256) ? map.get(c) : SPACE;
            cbuf[off + i] = mapped;
        }
        return num;
    }
}
