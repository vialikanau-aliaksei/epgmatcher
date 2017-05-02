package com.alex.epgmatcher.beans;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.alex.epgmatcher.util.EPGFinder.RATE_THRESHOLD;

/**
 * Class for storing channel data.
 * Created by Alex on 17.04.2017.
 */
public class Channel implements Serializable {
    private static final double FULL_MATCH_THRESHOLD = 0.9;
    private static final String NEW_LINE = "\r\n";
    private static final int MAX_LINE_LENGTH = 80;
    private static final double MATCH_RATE_THRESHOLD = 0.6;
    /**
     * Find result with higher rate would be first.
     */
    private final SortedSet<FindResult> findResults;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private String id;
    private String name;
    private String url;

    public Channel() {
        numberFormat.setMaximumFractionDigits(3);
        id = "";
        name = "";
        url = "";
        findResults = new TreeSet<>();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public EPG getEpg() {
        if (findResults.isEmpty() || findResults.first().getRate() < MATCH_RATE_THRESHOLD) {
            return null;
        } else {
            return findResults.first().getEpg();
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url != null ? url : "";
    }

    public EqualityType getEqualityType() {
        return EqualityType.getType(findResults);
    }

    public String getEPGName() {
        return findResults.isEmpty() ? "" : findResults.first().getEpg().getName();
    }

    public SortedSet<FindResult> getFindResults() {
        return findResults;
    }

    public void setFindResults(SortedSet<FindResult> results) {
        findResults.clear();
        findResults.addAll(results);
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("Search words: ");
        double rate;
        String[] searchWords;
        Iterator<FindResult> iterator = findResults.iterator();
        if (iterator.hasNext()) {
            FindResult findResult = iterator.next();
            searchWords = findResult.getSearchWords();
            rate = findResult.getRate();
        } else {
            searchWords = new String[]{name};
            rate = 0.0;
        }
        for (String word : searchWords) {
            stringBuffer.append(word).append(" ");
        }
        stringBuffer.setLength(stringBuffer.length() - 1);
        stringBuffer.append(" (").append(numberFormat.format(rate)).append(")");
        if (iterator.hasNext()) {
            stringBuffer.append(NEW_LINE).append("Same EPGs: ").append(NEW_LINE);
            int prev = stringBuffer.length();
            while (iterator.hasNext()) {
                FindResult sameResult = iterator.next();
                String epgName = sameResult.getEpg() != null ? sameResult.getEpg().getName() : "";
                stringBuffer.append(epgName).append(" (").
                        append(numberFormat.format(sameResult.getRate())).append("), ");
                if (stringBuffer.length() - prev > MAX_LINE_LENGTH) {
                    stringBuffer.append(NEW_LINE);
                    prev = stringBuffer.length();
                }
            }
            stringBuffer.setLength(stringBuffer.length() - 2);
        }
        return stringBuffer.toString();
    }

    public enum EqualityType {
        NONE,
        PARTIAL,
        FULL,
        MANUAL;

        public static EqualityType getType(SortedSet<FindResult> findResults) {
            if (findResults.isEmpty()) {
                return NONE;
            }
            Iterator<FindResult> iterator = findResults.iterator();
            double rate = iterator.next().getRate();
            double nextRate = iterator.hasNext() ? iterator.next().getRate() : 0;
            if (rate > 1.0) {
                return MANUAL;
            } else if (rate > FULL_MATCH_THRESHOLD && Math.abs(rate - nextRate) > RATE_THRESHOLD) {
                return FULL;
            } else if (rate >= RATE_THRESHOLD) {
                return PARTIAL;
            } else {
                return NONE;
            }
        }

        public boolean isMoreOrEqual(EqualityType type) {
            return ordinal() >= type.ordinal();
        }
    }

}
