package com.example.kirank.recognito;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button snackbarButton, cameraButton, verifyButton;
    private Uri outputFileUri;
    private String picturePath, byteString;
    private CoordinatorLayout coordinatorLayout;
    private final String TAG = "RECOGNITO";
    private final String BASE_URL = "http://98.116.40.213:5000/";
    private final String TEST_IMAGE_URL = BASE_URL + "testImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snackbarButton = (Button) findViewById(R.id.snackbar);
        cameraButton = (Button) findViewById(R.id.camera);
        verifyButton = (Button) findViewById(R.id.verify);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPic(0);
            }
        });

        snackbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "Message is deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        });
                snackbar.show();
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicAndGetInfo();
            }
        });
    }

    public void uploadPicAndGetInfo() {
//        final Snackbar snackbar = Snackbar
//                .make(coordinatorLayout, "upload pic and get info", Snackbar.LENGTH_LONG);
//        snackbar.show();
        clickPic(1);
    }


    public void clickPic(int requestCode) {
        // Check Camera
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // Open default camera
            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(Environment
                    .getExternalStorageDirectory(),
                    "testKiran1234.jpg");
            outputFileUri = Uri.fromFile(file);
            Log.d("TAG", "outputFileUri intent"
                    + outputFileUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    outputFileUri);
            startActivityForResult(intent, requestCode);

        } else {
            final Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, " camera not supported", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
            userInfoIntent.putExtra("PICTURE_PATH", outputFileUri.getPath());
            startActivity(userInfoIntent);
        }
        if(requestCode == 1 && resultCode == RESULT_OK) {
            picturePath = outputFileUri.getPath();
            //upload the picture to test against other images
            Log.d(TAG, "test picture path " + picturePath);
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
            byte[] ba = bao.toByteArray();
            byteString = Base64.encodeToString(ba, Base64.DEFAULT);
            Image image = new Image(byteString, null, null);
            new UploadToServer().execute(image);
        }
    }
    public class UploadToServer extends AsyncTask<Image, Void, String> {

        private ProgressDialog pd = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            Log.d(TAG, "Upload started");
            super.onPreExecute();
            pd.setMessage("Wait, Testing the image!");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(Image... image) {
            Image thisImage = image[0];
            String st = "";
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("base64", thisImage.getData()));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(TEST_IMAGE_URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                st = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "In the try Loop" + st);
            } catch (Exception e) {
                Log.d(TAG, "Error in http connection " + e.toString());
            }
            finally {
                return st;
            }

        }

        protected void onPostExecute(String jsonResultObject) {
            super.onPostExecute(jsonResultObject);
            pd.hide();
            pd.dismiss();
            if(jsonResultObject != null)
                Toast.makeText(getApplicationContext(),"result:" + jsonResultObject.toString(), Toast.LENGTH_LONG).show();
//            finish();
        }
    }

}
