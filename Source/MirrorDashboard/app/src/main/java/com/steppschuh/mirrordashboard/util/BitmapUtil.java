package com.steppschuh.mirrordashboard.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class BitmapUtil {

    public static Bitmap createBitmap(byte[] data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public static byte[] createByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    public static Bitmap scale(Bitmap original, int longestEdge) {
        int outWidth;
        int outHeight;
        int inWidth = original.getWidth();
        int inHeight = original.getHeight();
        if (inWidth > inHeight) {
            outWidth = longestEdge;
            outHeight = (inHeight * longestEdge) / inWidth;
        } else {
            outHeight = longestEdge;
            outWidth = (inWidth * longestEdge) / inHeight;
        }
        outWidth += outWidth % 2;
        outHeight += outHeight % 2;
        return Bitmap.createScaledBitmap(original, outWidth, outHeight, false);
    }

    public static Bitmap rotate(Bitmap original, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

    public static Bitmap greyscale(Bitmap original) {
        Bitmap newBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(f);
        canvas.drawBitmap(original, 0, 0, paint);
        return newBitmap;
    }

    public static boolean containsFace(Bitmap bitmap) {
        return !detectFaces(bitmap).isEmpty();
    }

    public static List<FaceDetector.Face> detectFaces(Bitmap bitmap) {
        return detectFaces(bitmap, 1);
    }

    public static List<FaceDetector.Face> detectFaces(Bitmap bitmap, int maximumNumberOfFaces) {
        FaceDetector.Face[] faces = new FaceDetector.Face[maximumNumberOfFaces];
        FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), maximumNumberOfFaces);
        faceDetector.findFaces(bitmap, faces);
        List<FaceDetector.Face> detectedFaces = new ArrayList<>();
        for (FaceDetector.Face face : faces) {
            if (face != null) {
                detectedFaces.add(face);
            }
        }
        return detectedFaces;
    }

}
