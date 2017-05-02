package com.alex.epgmatcher.beans;

/**
 * Class for storing result of founded EPG
 * Created by Alex on 18.04.2017.
 */
public class FindResult implements Comparable<FindResult> {
    private final EPG epg;
    private double rate;
    private String[] searchWords;

    public FindResult(EPG epg, double rate) {
        searchWords = new String[0];
        if (epg == null) throw new IllegalArgumentException("EPG is null");
        this.epg = epg;
        this.rate = rate;
    }

    public EPG getEpg() {
        return epg;
    }

    public double getRate() {
        return rate;
    }

    public String[] getSearchWords() {
        return searchWords;
    }

    public void setSearchWords(String[] searchWords) {
        if (searchWords != null) {
            this.searchWords = searchWords;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FindResult that = (FindResult) o;

        return epg.equals(that.epg);
    }

    @Override
    public int hashCode() {
        return epg.hashCode();
    }

    @Override
    public int compareTo(FindResult o) {
        if (o == null) return -1;
        return rate > o.rate ? -1 : rate < o.rate ? 1 : epg.compareTo(o.epg);
    }


    public void setRate(double rate) {
        this.rate = rate;
    }
}
