package net.danielpancake.shinyinu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class PhotoViewerLoader extends AsyncTask<String, Void, Bitmap> {

    private Context context;
    private PhotoView photoViewer;

    PhotoViewerLoader(PhotoView photoViewer) {
        this.photoViewer = photoViewer;
        this.context = photoViewer.getContext();
    }

    @Override
    protected Bitmap doInBackground(String[] urls) {

        if ((new File(urls[0]).exists())) {
            return BitmapFactory.decodeFile(urls[0]);
        } else {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_shiba_placeholder);
        }

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (bitmap != null) {
            photoViewer.setImageBitmap(bitmap);
        }
    }
}
