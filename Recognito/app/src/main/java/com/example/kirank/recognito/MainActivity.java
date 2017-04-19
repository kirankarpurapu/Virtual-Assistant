package com.example.kirank.recognito;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button snackbarButton, cameraButton, verifyButton;
    private Uri selectedImage;
    private Uri outputFileUri;
    private CoordinatorLayout coordinatorLayout;
    private String picturePath;
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
                clickPic();
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
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "upload pic and get info", Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void clickPic() {
        // Check Camera
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // Open default camera
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // start the image capture Intent
//            startActivityForResult(intent, 100);


            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(Environment
                    .getExternalStorageDirectory(),
                    "test.jpg");

            outputFileUri = Uri.fromFile(file);
            Log.d("TAG", "outputFileUri intent"
                    + outputFileUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    outputFileUri);
            startActivityForResult(intent, 0);

        } else {
            final Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, " camera not supported", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    // works on tablet

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "kiran1");
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Log.d(TAG, "kiran2");
            Log.d(TAG, outputFileUri.getPath() + " ***");

////            selectedImage = data.getData();
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getContentResolver().query(outputFileUri,
//                    filePathColumn, null, null, null);
//            assert cursor != null;
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            picturePath = cursor.getString(columnIndex);
//            Log.d(TAG, "picture path+ " + picturePath);
//            cursor.close();
            Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
            userInfoIntent.putExtra("PICTURE_PATH", outputFileUri.getPath());
            startActivity(userInfoIntent);
        }
    }

    // testing on mobile phone
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 100 && resultCode == RESULT_OK) {
//
//            selectedImage = data.getData();
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            assert cursor != null;
//            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            picturePath = cursor.getString(columnIndex);
//            Log.d(TAG, "picture path+ " + picturePath);
//            cursor.close();
//            Intent userInfoIntent = new Intent(MainActivity.this, UserInfoActivity.class);
//            userInfoIntent.putExtra("PICTURE_PATH", picturePath);
//            startActivity(userInfoIntent);
//        }
//    }
}
