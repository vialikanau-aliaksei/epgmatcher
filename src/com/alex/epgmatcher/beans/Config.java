package com.alex.epgmatcher.beans;

import java.io.Serializable;

/**
 * Class for storing config data.
 * Created by Alex on 20.04.2017.
 */
public class Config implements Serializable {

    private String m3uUrl;
    private String epgUrl;
    private String outputFilename;

    public Config() {
        m3uUrl = "";
        epgUrl = "";
        outputFilename = "";
    }

    public String getM3uUrl() {
        return m3uUrl;
    }

    public void setM3uUrl(String m3uUrl) {
        this.m3uUrl = m3uUrl;
    }

    public String getEpgUrl() {
        return epgUrl;
    }

    public void setEpgUrl(String epgUrl) {
        this.epgUrl = epgUrl;
    }

    public String getOutputFilename() {
        return outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

}
