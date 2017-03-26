package com.steppschuh.mirrordashboard.content.camera;

import android.graphics.Bitmap;

import com.steppschuh.mirrordashboard.content.Content;
import com.steppschuh.mirrordashboard.util.BitmapUtil;

public class Photo extends Content {

    private byte[] data;
    private Bitmap bitmap;

    public Photo(byte[] data) {
        super(Content.TYPE_PHOTO);
        this.data = data;
    }

    public boolean containsFace() {
        Bitmap scaledBitmap = BitmapUtil.scale(getBitmap(), 500);
        return BitmapUtil.containsFace(scaledBitmap);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Bitmap getBitmap() {
        if (bitmap == null) {
            bitmap = BitmapUtil.createBitmap(data);
        }
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

}
