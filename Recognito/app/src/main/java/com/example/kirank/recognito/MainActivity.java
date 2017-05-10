package com.example.kirank.recognito;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int NEW_PHOTO_REQUEST = 1;
    private static final int RETRIEVE_PHOTO_INFO_REQUEST = 2;
    private static final int INSERT_CONTACT_REQUEST = 3;
    private Button cameraButton, verifyButton;
    private Uri outputFileUri;
    private String picturePath, byteString;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    private final String TAG = "RECOGNITO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraButton = (Button) findViewById(R.id.camera);
        verifyButton = (Button) findViewById(R.id.verify);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPic(NEW_PHOTO_REQUEST);
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicAndGetInfo();
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
                    "Recognito" + System.currentTimeMillis() + ".jpg");
            outputFileUri = Uri.fromFile(file);
            Log.d("MAIN_ACTIVITY_TAG", "outputFileUri intent"
                    + outputFileUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    outputFileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, requestCode);
        } else {
            showSnackBar("camera not supported");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INSERT_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "after contact intent success");
            getNewContactInfo(data);
        }
        if (requestCode == INSERT_CONTACT_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "after contact intent failure");
            showSnackBar("failure in creating a new contact");
        }

        if (requestCode == NEW_PHOTO_REQUEST && resultCode == RESULT_OK) {
            picturePath = outputFileUri.getPath();
            Log.d(TAG, " path of image: " + picturePath);
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            newContactIntent(bm, picturePath);

        }
        if (requestCode == NEW_PHOTO_REQUEST && resultCode == RESULT_CANCELED) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "failure in taking a photo", Snackbar.LENGTH_LONG).
                            setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    clickPic(NEW_PHOTO_REQUEST);
                                }
                            });
            snackbar.show();
        }

        if (requestCode == RETRIEVE_PHOTO_INFO_REQUEST && resultCode == RESULT_OK) {
            getTestImageInfoFromServer();
        }
        if (requestCode == RETRIEVE_PHOTO_INFO_REQUEST && resultCode == RESULT_CANCELED) {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, "failure in taking a photo", Snackbar.LENGTH_LONG).
                            setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    clickPic(RETRIEVE_PHOTO_INFO_REQUEST);
                                }
                            });
            snackbar.show();
        }
    }

    private void uploadImage(String picturePath, int contactId) {
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        File imageFile = new File(picturePath);
        byte[] ba = bao.toByteArray();
        byteString = Base64.encodeToString(ba, Base64.DEFAULT);
        startProgressDialog("Hold on Tight!, uploading image");
        RecognitoImage image = new RecognitoImage(byteString, contactId, imageFile, "Person_NAME");
        UploadToServer.uploadNewImage(MainActivity.this, image, new CallBackInterface() {
            @Override
            public void callback(final JSONObject networkCallResponse) {
                if(networkCallResponse != null) {
                    Log.d(TAG, "upload new image result " + networkCallResponse.toString());
                    try {
                        int status = networkCallResponse.getInt("result_status");
                        if(status != 1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        showSnackBar("Server says: " + networkCallResponse.getString("message"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showSnackBar("Server failed to respond, please retry");
                            }
                        });

                    }
                    showSnackBar("Successfully uploaded the image");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopProgressDialog();
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSnackBar("Server failed to respond, please retry");
                        }
                    });
                }
            }
        });
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, " " + message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
    private void startProgressDialog(String message) {
        progressDialog = new ProgressDialog(MainActivity.this);
        Log.d(Constants.MAIN_ACTIVITY_TAG, "Upload started");
        progressDialog.setMessage(message);
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
        Log.d(TAG, "Original   dimensions" + bm.getWidth() + " " + bm.getHeight());
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bao);
        Log.d(TAG, "Compressed dimensions" + bm.getWidth() + " " + bm.getHeight());
        byte[] ba = bao.toByteArray();
        byteString = Base64.encodeToString(ba, Base64.DEFAULT);

        //upload the picture to test against other images from the database
        startProgressDialog("Hold Tight!, trying to find a match");
        UploadToServer.uploadToTestImage(MainActivity.this, byteString, new CallBackInterface() {

            @Override
            public void callback(JSONObject networkCallResponse) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopProgressDialog();
                    }
                });
                if (networkCallResponse != null) {
                    Log.d(TAG, "testing image with database" + networkCallResponse.toString());
                    try {
                        int contactId = networkCallResponse.getInt("contact_id");
                        if (contactId != -1) {
                            openContactCard(contactId);
                        } else {
                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "No matching user found", Snackbar.LENGTH_LONG)
                                    .setAction("Retry", new View.OnClickListener() {

                                        @Override
                                        public void onClick(View view) {
                                            clickPic(RETRIEVE_PHOTO_INFO_REQUEST);
                                        }
                                    });
                            snackbar.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "No matching user found", Snackbar.LENGTH_LONG).
                                    setAction("Retry", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            clickPic(RETRIEVE_PHOTO_INFO_REQUEST);
                                        }
                                    });
                    snackbar.show();
                    Log.d(TAG, "No result while testing image");
                }
            }
        });
    }

    private void getNewContactInfo(Intent data) {
        Log.d(TAG, "trying to retrieve the contact of the newly created contact");
        Uri contactData = data.getData();
        Cursor cursor = managedQuery(contactData, null, null, null, null);
        if (cursor.moveToFirst()) {
            long newId = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
            Log.d(Constants.MAIN_ACTIVITY_TAG, "New contact Added ID of newly added contact is : " + newId + " Name is : " + name);
            Log.d(Constants.MAIN_ACTIVITY_TAG, "New contact Added : Addedd new contact, Need to refress item list : DATA = " + data.toString());
            uploadImage(picturePath, (int) newId);
        }
    }

    private void openContactCard(int contactID) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
        intent.setData(uri);
        startActivity(intent);
    }
}
