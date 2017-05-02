package com.alex.epgmatcher;

import com.alex.epgmatcher.beans.Channel;
import com.alex.epgmatcher.beans.EPG;
import com.alex.epgmatcher.beans.FindResult;
import com.alex.epgmatcher.util.EPGFinder;

import java.util.*;
import java.util.logging.Logger;

import static com.alex.epgmatcher.beans.Channel.EqualityType.FULL;
import static com.alex.epgmatcher.beans.Channel.EqualityType.PARTIAL;


/**
 * Main class for matching EPGs.
 * Created by Alex on 19.04.2017.
 */
public final class Matcher {
    private final static Logger logger = Logger.getGlobal();
    private static final String MSG_PARTIAL_MATCH = "Partial match: %s -> %s";
    private static final String CR = "\r\n";
    private static final String[] STOP_WORDS = {"FM", "ФМ", "РАДИО", "RADIO"};

    private Matcher() {

    }

    /**
     * Set EPG matched to channel for each channel in given list
     *
     * @param channels list of {@link Channel}
     * @param epgs     list of {@link EPG}
     * @param mapping  map of previously matched epg to channels
     */
    public static void matchEpg(List<Channel> channels, List<EPG> epgs, Map<String, String> mapping) {
        logger.finest("Start matching...");
        Map<String, EPG> epgChannelMap = new HashMap<>();
        for (EPG epgChannel : epgs) {
            epgChannelMap.put(epgChannel.getName(), epgChannel);
        }
        Map<String, EPG> epgMap = new HashMap<>();
        Map<String, String[]> epgWordsMap = new HashMap<>();
        for (Map.Entry<String, EPG> entry : epgChannelMap.entrySet()) {
            String name = entry.getKey().toUpperCase().replaceAll(EPGFinder.NON_WORD_CHAR, "");
            epgWordsMap.put(name, EPGFinder.getWords(entry.getKey().toUpperCase()));
            epgMap.put(name, entry.getValue());
        }

        EPGFinder channelMatcher = new EPGFinder(epgMap, epgWordsMap, mapping);
        Map<String, Channel> fullMatchMap = new HashMap<>();
        for (Channel channel : channels) {
            // include non radio channels
            if (!hasStopWord(channel.getName().toUpperCase())) {
                channel.setFindResults(channelMatcher.findEPG(channel.getName()));
                if (channel.getEqualityType().isMoreOrEqual(FULL)) {
                    fullMatchMap.put(channel.getEpg().getName(), channel);
                }
                if (channel.getEqualityType() == PARTIAL) {
                    logger.finest(String.format(MSG_PARTIAL_MATCH, channel.getName(), channel.getEPGName()));
                }
            }
        }
        // remove existing full matched epg from partial matched
        for (Channel channel : channels) {
            if (channel.getEqualityType() == PARTIAL) {
                if (fullMatchMap.get(channel.getEPGName()) != null) {
                    Iterator<FindResult> iterator = channel.getFindResults().iterator();
                    while (iterator.hasNext()) {
                        FindResult result = iterator.next();
                        if (fullMatchMap.get(result.getEpg().getName()) != null) {
                            iterator.remove();
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        printResult(channels);
    }

    private static boolean hasStopWord(String name) {
        for (String stopWord : STOP_WORDS) {
            if (name.contains(stopWord)) {
                return true;
            }
        }
        return false;
    }

    private static void printResult(List<Channel> channels) {
        StringBuilder partialBuffer = new StringBuilder();
        StringBuilder noneBuffer = new StringBuilder();
        int fullMatched = 0;
        int partialMatched = 0;
        for (Channel channel : channels) {
            switch (channel.getEqualityType()) {
                case FULL: {
                    fullMatched++;
                    break;
                }
                case PARTIAL: {
                    partialBuffer.append(channel.getName()).append(": ").append(channel.getEPGName());
                    partialBuffer.append(CR);
                    break;
                }
                default: {
                    noneBuffer.append(channel.getName()).append(CR);
                }
            }
        }
        logger.fine("Fully matched: " + fullMatched + CR);
        logger.fine("Partial matched - " + partialMatched + ":" + CR + partialBuffer.toString() + CR);
        int count = (channels.size() - fullMatched - partialMatched);
        logger.fine("Non matched - " + count + ":" + CR + noneBuffer.toString());
    }
}
