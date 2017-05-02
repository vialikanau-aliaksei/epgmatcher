package com.alex.epgmatcher.beans;

import java.util.List;

/**
 * Class for storing data from {@link com.alex.epgmatcher.LoadTask}
 * Created by Alex on 21.04.2017.
 */
public class LoadData {
    private final List<Channel> channels;
    private final List<EPG> epgs;

    public LoadData(List<Channel> channels, List<EPG> epgs) {
        this.channels = channels;
        this.epgs = epgs;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public List<EPG> getEpgs() {
        return epgs;
    }
}
