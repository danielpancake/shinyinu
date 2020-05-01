package net.danielpancake.shinyinu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class ImageAsyncLoader extends AsyncTask<String, Void, Bitmap> {

    ImageView imageView;
    String imageCode;
    byte[] imagePreview;

    ImageAsyncLoader(ImageView imageView, String imageCode, byte[] imagePreview) {
        this.imageView = imageView;
        this.imageCode = imageCode;

        this.imagePreview = imagePreview;
    }

    @Override
    protected Bitmap doInBackground(String[] urls) {
        return decodeURI(urls[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            MainActivity.getMemoryCache().addBitmapToMemoryCache(imageCode, bitmap, false);
        } else {
            // TODO: Blur image if it's just a preview

            Bitmap imagePreviewBitmap = BitmapFactory.decodeByteArray(imagePreview, 0, imagePreview.length);
            imageView.setImageBitmap(imagePreviewBitmap);

            MainActivity.getMemoryCache().addBitmapToMemoryCache(imageCode, imagePreviewBitmap, true);
        }

        super.onPostExecute(bitmap);
    }

    public Bitmap decodeURI(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        // Just load some information about the image
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Scale down the image
        double ratio = options.outHeight >= options.outWidth ? options.outHeight / 240 : options.outWidth / 240;
        options.inSampleSize = (int) Math.pow(2d, Math.floor(Math.log(ratio) / Math.log(2d)));

        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[512];

        return BitmapFactory.decodeFile(filePath, options);
    }
}
