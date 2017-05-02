package com.alex.epgmatcher.beans;

import com.alex.epgmatcher.util.CountingInputStream;

import java.io.*;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * Class providing {@link InputStream} from {@link URLConnection} wrapped to {@link CountingInputStream},
 * count of bytes read from InputStream and total size in bytes of content
 * Created by Alex on 21.04.2017.
 */
public class InputStreamReaderData {
    private final CountingInputStream countingInputStream;
    private final Reader reader;
    private final int size;
    private static final String GZ = ".gz";

    /**
     * Constructs new InputStreamReaderData.
     * If content have ".gz" extension returning InputStream is wrapped to {@link GZIPInputStream}
     *
     * @param connection URLConnection for retrieving InputStream
     * @throws IOException if an I/O error has occurred
     */
    public InputStreamReaderData(URLConnection connection) throws IOException {
        this.size = connection.getContentLength();
        this.countingInputStream = new CountingInputStream(connection.getInputStream());
        String file = connection.getURL().getFile();
        InputStream inputStream;
        if (file != null && file.toLowerCase().endsWith(GZ)) {
            inputStream = new GZIPInputStream(countingInputStream);
        } else {
            inputStream = countingInputStream;
        }

        reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        skipBOM(reader);

    }

    private void skipBOM(Reader reader) throws IOException {
        if (reader.ready()) {
            reader.mark(1);
            char[] possibleBOM = new char[1];
            reader.read(possibleBOM);
            if (possibleBOM[0] != '\ufeff') {
                reader.reset();
            }
        }
    }


    /**
     * @return {@link InputStream} form {@link URLConnection}
     */
    public Reader getReader() {
        return reader;
    }

    /**
     * @return count of bytes read from connections InputStream
     */
    public int getCount() {
        return countingInputStream.getCount();
    }


    /**
     * @return size in bytes of URLConnection content
     */
    public int getSize() {
        return size;
    }


}
