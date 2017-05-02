package com.alex.epgmatcher.util;

import com.alex.epgmatcher.beans.Channel;
import com.alex.epgmatcher.beans.EPG;
import com.alex.epgmatcher.beans.FindResult;

import java.util.*;

/**
 * Finds EPG for given channel
 * Created by Alex on 18.04.2017.
 */
public class EPGFinder {
    public static final double RATE_THRESHOLD = 0.45;
    public static final String NON_WORD_CHAR = "[^А-ЯA-Z0-9]+";
    private static final double FACTOR_DELTA_LENGTH = 0.02;
    private final static double MISSED_WORDS_FACTOR = 0.04;
    private static final int MAX_RESULTS = 10;
    private static final int MIN_WORD_LENGTH = 2;
    private static final double REPLACE_BY_SYNONYM_FACTOR = 0.9;
    private static final double WORD_ORDER_FACTOR = 0.2;
    private final Map<String, String> mapping;
    private final Map<String, String[]> epgWordsMap;
    private final Map<String, EPG> epgMap;

    private final Synonyms synonyms;


    /**
     * Constructs new {@link EPGFinder} with given arguments.
     *
     * @param epgWordsMap map of EPG name to {@link EPG} name split by words
     * @param epgMap      map of EPG name to {@link EPG} object
     * @param mapping     previous mapping of EPGs to channels.
     */
    public EPGFinder(Map<String, EPG> epgMap, Map<String, String[]> epgWordsMap, Map<String, String> mapping) {
        this.epgMap = epgMap;
        this.epgWordsMap = epgWordsMap;
        this.mapping = mapping != null ? mapping : new HashMap<>();
        synonyms = new Synonyms();
    }

    /**
     * Split given argument by words by Regex {@link EPGFinder#NON_WORD_CHAR}
     *
     * @param phrase phrase to split.
     * @return splitted phrase
     */
    public static String[] getWords(String phrase) {
        String[] split = phrase.split(NON_WORD_CHAR);
        StringBuilder prev = new StringBuilder();
        List<String> list = new ArrayList<>();
        for (String str : split) {
            if (str.length() < MIN_WORD_LENGTH) {
                prev.append(str);
            } else {
                if (prev.length() > 0) {
                    list.add(prev.toString());
                }
                prev.setLength(0);
                prev.append(str);
            }
        }
        if (prev.length() > 0) {
            list.add(prev.toString());
        }
        return list.toArray(new String[]{});
    }

    /**
     * find {@link EPG} by given argument.
     *
     * @param channelName name of channel for searching.
     * @return {@link FindResult} if {@link EPG} was founded or null otherwise
     */
    public SortedSet<FindResult> findEPG(String channelName) {
        SortedSet<FindResult> results;

        String key = mapping.get(channelName);
        if (key != null) {
            results = findByChannelName(key.toUpperCase().replaceAll(NON_WORD_CHAR, ""));
            if (!results.isEmpty()) {
                return results;
            }
        }

        key = channelName.toUpperCase().replaceAll(NON_WORD_CHAR, "");
        results = findByChannelName(key);
        if (!results.isEmpty()) {
            return results;
        }


        return findByWords(channelName.toUpperCase());
    }

    /**
     * Split given argument by words and find {@link EPG}.
     *
     * @param channelName channel's name for EPG finding.
     * @return {@link FindResult} if {@link EPG} was founded or null otherwise
     */
    private SortedSet<FindResult> findByWords(String channelName) {
        Map<String, FindResult> resultMap = new HashMap<>();

        String[] channelJoinedWords = new String[]{channelName.replaceAll(NON_WORD_CHAR, "")};
        String[] words = getWords(channelName);
        for (Map.Entry<String, String[]> entry : epgWordsMap.entrySet()) {
            EPG epg = epgMap.get(entry.getKey());

            // search by joined words without delimeters
            String[] epgJoindeWords = new String[]{entry.getKey().replaceAll(NON_WORD_CHAR, "")};
            double rate = getRate(channelJoinedWords, epgJoindeWords);
            addRate(resultMap, words, epg, rate);

            // search by words
            rate = getRate(words, entry.getValue());
            addRate(resultMap, words, epg, rate);

        }
        List<FindResult> findResults = new ArrayList<>(resultMap.values());
        Collections.sort(findResults);
        return new TreeSet<>(findResults.subList(0, Math.min(MAX_RESULTS, findResults.size())));
    }

    private void addRate(Map<String, FindResult> results, String[] words, EPG epg, double rate) {
        if (rate >= RATE_THRESHOLD) {
            FindResult result = new FindResult(epg, rate);
            result.setSearchWords(words);
            FindResult prev = results.get(epg.getName());
            if (prev == null || prev.getRate() < result.getRate()) {
                results.put(epg.getName(), result);
            }
        }
    }

    /**
     * find {@link EPG} by given argument.
     *
     * @param phrase phrase for searching
     * @return {@link FindResult} if {@link EPG} was founded or null otherwise
     */
    private SortedSet<FindResult> findByChannelName(String phrase) {
        SortedSet<FindResult> results = new TreeSet<>();
        EPG epg = epgMap.get(phrase);
        if (epg != null) {
            FindResult findResult = new FindResult(epg, 1.0);
            findResult.setSearchWords(new String[]{phrase});
            results.add(findResult);
        }
        return results;
    }

    /**
     * Calculate rate by similarity of arguments.
     *
     * @param channelKeyWords {@link Channel} name divided by words.
     * @param epgKeyWords     {@link EPG} name divided by words.
     * @return rate from 0.0 to 1.0. 1.0 means words has 100% similarity.
     */
    private double getRate(String[] channelKeyWords, String[] epgKeyWords) {
        double matched = 0.0;
        double total = 0.0;
        List<String> chWordList = new ArrayList<>(Arrays.asList(channelKeyWords));
        List<String> epgWordList = new ArrayList<>(Arrays.asList(epgKeyWords));
        double wordOrderFactor = channelKeyWords.length - 1;
        int index = 0;
        for (Iterator<String> itCH = chWordList.iterator(); itCH.hasNext(); ) {
            String chNext = itCH.next();
            String chWord = synonyms.getSynonym(chNext);
            double factor = 1.0 + (wordOrderFactor > 0 ? ((wordOrderFactor - index) / wordOrderFactor) : 0) * WORD_ORDER_FACTOR;
            if (!chWord.equals(chNext)) {
                factor *= REPLACE_BY_SYNONYM_FACTOR;
            }
            total += factor;
            for (Iterator<String> itEPG = epgWordList.iterator(); itEPG.hasNext(); ) {
                String epgNext = itEPG.next();
                String epgWord = synonyms.getSynonym(epgNext);
                int length = Math.min(chWord.length(), epgWord.length());
                if (chWord.substring(0, length).equals(epgWord.substring(0, length))) {
                    double deltaLength = epgWord.length() - chWord.length();
                    matched += factor * (double) length / chWord.length() - (deltaLength * FACTOR_DELTA_LENGTH);
                    if (!epgWord.equals(epgNext)) {
                        matched *= REPLACE_BY_SYNONYM_FACTOR;
                    }
                    //total += (double) chWord.length();
                    itCH.remove();
                    itEPG.remove();
                    break;
                }
            }
            index++;
        }
        return matched / total - ((double) epgWordList.size() * MISSED_WORDS_FACTOR);
    }
}
