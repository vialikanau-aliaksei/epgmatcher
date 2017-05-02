package com.alex.epgmatcher.util;

import java.util.*;

/**
 * Class provide synonyms.
 * Created by Alex on 25.04.2017.
 */
public class Synonyms {
    private static final String SYNONYMS_XML = "synonyms.xml";
    private final HashSet<TreeSet<String>> synonymsSet;
    private final Map<String, String> synonymMap;

    /**
     * Constructs new instance of Synonyms
     */
    public Synonyms() {
        synonymMap = new HashMap<>();
        synonymsSet = DataHandler.load(SynonymsPreset.getSynonymSet(), SYNONYMS_XML);
        synonymsSet.addAll(SynonymsPreset.getSynonymSet());
    }

    /**
     * Search synonym of given argument
     * @param word argument for which synonym will be searched
     * @return first synonym of given argument if it exists otherwise return given argument
     */
    public String getSynonym(String word) {
        if (word == null) return null;
        String upperCasedWord = word.toUpperCase();
        String mapValue = synonymMap.get(upperCasedWord);
        if (mapValue != null) return mapValue;
        for (TreeSet<String> synonyms : synonymsSet) {
            if (synonyms.contains(upperCasedWord)) {
                synonymMap.put(upperCasedWord, synonyms.first());
                return synonyms.first();
            }
        }
        return word;
    }

    /**
     * Synonyms preset.
     */
    private enum SynonymsPreset {
        TV("ТЕЛЕВИДЕНИЕ", "ТВ", "ТЕЛЕКАНАЛ"),
        CHANNEL("КАНАЛ", "ТЕЛЕКАНАЛ"),
        RU("РУССКИЙ", "RUSSIAN"),
        FIRST("1Й", "1ЫЙ", "ПЕРВЫЙ", "1ST"),
        SECOND("2Й", "2ОЙ", "ВТОРОЙ", "2ND"),
        THIRD("3Й", "3ИЙ", "ТРЕТИЙ", "3RD"),
        FOURTH("4Й", "4ЫЙ", "ЧЕТВЕРТЫЙ", "4TH"),
        FIFTH("5Й", "5ЫЙ", "ПЯТЫЙ", "5TH"),
        SIXTH("6Й", "6ОЙ", "ШЕСТОЙ", "6TH"),
        SEVENTH("7Й", "7ОЙ", "СЕДЬМОЙ", "7TH"),
        EIGHTH("8Й", "8ОЙ", "ВОСЬМОЙ", "8TH"),
        NINTH("9Й", "9ЫЙ", "ДЕВЯТЫЙ", "9TH"),
        TENTH("10Й", "10ЫЙ", "ДЕСЯТЫЙ", "10TH");

        private final String[] values;

        SynonymsPreset(String... synonyms) {
            this.values = synonyms;
        }

        public boolean isSynonym(String word) {
            String upperCaseWord = word.toUpperCase();
            if (name().equals(upperCaseWord)) return true;
            for (String synonym : values) {
                if (synonym.equals(upperCaseWord)) {
                    return true;
                }
            }
            return false;
        }

        private static HashSet<TreeSet<String>> getSynonymSet() {
            HashSet<TreeSet<String>> synonymSet = new HashSet<>();
            for (SynonymsPreset preset : values()) {
                TreeSet<String> synonyms = new TreeSet<>();
                synonyms.add(preset.name());
                synonyms.addAll(Arrays.asList(preset.values));
                synonymSet.add(synonyms);
            }
            return synonymSet;
        }
    }


}
