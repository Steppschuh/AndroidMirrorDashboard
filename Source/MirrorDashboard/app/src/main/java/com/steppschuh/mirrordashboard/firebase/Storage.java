package com.steppschuh.mirrordashboard.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Stephan on 4/9/2017.
 */

public final class Storage {

    private static final String PATH_IMAGES = "images";
    private static final String FILE_LATEST_IMAGE = "latest_image.jpg";

    private static Storage instance;
    private FirebaseStorage firebaseStorage;

    private Storage() {

    }

    public static Storage getInstance() {
        if (instance == null) {
            instance = new Storage();
            instance.firebaseStorage = FirebaseStorage.getInstance();
        }
        return instance;
    }

    public static UploadTask uploadImage(byte[] data) {
        StorageReference storageRef = getInstance().firebaseStorage.getReference();
        Date now = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

        String filePath = new StringBuilder(PATH_IMAGES).append("/")
                .append(calendar.get(Calendar.YEAR)).append("/")
                .append(calendar.get(Calendar.MONTH) + 1).append("/")
                .append(calendar.get(Calendar.DAY_OF_MONTH)).append("/")
                .append("image_").append(dateFormat.format(now)).append(".jpg")
                .toString();
        StorageReference imageRef = storageRef.child(filePath);

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.w(Storage.class.getSimpleName(), "Image upload failed: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                Log.d(Storage.class.getSimpleName(), "Uploaded image to: " + downloadUrl);
            }
        });
        return uploadTask;
    }

}
