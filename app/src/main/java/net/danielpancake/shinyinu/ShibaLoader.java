package net.danielpancake.shinyinu;

/*

    This class loads Shiba's image from shibe.online

*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ShibaLoader extends AsyncTask<Void, Void, Shiba> {

    private Context context;
    private View view;
    private ImageView imageView;
    private Button button;
    private ProgressBar progressBar;

    ShibaLoader(Context context, View view, ImageView imageView, Button button, ProgressBar progressBar) {

        // We import these to change UI in AsyncTask
        this.context = context;
        this.view = view;
        this.imageView = imageView;
        this.button = button;
        this.progressBar = progressBar;

        button.setEnabled(false);                                               //Disable button click
        button.setText(context.getResources().getText(R.string.app_loading));   //Set text to "loading"

        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    protected Shiba doInBackground(Void[] nothing) {

        String JSON = "";
        Bitmap bitmap = null;
        String shibacode = null;

        try {
            // Use https to prevent errors on android API 25+
            BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader (
                    new URL("https://shibe.online/api/shibes?count=1&urls=false").openStream() //Open https stream and get image
                )
            );

            // Get JSON from the link above
            String inputLine = "";

            while (inputLine != null) {
                JSON += inputLine;
                inputLine = bufferedReader.readLine();
            }

            // If app gets here (no error occurred), we'll get the image code
            shibacode = new JSONArray(JSON).getString(0);

            bitmap = BitmapFactory.decodeStream(
                new URL("https://cdn.shibe.online/shibes/" + shibacode + ".jpg").openStream() //Load image from link in json and store it
            );

        } catch (IOException | JSONException e) {
            e.printStackTrace(); //If error occured then print the stack trace
        }

        return new Shiba(shibacode, bitmap); //Return class with filled in code and image
    }

    @Override
    protected void onPostExecute(Shiba result) {

        button.setText(context.getResources().getText(R.string.button_shiny)); //Set button text back to original

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        //REMOVE DELAY ITS ANNOYING
        // Handler delay = new Handler();

        // delay.postDelayed(new Runnable() {
        //     public void run() {
        //         button.setEnabled(true);
        //     }
        // }, 2100);

        // If all is right, set the image
        if (result.bitmap != null) {
            imageView.setImageBitmap(result.bitmap); //Set image viewport to image stored in bitmap
            CustomSnackbar.make(view, "Woof!", view.getResources().getDrawable(R.drawable.ic_shiba_status), Snackbar.LENGTH_LONG).show(); //Indicate that image is loaded
        }
    }
}



