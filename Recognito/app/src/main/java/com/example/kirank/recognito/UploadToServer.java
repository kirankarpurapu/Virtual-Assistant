package com.example.kirank.recognito;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by kirank on 5/7/17.
 */

public class UploadToServer {

    private UploadToServer() {
    }

    public static String uploadNewImage(RecognitoImage image) {
        String resultString = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Constants.NEW_IMAGE_URL);
            Part[] parts = {
                    new FilePart("file", image.getImageFile()),
//                    new StringPart("base64", image.getData()),
                    new StringPart("contactid", ""+image.getId()),
                    new StringPart("ImageName", image.getPersonName() + ".jpg")
            };
            MultipartEntity multipartEntity = new MultipartEntity(parts, httppost.getParams());
            httppost.setEntity(multipartEntity);
            HttpResponse response = httpclient.execute(httppost);
            resultString = EntityUtils.toString(response.getEntity());
            Log.d(Constants.SERVER_UPLOAD_TAG, "In the try Loop" + resultString);
        } catch (Exception e) {
            Log.d(Constants.SERVER_UPLOAD_TAG, "Error in http connection " + e.toString());
        }
        return resultString;
    }

    public static void uploadNewImage(final Context context, final RecognitoImage image, final CallBackInterface callBack) {
        Log.d(Constants.SERVER_UPLOAD_TAG, "Trying to upload the image");
        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                String resultString = uploadNewImage(image);
                callBack.callback(resultString);
            }
        });
        uploadThread.start();
    }
}