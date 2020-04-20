package net.danielpancake.shinyinu;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load UI elements
        final Button loadShibaInu = findViewById(R.id.loadShibaInu);
        final ImageView showShibaInu = findViewById(R.id.imageShibaInu);

        loadShibaInu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ShibaLoader shiba = new ShibaLoader(getApplicationContext(), showShibaInu, loadShibaInu);
                shiba.execute();
            }
        });

    }
}
