package com.example.kirank.recognito;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

/**
 * Created by kirank on 5/11/17.
 */

public class S3Upload extends AsyncTask<String, String, String> {
    Context applicationContext;
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;

    //connect to s3 securely
    public S3Upload(Context applicationContext) {
        this.applicationContext = applicationContext;
         credentialsProvider = new CognitoCachingCredentialsProvider(
                applicationContext,
                "us-east-1:b64eb96b-f4d1-48b8-adb1-df168b1c3cff", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );
        s3 = new AmazonS3Client(credentialsProvider);
    }
    //Upload images to sr
    public void upload(String bucketName, String fileName) {
        try {
            Log.d(Constants.SERVER_UPLOAD_TAG,"1. new upload s3");
            Log.d(Constants.SERVER_UPLOAD_TAG, "filename " + fileName);

            TransferUtility transferUtility = new TransferUtility(s3, applicationContext);
            TransferObserver observer = transferUtility.upload(
                    bucketName,     /* The bucket to upload to */
                    fileName,    /* The key for the uploaded object */
                    new File(fileName)        /* The file where the data to upload exists */
            );
            Log.d(Constants.SERVER_UPLOAD_TAG, "2. new upload s3");
        }
        catch(Exception e) {
            Log.d(Constants.SERVER_UPLOAD_TAG,e.getMessage());
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        upload(strings[0], strings[1]);
        return null;
    }


}