package net.danielpancake.shinyinu;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class JSONGetter extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String[] api) {
        String JSON = "";

        try {
            // We use https to prevent errors on android API 25+
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(api[0]).openStream()));

            // Get JSON from the link above
            String inputLine = "";

            while (inputLine != null) {
                JSON += inputLine;
                inputLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (JSON.equals("") ? null : JSON);
    }
}