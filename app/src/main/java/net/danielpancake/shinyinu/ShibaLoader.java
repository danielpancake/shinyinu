package net.danielpancake.shinyinu;

/*

    This class loads Shiba's image from shibe.online

*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ShibaLoader extends AsyncTask <Void, Void, Bitmap> {

    private Context context;
    private ImageView imageView;
    private Button button;

    public ShibaLoader(Context context, ImageView imageView, Button button) {

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
    protected Bitmap doInBackground(Void[] nothing) {

        String JSON = "";
        Bitmap bitmap = null;

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
            String shibacode = new JSONArray(JSON).getString(0);

            // Now use it to load the actual image and then pass the image on...
            bitmap = BitmapFactory.decodeStream(
                    new URL("https://cdn.shibe.online/shibes/" + shibacode + ".jpg").openStream());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // ...there
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {

        // Okay, we're through, you can click me again!
        button.setEnabled(true);
        button.setText(context.getResources().getText(R.string.button_shiny));

        // If all is right, set the image
        if (result != null) {
            imageView.setImageBitmap(result);
        }

    }
}
