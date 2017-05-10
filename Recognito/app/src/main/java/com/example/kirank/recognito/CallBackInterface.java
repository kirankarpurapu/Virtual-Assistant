package com.example.kirank.recognito;

import org.json.JSONObject;

/**
 * Created by kirank on 5/7/17.
 */

public interface CallBackInterface {

    public void onSuccess(JSONObject networkCallResponseSuccess);

    public void onFailure(JSONObject networkCallResponseFailure);
}


