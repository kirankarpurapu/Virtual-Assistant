package com.example.kirank.recognito;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button snackbarButton, cameraButton, uploadButton;
    private  Snackbar snackbar;
    private Uri fileUri, selectedImage;
    private Bitmap photo;
    private CoordinatorLayout coordinatorLayout;
    private String picturePath, byteString = null;
    private final String TAG = "RECOGNITO", URL = "http://192.168.1.164:5000/image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snackbarButton = (Button)findViewById(R.id.snackbar);
        cameraButton = (Button) findViewById(R.id.camera);
        uploadButton = (Button) findViewById(R.id.upload);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPic();
            }
        });

        snackbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar = Snackbar
                        .make(coordinatorLayout, "Message is deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar = Snackbar.make(coordinatorLayout, "Message is restored!", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        });
                snackbar.show();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture();
            }
        });
    }
    public void clickPic() {
            // Check Camera
            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)) {
                // Open default camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                // start the image capture Intent
                startActivityForResult(intent, 100);

            } else {
                Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
            }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {

            selectedImage = data.getData();
            photo = (Bitmap) data.getExtras().get("data");

            // Cursor to get image uri to display

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

//            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Toast.makeText(getApplicationContext(), "Kiran Karpurapu" + picturePath, Toast.LENGTH_LONG).show();
        }
    }
    public void uploadPicture() {
        if(picturePath == null) {
            Toast.makeText(getApplicationContext(), "no picture to post", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, " path : " + picturePath);
        Toast.makeText(getApplicationContext(), "Path: " + picturePath, Toast.LENGTH_LONG).show();
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        byteString = Base64.encodeToString(ba, Base64.DEFAULT);
        Log.d("TAG", "-----base 64" + byteString);
        new UploadToServer().execute();
    }

    public class UploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(MainActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait image uploading!");
            pd.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("base64", byteString));
            nameValuePairs.add(new BasicNameValuePair("ImageName", System.currentTimeMillis() + ".jpg"));
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
            return "Success";
        }
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
        }
    }
}
