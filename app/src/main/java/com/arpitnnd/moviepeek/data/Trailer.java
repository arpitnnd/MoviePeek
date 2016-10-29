package com.arpitnnd.moviepeek.data;

import org.parceler.Parcel;

@Parcel
public class Trailer {

    private String name, key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTrailerName() {
        return name;
    }

    public void setTrailerName(String name) {
        this.name = name;
    }

}
