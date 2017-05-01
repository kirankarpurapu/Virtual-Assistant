package com.example.kirank.recognito;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by kirank on 4/17/17.
 */

public class Image {

    private final String data;
    private final String name;
    private final JSONObject info;
    private final File imageFile;

    public Image(String data, String name, JSONObject info, File file) {
        this.name = name;
        this.data = data;
        this.info = info;
        this.imageFile = file;
    }

    public String getName() {
        return this.name;
    }
    public String getData() {
        return this.data;
    }
    public JSONObject getInfo() {return this.info;}
    public File getFile() {return this.imageFile;}
}
