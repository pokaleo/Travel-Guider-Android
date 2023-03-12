package uk.ac.shef.dcs.travelguider.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageHelper {

    public static Bitmap getBitmapByFile(File file) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return bitmap;
    }

    public  static Bitmap changeBitmapSize(Bitmap bitmap) {


        int width = bitmap.getWidth();

        int height = bitmap.getHeight();
        int newWidth=160;

        int newHeight=160;
        float scaleWidth=((float)newWidth)/width;

        float scaleHeight=((float)newHeight)/height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth,scaleHeight);

        bitmap=Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);

        bitmap.getWidth();

        bitmap.getHeight();

        return bitmap;

    }

    public  static Bitmap changeBitmapSizeForGallery(Bitmap bitmap) {


        int width = bitmap.getWidth();

        int height = bitmap.getHeight();
        int newWidth=320;

        int newHeight=320;
        float scaleWidth=((float)newWidth)/width;

        float scaleHeight=((float)newHeight)/height;

        Matrix matrix = new Matrix();

        matrix.postScale(scaleWidth,scaleHeight);

        bitmap=Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);

        bitmap.getWidth();

        bitmap.getHeight();

        return bitmap;

    }

}
