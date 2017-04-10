package com.steppschuh.mirrordashboard.firebase;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
        StorageReference imagesRef = storageRef.child(PATH_IMAGES);
        StorageReference imageRef = imagesRef.child(FILE_LATEST_IMAGE);

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
