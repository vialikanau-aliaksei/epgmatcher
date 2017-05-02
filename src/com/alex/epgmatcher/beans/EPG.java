package com.alex.epgmatcher.beans;

import java.io.Serializable;

/**
 * Class for storing EPG data.
 * Created by Alex on 18.04.2017.
 */
public class EPG implements Serializable, Comparable<EPG> {

    private String id;
    private String name;
    private String lang;

    public EPG() {
        id = "";
        name = "";
        lang = "";
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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang != null ? lang : "";
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EPG epg = (EPG) o;

        return name != null ? name.equals(epg.name) : epg.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public int compareTo(EPG o) {
        return name.toUpperCase().compareTo(o.getName().toUpperCase());
    }
}
