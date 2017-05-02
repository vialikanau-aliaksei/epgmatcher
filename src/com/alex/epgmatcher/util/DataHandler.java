package com.alex.epgmatcher.util;

import com.alex.epgmatcher.beans.Channel;
import com.alex.epgmatcher.beans.EPG;
import com.alex.epgmatcher.beans.InputStreamReaderData;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * Utility class for loading and saving data.
 * Created by Alex on 19.04.2017.
 */
public final class DataHandler {
    private static final String SAX_VALIDATION = "http://xml.org/sax/features/validation";
    private static final String APACHE_VALIDATION = "http://apache.org/xml/features/validation/schema";
    private static final String EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private static final String EXTINF = "#EXTINF:";
    private static final String NEW_LINE = "\r\n";
    private static final String EXTM3U = "#EXTM3U";
    private static final String TVG_NAME = " tvg-name=\"";
    private static final String QUOTE = "\"";
    private static final String DOWNLOADING_EPG = "Downloading EPG...";
    private static final String EPG_SUCCESSFULLY_DOWNLOADED = "EPG successfully downloaded.";
    private static final String DOWNLOADING_CHANNELS = "Downloading channels...";
    private static final String CHANNELS_SUCCESSFULLY_DOWNLOADED = "Channels successfully downloaded.";
    private static final String CREATING_M3U = "Creating m3u...";
    private static final String M3U_SUCCESSFULLY_SAVED = "M3U successfully saved.";

    private static final String ERROR_CONFIG_READ = "Error on load config: ";
    private final static Logger logger = Logger.getGlobal();
    private final static Proxy proxy = getProxy();

    private DataHandler() {
    }

    /**
     * Retrieve EPG list from given URL.
     *
     * @param reader reader for retrieving data.
     * @return list of {@link EPG}
     * @throws IOException  Any I/O exception of some sort has occurred.
     * @throws SAXException Any SAX exception, possibly wrapping another exception.
     */
    public static List<EPG> getEPG(Reader reader) throws IOException, SAXException {
        List<EPG> epgNames;
        logger.finest(DOWNLOADING_EPG);
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setFeature(EXTERNAL_DTD, false);
        InputSource source = new InputSource(reader);
        EPGXmlHandler handler = new EPGXmlHandler();
        xmlReader.setContentHandler(handler);
        xmlReader.setFeature(SAX_VALIDATION, false);
        xmlReader.setFeature(APACHE_VALIDATION, false);
        xmlReader.parse(source);
        epgNames = handler.getResults();
        logger.finest(EPG_SUCCESSFULLY_DOWNLOADED);
        return epgNames;
    }


    public static InputStreamReaderData getInputStreamData(String uri) throws IOException {
        URL url = new URL(uri);
        URLConnection connection;
        if (proxy != null) {
            connection = url.openConnection(proxy);
        } else {
            connection = url.openConnection();
        }
        connection.connect();
        return new InputStreamReaderData(connection);
    }

    /**
     * Retrieve channel list from given URL.
     *
     * @param reader reader for retrieving data.
     * @return list of {@link Channel}
     */
    public static List<Channel> getChannels(Reader reader){
        List<Channel> channels = new ArrayList<>();
        logger.finest(DOWNLOADING_CHANNELS);

        try (Scanner scanner = new Scanner(reader)) {
            while (scanner.hasNext()) {
                String nextLine = scanner.nextLine();
                if (nextLine.startsWith(EXTINF)) {
                    nextLine = nextLine.substring(EXTINF.length(), nextLine.length());
                    Channel channel = new Channel();
                    String[] split = nextLine.split(",", 2);
                    if (split.length != 2) continue;
                    channel.setId(split[0]);
                    channel.setName(split[1]);

                    if (scanner.hasNextLine()) {
                        channel.setUrl(scanner.nextLine());
                    }
                    channels.add(channel);
                }
            }
            scanner.close();
        }
        logger.finest(CHANNELS_SUCCESSFULLY_DOWNLOADED);
        return channels;
    }

    private static Proxy getProxy() {
        try {
            System.setProperty("java.net.useSystemProxies", "true");
            List<Proxy> l = ProxySelector.getDefault().select(new URI("http://www.google.com/"));
            for (Proxy proxy : l) {
                logger.finest("Proxy: " + proxy.type());
                InetSocketAddress addr = (InetSocketAddress) proxy.address();
                if (addr != null) {
                    return proxy;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Create M3U text data from given argument.
     *
     * @param channels list of {@link Channel}
     * @return string containing M3U file data.
     */
    private static String createM3U(List<Channel> channels) {
        StringBuilder result = new StringBuilder();
        result.append(EXTM3U + NEW_LINE);
        for (Channel channel : channels) {
            result.append(EXTINF);
            result.append(channel.getId());
            if (!channel.getEPGName().isEmpty()) {
                result.append(TVG_NAME).append(channel.getEPGName()).append(QUOTE);
            }
            result.append(",").append(channel.getName());

            result.append(NEW_LINE);
            result.append(channel.getUrl()).append(NEW_LINE);
        }
        return result.toString();
    }

    /**
     * save channel data to EXTINF M3U file
     *
     * @param channels list of {@link Channel}
     * @param file     name of m3u file
     * @throws IOException Any I/O exception of some sort has occurred.
     */
    public static void saveM3U(List<Channel> channels, File file) throws IOException {
        logger.finest(CREATING_M3U);
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
             BufferedWriter writer = new BufferedWriter(osw)) {
            writer.write(createM3U(channels));
            writer.flush();
            writer.close();
        }
        logger.finest(M3U_SUCCESSFULLY_SAVED);
    }

    /**
     * Load object from XML file.
     *
     * @param defaultValue default value for returning if exception was occurred.
     * @param filename     name of XML file with data
     * @param <T>          type of loaded object
     * @return new instance of <T> or default value if exception was occured.
     */
    public static <T extends Serializable> T load(T defaultValue, String filename) {
        try (FileInputStream fis = new FileInputStream(filename)) {
            BufferedInputStream bis = new BufferedInputStream(fis);
            XMLDecoder xmlDecoder = new XMLDecoder(bis);
            return (T) xmlDecoder.readObject();
        } catch (Exception e) {
            logger.severe(ERROR_CONFIG_READ + e.getMessage());
            try {
                save(defaultValue, filename);
            } catch (IOException e1) {
                logger.severe(ERROR_CONFIG_READ + e.getMessage());
            }
            return defaultValue;
        }
    }

    /**
     * Save object in XML file.
     *
     * @param value    object to saving.
     * @param filename name of XML file for saving.
     * @param <T>      type of object
     * @throws IOException Any I/O exception of some sort has occurred.
     */
    public static <T extends Serializable> void save(T value, String filename) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            XMLEncoder xmlEncoder = new XMLEncoder(bos);
            xmlEncoder.writeObject(value);
            xmlEncoder.close();
        }
    }
}
