package net.danielpancake.shinyinu;

/*
    This class loads Shiba's image from shibe.online

    Author: danielpancake
*/

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

public class ShibaLoader extends ImageLoader {

    private Context context;
    private View view;
    private ImageView imageView;
    private Button button;
    private ProgressBar progressBar;
    private View.OnClickListener listener;

    ShibaLoader(Context context, View view, ImageView imageView, Button button, ProgressBar progressBar, View.OnClickListener listener) {
        // Match super class
        super(context, view, imageView, true);

        // We import these to change UI in AsyncTask
        this.context = context;
        this.view = view;
        this.imageView = imageView;
        this.button = button;
        this.progressBar = progressBar;

        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        // Don't click me!
        // Please, just wait until image's loaded
        button.setEnabled(false);
        button.setText(context.getResources().getText(R.string.app_loading));

        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    protected Shiba doInBackground(String[] JSON) {
        return super.doInBackground(JSON);
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
            CustomSnackbar.make(view, context.getString(R.string.woof), view.getResources().getDrawable(R.drawable.ic_shiba_status), Snackbar.LENGTH_LONG,
                    context.getString(R.string.undo), listener).show();

            MainActivity.prevShiba = MainActivity.shiba;
            MainActivity.shiba = result;
        }
    }
}