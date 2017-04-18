package com.example.kirank.recognito;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by kirank on 4/17/17.
 */

public class UserInfoActivity extends Activity {
    private EditText name, phoneNumber, email, additionalInfo;
    private Button cancel, submit;
    private String picturePath, byteString = null;
    private final String TAG = "RECOGNITO", URL = "http://192.168.1.164:5000/newImage";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info);
        picturePath = getIntent().getStringExtra("PICTURE_PATH");
        name = (EditText) findViewById(R.id.NameEditText);
        phoneNumber = (EditText) findViewById(R.id.PhoneEditText);
        email = (EditText) findViewById(R.id.EmailEditText);
        additionalInfo = (EditText) findViewById(R.id.additionaldetailsEditText);
        cancel = (Button) findViewById(R.id.cancelbutton);
        submit = (Button) findViewById(R.id.submitbutton);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture(picturePath);
            }
        });
    }

    public void uploadPicture(String picturePath) {
        if (picturePath == null) {
            Toast.makeText(getApplicationContext(), "no picture to post", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, " path : " + picturePath);
//        Toast.makeText(getApplicationContext(), "Path: " + picturePath, Toast.LENGTH_LONG).show();
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        byteString = Base64.encodeToString(ba, Base64.DEFAULT);
        Log.d("TAG", "-----base 64" + byteString);
        JSONObject info = getImageInformation();
        Image image = new Image(byteString, "Kiran", info);
        new UploadToServer().execute(image);
    }

    public class UploadToServer extends AsyncTask<Image, Void, JSONObject> {

        private ProgressDialog pd = new ProgressDialog(UserInfoActivity.this);

        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected JSONObject doInBackground(Image... image) {
            Image thisImage = image[0];

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("base64", thisImage.getData()));
            nameValuePairs.add(new BasicNameValuePair("ImageName", System.currentTimeMillis() + thisImage.getName() + ".jpg"));
            nameValuePairs.add(new BasicNameValuePair("ImageInfo", thisImage.getInfo().toString()));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                String st = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "In the try Loop" + st);
            } catch (Exception e) {
                Log.d(TAG, "Error in http connection " + e.toString());
            }
            return null;
        }

        protected void onPostExecute(JSONObject jsonResultObject) {
            super.onPostExecute(jsonResultObject);
            pd.hide();
            pd.dismiss();
            finish();
        }
    }

    public JSONObject getImageInformation() {
        String nameString = name.getText().toString();
        String phoneNumberString = phoneNumber.getText().toString();
        String emailString = email.getText().toString();
        String additionalInfoString = additionalInfo.getText().toString();
        JSONObject obj = null;

        nameString = "kiran";
        phoneNumberString = "1234";
        emailString = "kk@kk.com";
        additionalInfoString = "justlame";
        try {
            obj = new JSONObject();
            obj.put("name", nameString);
            obj.put("phonenumber", phoneNumberString);
            obj.put("email", emailString);
            obj.put("additionalinfo", additionalInfoString);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return obj;
        }
    }
}
