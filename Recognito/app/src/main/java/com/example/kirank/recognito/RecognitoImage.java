package com.example.kirank.recognito;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by kirank on 5/7/17.
 */


public class RecognitoImage {

    private final String data;
    private final int id;
    private final File imageFile;
    private final String personName;

    public RecognitoImage(String data, int id, File imageFile, String personName) {

        this.data = data;
        this.id = id;
        this.imageFile = imageFile;
        this.personName = personName;


    }

    public String getData() {
        return this.data;
    }

    public String getPersonName() {
        return personName;
    }

    public File getImageFile() {
        return imageFile;
    }
    public int getId() {return this.id;}
}
