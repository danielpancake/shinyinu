package net.danielpancake.shinyinu;

/*
    This class loads Shiba's image from shibe.online

    Author: danielpancake
*/

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.ExecutionException;

public class ShibaLoader extends ImageLoader {

    private Context context;
    private View view;
    private ImageView imageView;
    private Button button;
    private ProgressBar progressBar;

    private String[] JSON = {null};

    ShibaLoader(Context context, View view, ImageView imageView, Button button, ProgressBar progressBar) {
        // Match super class
        super(context, view, imageView, true);

        // We import these to change UI in AsyncTask
        this.context = context;
        this.view = view;
        this.imageView = imageView;
        this.button = button;
        this.progressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        // Don't click me!
        // Please, just wait until image's loaded
        button.setEnabled(false);
        button.setText(context.getResources().getText(R.string.app_loading));

        progressBar.setVisibility(ProgressBar.VISIBLE);

        // Collect information about Internet access
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            JSONGetter jsonGetter = new JSONGetter();
            try {
                JSON[0] = jsonGetter.execute("https://shibe.online/api/shibes?count=1&urls=false").get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected Shiba doInBackground(String[] JSON) {
        return super.doInBackground(this.JSON);
    }

    @Override
    protected void onPostExecute(Shiba result) {
        // Okay, we're through. Now give me a moment to catch my breath
        // And then you'll be able click me again!
        button.setText(context.getResources().getText(R.string.button_shiny));

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            public void run() {
                button.setEnabled(true);
            }
        }, 500);

        // If all is right, set the image
        if (result.bitmap != null) {
            // But firstly adjust screen size
            new ImageAdjuster(context, imageView, result.bitmap);
            CustomSnackbar.make(view, "Woof!", view.getResources().getDrawable(R.drawable.ic_shiba_status), Snackbar.LENGTH_LONG).show();
        }
    }
}