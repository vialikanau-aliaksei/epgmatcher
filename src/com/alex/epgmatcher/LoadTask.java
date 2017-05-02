package com.alex.epgmatcher;

import com.alex.epgmatcher.beans.Channel;
import com.alex.epgmatcher.beans.EPG;
import com.alex.epgmatcher.beans.InputStreamReaderData;
import com.alex.epgmatcher.beans.LoadData;
import com.alex.epgmatcher.util.DataHandler;
import javafx.concurrent.Task;

import java.util.List;

/**
 * Class for loading M3U playlist and EPG guide from given URLs.
 * Created by Alex on 21.04.2017.
 */
public class LoadTask extends Task<LoadData> {

    private static final int TIMEOUT = 500;
    private final String m3uUrl;
    private final String epgUrl;

    /**
     * Constructs new LoadTask.
     *
     * @param m3uUrl url of M3U playlist
     * @param epgUrl url of EPG guide
     */
    public LoadTask(String m3uUrl, String epgUrl) {
        this.m3uUrl = m3uUrl;
        this.epgUrl = epgUrl;
        updateProgress(0, 1.0);
    }


    @Override
    protected LoadData call() throws Exception {
        updateMessage("Loading M3U from " + m3uUrl);
        InputStreamReaderData streamData = DataHandler.getInputStreamData(m3uUrl);
        Thread thread = new ProgressThread(streamData);
        thread.start();
        List<Channel> channels = DataHandler.getChannels(streamData.getReader());
        updateMessage("Loading EPG from " + epgUrl);
        thread.interrupt();

        streamData = DataHandler.getInputStreamData(epgUrl);
        thread = new ProgressThread(streamData);
        thread.start();
        List<EPG> epgs = DataHandler.getEPG(streamData.getReader());
        thread.interrupt();
        updateProgress(1);
        return new LoadData(channels, epgs);
    }

    private void updateProgress(double value) {
        updateProgress(value, 1.0);
    }

    /**
     * Class for updating the progress property
     */
    private class ProgressThread extends Thread {

        private final InputStreamReaderData streamData;

        private ProgressThread(InputStreamReaderData streamData) {
            this.streamData = streamData;
        }

        @Override
        public void run() {
            double count = 0;
            double size = 1;
            while (!isInterrupted() && count < size) {
                try {
                    Thread.sleep(TIMEOUT);
                } catch (InterruptedException ignored) {
                }
                count = streamData.getCount();
                size = streamData.getSize();
                updateProgress(count / size);
            }
        }
    }
}
