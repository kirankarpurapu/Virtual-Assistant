package com.example.kirank.recognito;

import org.json.JSONObject;

/**
 * Created by kirank on 4/17/17.
 */

public class Image {

    private final String data;
    private final String name;
    private final JSONObject info;

    public Image(String data, String name, JSONObject info) {
        this.name = name;
        this.data = data;
        this.info = info;
    }

    public String getName() {
        return this.name;
    }
    public String getData() {
        return this.data;
    }
    public JSONObject getInfo() {return this.info;}
}
