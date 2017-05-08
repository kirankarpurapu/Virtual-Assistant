package com.example.kirank.recognito;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
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
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int NEW_PHOTO_REQUEST = 1;
    private static final int RETRIEVE_PHOTO_INFO_REQUEST = 2;
    private static final int INSERT_CONTACT_REQUEST = 3;
    private Button snackbarButton, cameraButton, verifyButton, newContactButton;
    private Uri outputFileUri;
    private String picturePath, byteString;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    private final String TAG = "RECOGNITO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        snackbarButton = (Button) findViewById(R.id.snackbar);
        newContactButton = (Button) findViewById(R.id.newContact);
        cameraButton = (Button) findViewById(R.id.camera);
        verifyButton = (Button) findViewById(R.id.verify);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPic(NEW_PHOTO_REQUEST);
            }
        });
        newContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                newContactIntent(bitmap, "");
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
                Toast.makeText(getApplicationContext(), "trying to retrieve the info from the database", Toast.LENGTH_SHORT).show();
                openContactCard(18);
//                uploadPicAndGetInfo();
            }
        });
    }

    private void newContactIntent(Bitmap bitmap, String picturePath) {
        ArrayList<ContentValues> data = new ArrayList<ContentValues>();
        byte[] byteArray = bitMapToByteArray(bitmap);
        ContentValues row = new ContentValues();
        row.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
        row.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray);
        data.add(row);
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);

        Log.d(Constants.MAIN_ACTIVITY_TAG, "trying to open the create contact intent");
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data);
        if (Integer.valueOf(Build.VERSION.SDK) > 14)
            intent.putExtra("finishActivityOnSaveCompleted", true);
        startActivityForResult(intent, INSERT_CONTACT_REQUEST);
    }

    private byte[] bitMapToByteArray(Bitmap bitmap) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        byte[] ba = bao.toByteArray();
        return ba;

    }

    public void uploadPicAndGetInfo() {
        clickPic(RETRIEVE_PHOTO_INFO_REQUEST);
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
            Log.d("MAIN_ACTIVITY_TAG", "outputFileUri intent"
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INSERT_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "after contact intent success");
            getNewContactInfo(data);
        }
        if (requestCode == INSERT_CONTACT_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "after contact intent failure");
            Toast.makeText(getApplicationContext(), "failure in adding contact", Toast.LENGTH_LONG).show();
        }

        if (requestCode == NEW_PHOTO_REQUEST && resultCode == RESULT_OK) {

            Toast.makeText(getApplicationContext(), "success in taking a photo", Toast.LENGTH_LONG).show();
//            Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
//            userInfoIntent.putExtra("PICTURE_PATH", outputFileUri.getPath());
//            startActivity(userInfoIntent);
            picturePath = outputFileUri.getPath();
            Log.d(TAG, " path of image: " + picturePath);
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            newContactIntent(bm, picturePath);
//            uploadImage(picturePath);

        }
        if (requestCode == NEW_PHOTO_REQUEST && resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "failure in taking a photo", Toast.LENGTH_LONG).show();
        }

        if (requestCode == RETRIEVE_PHOTO_INFO_REQUEST && resultCode == RESULT_OK) {
            getTestImageInfoFromServer();
        }
        if (requestCode == RETRIEVE_PHOTO_INFO_REQUEST && resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "failure in retrieving the information of the photo", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadImage(String picturePath, int contactId) {
//        picturePath = outputFileUri.getPath();
//        Log.d(TAG, " path of image: " + picturePath);
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
//        newContactIntent(bm, picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        File imageFile = new File(picturePath);
        byte[] ba = bao.toByteArray();
        byteString = Base64.encodeToString(ba, Base64.DEFAULT);
        startProgressDialog();
        RecognitoImage image = new RecognitoImage(byteString, contactId, imageFile, "KKKKKIIII");
        UploadToServer.uploadNewImage(MainActivity.this, image, new CallBackInterface() {
            @Override
            public void callback(String networkCallResponse) {
                Toast.makeText(MainActivity.this, "server response " + networkCallResponse, Toast.LENGTH_LONG).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopProgressDialog();
                    }
                });
            }
        });
    }

    private void startProgressDialog() {
        progressDialog = new ProgressDialog(MainActivity.this);
        Log.d(Constants.MAIN_ACTIVITY_TAG, "Upload started");
        progressDialog.setMessage("Wait, image uploading!");
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    private void stopProgressDialog() {
        Log.d(Constants.MAIN_ACTIVITY_TAG, "Upload finished");
        progressDialog.hide();
        progressDialog.dismiss();
        progressDialog = null;
    }


    private void getTestImageInfoFromServer() {
        picturePath = outputFileUri.getPath();
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        Log.d("Original   dimensions", bm.getWidth() + " " + bm.getHeight());
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        Log.d("Compressed dimensions", bm.getWidth() + " " + bm.getHeight());
        byte[] ba = bao.toByteArray();
        byteString = Base64.encodeToString(ba, Base64.DEFAULT);
        final Image image = new Image(byteString, null, null, null);

        //upload the picture to test against other images from the database
        new UploadToServerToTest().execute(image);
    }

    private void getNewContactInfo(Intent data) {
        Log.d(TAG, "trying to retrieve the contact of the newly created contact");
        Toast.makeText(getApplicationContext(), "success in adding a new contact", Toast.LENGTH_LONG).show();
        Uri contactData = data.getData();
        Cursor cursor = managedQuery(contactData, null, null, null, null);
        if (cursor.moveToFirst()) {
            long newId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            Log.d( Constants.MAIN_ACTIVITY_TAG, "New contact Added ID of newly added contact is : " + newId + " Name is : " + name);
            Log.d(Constants.MAIN_ACTIVITY_TAG, "New contact Added : Addedd new contact, Need to refress item list : DATA = " + data.toString());
            uploadImage(picturePath, (int)newId);

        }

    }

    private void openContactCard(int contactID) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
        intent.setData(uri);
        startActivity(intent);
    }

    private class UploadToServerToTest extends AsyncTask<Image, Void, String> {

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
