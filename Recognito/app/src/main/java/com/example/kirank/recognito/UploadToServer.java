package com.example.kirank.recognito;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kirank on 5/7/17.
 */

public class UploadToServer {

    private UploadToServer() {
    }

    /**
     *
     * @param image
     * @return JsonObject result object after uploading a new image to add to the database
     */
    private static JSONObject uploadNewImage(RecognitoImage image) {
        String resultString = null;
        JSONObject jsonResult = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(Constants.NEW_IMAGE_URL);
            Part[] parts = {
                    new FilePart("file", image.getImageFile()),
                    new StringPart("contactid", "" + image.getId()),
                    new StringPart("imagename", image.getPersonName() + System.currentTimeMillis()+ ".jpg"),
            };
            MultipartEntity multipartEntity = new MultipartEntity(parts, httppost.getParams());
            httppost.setEntity(multipartEntity);
            HttpResponse response = httpclient.execute(httppost);
            resultString = EntityUtils.toString(response.getEntity());
            Log.d(Constants.SERVER_UPLOAD_TAG, "result from new image upload " + resultString);
            jsonResult = new JSONObject(resultString);
            Log.d(Constants.SERVER_UPLOAD_TAG, "In the try Loop" + resultString);
        } catch (Exception e) {
            Log.d(Constants.SERVER_UPLOAD_TAG, "Error in http connection while adding new image" + e.toString());
        }
        return jsonResult;
    }

    /**
     *
     * @param base64String
     * @return JSONObject result after uploading the image to test for a match
     */
    private static JSONObject uploadToTestImage(String base64String) {
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
            Log.d(Constants.SERVER_UPLOAD_TAG, "Error in http connection while testing image" + e.toString());
        }
        return resultJson;
    }

    /**
     *
     * @param context
     * @param image
     * @param callBack
     */
    public static void uploadNewImage(final Context context, final RecognitoImage image, final CallBackInterface callBack) {
        Log.d(Constants.SERVER_UPLOAD_TAG, "Trying to upload the image");
        if(!networkAvailable(context)) {
            callBack.onFailure(null);
//            callBack.onSuccess(null);

        }
        else {
            Thread uploadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    JSONObject resultJson = uploadNewImage(image);
                    callBack.onSuccess(resultJson);
                }
            });
            uploadThread.start();
        }
    }

    /**
     *
     * @param context
     * @param base64String
     * @param callBack
     */
    public static void uploadToTestImage(final Context context, final String base64String, final CallBackInterface callBack) {
        Log.d(Constants.SERVER_UPLOAD_TAG, "Trying to upload to test the image");
        networkAvailable(context);
        if(!networkAvailable(context)) {
            callBack.onFailure(null);
//            callBack.onSuccess(null);
        }
        else {
            Thread uploadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    JSONObject resultJson = uploadToTestImage(base64String);
                    callBack.onSuccess(resultJson);
                }
            });
            uploadThread.start();
        }
    }

    /**
     *
     * @param context
     * @return if the network is available ot not
     */
    private static boolean networkAvailable(Context context) {
        boolean isPhoneOnline = isOnline(context);
        Log.d(Constants.SERVER_UPLOAD_TAG, "Status of phone " + isPhoneOnline);
//        boolean isServerOnline = isServerAvailable();
//        boolean isServerOnline = isServerReachable(context);
//        Log.d(Constants.SERVER_UPLOAD_TAG, "Status of server " + isServerOnline);
//        return isPhoneOnline && isServerOnline;
        return isPhoneOnline;
    }

    /**
     *
     * @param context
     * @return boolean if the phone is connected to the internet or not
     */
    private static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     *
     * @param context
     * @return boolean if the server is reachable or not
     */
    private static boolean isServerReachable(Context context) {
        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        final boolean[] status = {false};
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(Constants.PING_URL);
                final HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            urlConn.connect();
                            if (urlConn.getResponseCode() == 200) {
                                status[0] = true;
                            }
                        }catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }).start();

            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    /**
     *
     * @return boolean if the server is reachable or not
     */

    private static boolean isServerAvailable() {
        boolean exists = false;
        try {
            SocketAddress sockaddr = new InetSocketAddress(Constants.PING_URL, 8000);
            // Create an unbound socket
            Socket sock = new Socket();

            // This method will block no more than timeoutMs.
            // If the timeout occurs, SocketTimeoutException is thrown.
            int timeoutMs = 3000;
            sock.connect(sockaddr, timeoutMs);
            exists = true;
            sock.close();
        } catch (SocketTimeoutException e) {
            exists = false;
            Log.d(Constants.SERVER_UPLOAD_TAG, "The server is not available");
        }
        catch (IOException e) {
            exists = false;
            Log.d(Constants.SERVER_UPLOAD_TAG, "The server is not available");
        }
        finally {
            return exists;
        }
    }
}