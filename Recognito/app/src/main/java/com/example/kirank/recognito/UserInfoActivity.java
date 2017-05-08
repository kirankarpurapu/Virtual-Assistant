package com.example.kirank.recognito;

import android.app.Activity;

//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.util.Base64;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.util.EntityUtils;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import com.android.internal.http.multipart.FilePart;
//import com.android.internal.http.multipart.MultipartEntity;
//import com.android.internal.http.multipart.Part;
//import com.android.internal.http.multipart.StringPart;
//import com.example.kirank.recognito.Constants;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
// * Created by kirank on 4/17/17.
// */
//
public class UserInfoActivity extends Activity {
}
//    private static final double IMAGE_MAX_SIZE = 70d;
//    private EditText name, phoneNumber, email, additionalInfo;
//    private Button cancel, submit;
//    private String picturePath, byteString = null;
//    private final String MAIN_ACTIVITY_TAG = "RECOGNITO";
//
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.user_info);
//        picturePath = getIntent().getStringExtra("PICTURE_PATH");
//        Log.d(MAIN_ACTIVITY_TAG, "user activity: " + picturePath);
//        name = (EditText) findViewById(R.id.NameEditText);
//        phoneNumber = (EditText) findViewById(R.id.PhoneEditText);
//        email = (EditText) findViewById(R.id.EmailEditText);
//        additionalInfo = (EditText) findViewById(R.id.additionaldetailsEditText);
//        cancel = (Button) findViewById(R.id.cancelbutton);
//        submit = (Button) findViewById(R.id.submitbutton);
//
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
//        submit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                uploadPicture(picturePath);
//            }
//        });
//    }
//
//    /**
//     *
//     * @param picturePath
//     */
//    private void uploadPicture(String picturePath) {
//        if (picturePath == null) {
//            Toast.makeText(getApplicationContext(), "no picture to post", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        Log.d(MAIN_ACTIVITY_TAG, " path of image: " + picturePath);
//        Bitmap bm = BitmapFactory.decodeFile(picturePath);
//        Log.d("Original   dimensions", bm.getWidth()+" "+bm.getHeight());
//        ByteArrayOutputStream bao = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
//        File imageFile = new File(picturePath);
////        bm = decodeFile(imageFile);
//        Log.d("compressed   dimensions", bm.getWidth()+" "+bm.getHeight());
//        byte[] ba = bao.toByteArray();
//        byteString = Base64.encodeToString(ba, Base64.DEFAULT);
//        JSONObject info = getImageInformation();
//        Image image = new Image(byteString, "Kiran", info, imageFile);
//        new UploadToServer().execute(image);
//    }
//
//    private class UploadToServer extends AsyncTask<Image, Void, String> {
//
//        private ProgressDialog pd = new ProgressDialog(UserInfoActivity.this);
//
//        protected void onPreExecute() {
//            Log.d(MAIN_ACTIVITY_TAG, "Upload started");
//            super.onPreExecute();
//            pd.setMessage("Wait, image uploading!");
//            pd.setCancelable(false);
//            pd.show();
//        }
//
//        @Override
//        protected String doInBackground(Image... image) {
//            Image thisImage = image[0];
//            String resultString = null;
//            Log.d("MAIN_ACTIVITY_TAG", "-----base 64" + thisImage.getName());
//
//            try {
//                HttpClient httpclient = new DefaultHttpClient();
//                HttpPost httppost = new HttpPost(Constants.NEW_IMAGE_URL);
//                Part[] parts = {
//                        new FilePart("file", thisImage.getFile()),
//                        new StringPart("base64", thisImage.getData()),
//                        new StringPart("ImageName", System.currentTimeMillis() + thisImage.getName() + ".jpg"),
//                        new StringPart("ImageInfo", thisImage.getInfo().toString())
//                };
//                MultipartEntity multipartEntity = new MultipartEntity(parts, httppost.getParams());
//                httppost.setEntity(multipartEntity);
//
//                HttpResponse response = httpclient.execute(httppost);
//                resultString = EntityUtils.toString(response.getEntity());
//                Log.d(MAIN_ACTIVITY_TAG, "In the try Loop" + resultString);
//            } catch (Exception e) {
//                Log.d(MAIN_ACTIVITY_TAG, "Error in http connection " + e.toString());
//            }
//            return resultString;
//        }
//
//        protected void onPostExecute(String jsonResultObject) {
//            super.onPostExecute(jsonResultObject);
//            pd.hide();
//            pd.dismiss();
//            finish();
//        }
//    }
//
//    /**
//     *
//     * @return JSONObject
//     */
//    public JSONObject getImageInformation() {
//        String nameString = name.getText().toString();
//        String phoneNumberString = phoneNumber.getText().toString();
//        String emailString = email.getText().toString();
//        String additionalInfoString = additionalInfo.getText().toString();
//        JSONObject obj = null;
//
//        //dummy data if the user hasn't filled any info
//        if (2 > nameString.length()) {
//            nameString = "kiran";
//        }
//        if (2 > phoneNumberString.length()) {
//            phoneNumberString = "1234";
//        }
//        if (2 > emailString.length()) {
//            emailString = "kk@kk.com";
//        }
//        if (2 > additionalInfoString.length()) {
//            additionalInfoString = "justlame";
//        }
//
//        try {
//            obj = new JSONObject();
//            obj.put("name", nameString);
//            obj.put("phonenumber", phoneNumberString);
//            obj.put("email", emailString);
//            obj.put("additionalinfo", additionalInfoString);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return obj;
//    }
//}
