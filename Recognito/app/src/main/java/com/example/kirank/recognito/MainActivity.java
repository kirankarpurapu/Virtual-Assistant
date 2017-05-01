package com.example.kirank.recognito;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.internal.http.multipart.MultipartEntity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Button snackbarButton, cameraButton, verifyButton;
    private Uri outputFileUri;
    private String picturePath, byteString;
    private CoordinatorLayout coordinatorLayout;
    private final String TAG = "RECOGNITO";

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
        clickPic(1);
    }

    /**
     * @param requestCode
     */
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
//            outputFileUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getApplicationContext().getPackageName() + ".provider", createImageFile());
            Log.d("TAG", "outputFileUri intent"
                    + outputFileUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    outputFileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, requestCode);
        } else {
            final Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, " camera not supported", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    /*
    Note: request code is 0 when the user is adding a new entry to the system.
    request code is 1 when the user is trying to match a photo against the database.
     */

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
            userInfoIntent.putExtra("PICTURE_PATH", outputFileUri.getPath());
            startActivity(userInfoIntent);
        }
        if (requestCode == 1 && resultCode == RESULT_OK) {
            picturePath = outputFileUri.getPath();
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            Log.d("Original   dimensions", bm.getWidth()+" "+bm.getHeight());
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
            Log.d("Compressed dimensions", bm.getWidth()+" "+bm.getHeight());
            byte[] ba = bao.toByteArray();
            byteString = Base64.encodeToString(ba, Base64.DEFAULT);
            final Image image = new Image(byteString, null, null, null);

            //upload the picture to test against other images from the database
            new UploadToServer().execute(image);
        }
    }

    private class UploadToServer extends AsyncTask<Image, Void, String> {

        public static final String WAIT_TESTING_THE_IMAGE = "Wait, Testing the image!";
        public static final String UPLOAD_STARTED = "Upload started";
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            Log.d(TAG, UPLOAD_STARTED);
            super.onPreExecute();
            progressDialog.setMessage(WAIT_TESTING_THE_IMAGE);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
        //TODO: fix the return type of the @doInBackground method, it should return a JSON Object instead of a String
        @Override
        protected String doInBackground(Image... image) {
            Image thisImage = image[0];
            String resultString = null;
//            MultipartEntity multipartEntity = new MultipartEntity();
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("base64", thisImage.getData()));
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(Constants.TEST_IMAGE_URL);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                resultString = EntityUtils.toString(response.getEntity());
                Log.d(TAG, "In the try Loop" + resultString);
            } catch (Exception e) {
                Log.d(TAG, "Error in http connection " + e.toString());
            }
            return resultString;
        }

        protected void onPostExecute(String jsonResultObject) {
            super.onPostExecute(jsonResultObject);
            progressDialog.hide();
            progressDialog.dismiss();
            if (jsonResultObject != null) {
                Toast.makeText(getApplicationContext(), "result:" + jsonResultObject.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
