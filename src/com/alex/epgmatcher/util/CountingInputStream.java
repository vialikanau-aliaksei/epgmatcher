package com.alex.epgmatcher.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class for wrapping {@link InputStream} and counting bytes read from stream.
 * Created by Alex on 21.04.2017.
 */
public class CountingInputStream extends InputStream {


    private int count;

    private final InputStream inputStream;

    /**
     * Constructs new CountingInputStream
     * @param inputStream stream to be wrapped in this instance.
     */
    public CountingInputStream(InputStream inputStream) {
        this.count = 0;
        this.inputStream = inputStream;
    }

    /**
     * @return count of bytes read from wrapped {@link InputStream}
     */
    public int getCount() {
        return count;
    }

    @Override
    public int read() throws IOException {
        int found = inputStream.read();
        this.count += (found >= 0) ? 1 : 0;
        return found;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int found = inputStream.read(b);
        this.count += (found >= 0) ? found : 0;
        return found;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int found = inputStream.read(b, off, len);
        this.count += (found >= 0) ? found : 0;
        return found;
    }

    @Override
    public long skip(long n) throws IOException {
        final long skip = inputStream.skip(n);
        this.count += skip;
        return skip;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }
}
