package com.example.kirank.recognito;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kirank on 5/7/17.
 */

public class UploadToServer {

    private UploadToServer() {
    }

    public static JSONObject uploadNewImage(RecognitoImage image) {
        String resultString = null;
        JSONObject jsonResult = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Constants.NEW_IMAGE_URL);
            Part[] parts = {
                    new FilePart("file", image.getImageFile()),
                    new StringPart("contactid", ""+image.getId()),
                    new StringPart("ImageName", image.getPersonName() + ".jpg")
            };
            MultipartEntity multipartEntity = new MultipartEntity(parts, httppost.getParams());
            httppost.setEntity(multipartEntity);
            HttpResponse response = httpclient.execute(httppost);
            resultString = EntityUtils.toString(response.getEntity());
            jsonResult = new JSONObject(resultString);
            Log.d(Constants.SERVER_UPLOAD_TAG, "In the try Loop" + resultString);
        } catch (Exception e) {
            Log.d(Constants.SERVER_UPLOAD_TAG, "Error in http connection " + e.toString());
        }
        return jsonResult;
    }

    public static JSONObject uploadToTestImage(String base64String) {
        String resultString = null;
        JSONObject resultJson = null;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("base64", base64String));
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Constants.TEST_IMAGE_URL);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            resultString = EntityUtils.toString(response.getEntity());
            resultJson = new JSONObject(resultString);
            Log.d(Constants.SERVER_UPLOAD_TAG, "In the try Loop" + resultString);
        } catch (Exception e) {
            Log.d(Constants.SERVER_UPLOAD_TAG, "Error in http connection " + e.toString());
        }
        return resultJson;
    }

    public static void uploadNewImage(final Context context, final RecognitoImage image, final CallBackInterface callBack) {
        Log.d(Constants.SERVER_UPLOAD_TAG, "Trying to upload the image");
        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                JSONObject resultJson = uploadNewImage(image);
                callBack.callback(resultJson);
            }
        });
        uploadThread.start();
    }

    public static void uploadToTestImage(final Context context, final String base64String, final CallBackInterface callBack) {
        Log.d(Constants.SERVER_UPLOAD_TAG, "Trying to upload to test the image");
        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                JSONObject resultJson = uploadToTestImage(base64String);
                callBack.callback(resultJson);
            }
        });
        uploadThread.start();
    }

}