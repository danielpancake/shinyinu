package net.danielpancake.shinyinu;

/*

    This class loads Shiba's image from shibe.online

*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ShibaLoader extends AsyncTask<Void, Void, Shiba> {

    private Context context;
    private ImageView imageView;
    private Button button;

    ShibaLoader(Context context, ImageView imageView, Button button) {

        // We import these to change UI in AsyncTask
        this.context = context;
        this.imageView = imageView;
        this.button = button;

        // Don't click me!
        // Please, just wait until image's loaded
        button.setEnabled(false);
        button.setText(context.getResources().getText(R.string.app_loading));
    }

    @Override
    protected Shiba doInBackground(Void[] nothing) {

        String JSON = "";
        Bitmap bitmap = null;
        String shibacode = null;

        try {
            // We use https to prevent errors on android API 25+ (not sure)
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    new URL("https://shibe.online/api/shibes?count=1&urls=false").openStream()));

            // Get JSON from the link above
            String inputLine = "";

            while (inputLine != null) {
                JSON += inputLine;
                inputLine = bufferedReader.readLine();
            }

            // If app gets here (no error occurred), we'll get the image code
            shibacode = new JSONArray(JSON).getString(0);

            // Now use it to load the actual image and then pass the image on...
            bitmap = BitmapFactory.decodeStream(
                    new URL("https://cdn.shibe.online/shibes/" + shibacode + ".jpg").openStream());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // ...there
        return new Shiba(shibacode, bitmap);
    }

    @Override
    protected void onPostExecute(Shiba result) {

        // Okay, we're through. Now give a moment to catch my breath
        // And then you'll be able click me again!
        button.setText(context.getResources().getText(R.string.button_shiny));

        Handler delay = new Handler();

        delay.postDelayed(new Runnable() {
            public void run() {
                button.setEnabled(true);
            }
        }, 2100);

        // If all is right, set the image
        if (result.bitmap != null) {
            imageView.setImageBitmap(result.bitmap);

            Toast toast = Toast.makeText(context, "Woof!", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
        }

    }
}
