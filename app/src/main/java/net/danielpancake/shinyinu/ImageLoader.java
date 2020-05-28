package net.danielpancake.shinyinu;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class ImageLoader extends AsyncTask<String, Void, Shiba> {

    private Context context;
    private View view;
    private ImageView imageView;
    private boolean randomOnError;

    Drawable ic_ok;
    Drawable ic_bad;

    ImageLoader(Context context, View view, ImageView imageView, boolean randomOnError) {
        this.context = context;
        this.view = view;
        this.imageView = imageView;
        this.randomOnError = randomOnError;

        this.ic_ok = view.getResources().getDrawable(R.drawable.ic_shiba_status);
        this.ic_bad = view.getResources().getDrawable(R.drawable.ic_shiba_status_bad);
    }

    private static String getFileNameWithoutExtension(File file) {
        String fileName = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }

        return fileName;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        ((FrameLayout) imageView.getParent()).setForeground(null);
    }

    @Override
    protected Shiba doInBackground(String[] JSON) {
        Bitmap bitmap = null;
        String code = null;

        // Collect information about Internet access
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (JSON[0] != null && networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            try {
                code = new JSONArray(JSON[0]).getString(0);

                if (code.equals("Original")) {
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.original);
                } else {
                    bitmap = BitmapFactory.decodeStream(
                            new URL("https://cdn.shibe.online/shibes/" + code + ".jpg").openStream());
                }
            } catch (IOException | JSONException e) {
                CustomSnackbar.make(view, context.getString(R.string.errror), ic_bad, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            if (randomOnError) {
                File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu");
                File[] imagesList = directory.listFiles();

                if (imagesList != null) {
                    Random random = new Random();
                    File image = imagesList[random.nextInt(imagesList.length)];

                    bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                    code = getFileNameWithoutExtension(image);
                } else {
                    CustomSnackbar.make(view, context.getString(R.string.no_internet), ic_ok, Snackbar.LENGTH_LONG).show();
                }
            } else {
                CustomSnackbar.make(view, context.getString(R.string.no_internet), ic_ok, Snackbar.LENGTH_LONG).show();
            }
        }

        return new Shiba(code, bitmap);
    }

    @Override
    protected void onPostExecute(Shiba result) {
        super.onPostExecute(result);

        if (result.bitmap != null) {
            ((FrameLayout) imageView.getParent()).setForeground(null);
            bitmapSaveToFile(result.code, result.bitmap);

            ImageAsyncLoader imageAsyncLoader = new ImageAsyncLoader(imageView, result.code);
            imageAsyncLoader.execute(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                    "/ShinyInu/" + result.code + ".jpg");
        } else {
            ((FrameLayout) imageView.getParent()).setForeground(context.getDrawable(R.drawable.ic_load));
        }
    }

    private void bitmapSaveToFile(String name, Bitmap bitmap) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu");
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        File saving_image = new File(directory, name + ".jpg");

        if (!saving_image.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(saving_image);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                MainActivity.memoryCache.removeBitmapFromMemoryCache(name);

                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saving_image)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
