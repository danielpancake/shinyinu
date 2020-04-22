package net.danielpancake.shinyinu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ShibaLoader shiba = null;

    private Bitmap bitmap;
    private String shibacode;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set default image and code
        // In case you want to save it
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.original);
        shibacode = "Original";

        // Load UI elements
        final Button loadShibaInu = findViewById(R.id.loadShibaInu);
        final ImageView showShibaInu = findViewById(R.id.imageShibaInu);

        loadShibaInu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                shiba = new ShibaLoader(getApplicationContext(), showShibaInu, loadShibaInu);
                shiba.execute();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.option_save:
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 1)) {

                    if (shiba != null) {
                        try {
                            bitmap = shiba.get().bitmap;
                            shibacode = shiba.get().code;
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    // Let's create folder for our images inside phone's pictures folder
                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu");
                    if (!directory.isDirectory()) {
                        directory.mkdirs();
                    }

                    File saving_image = new File(directory, shibacode + ".jpg");

                    if (!saving_image.exists()) {
                        try {
                            FileOutputStream out = new FileOutputStream(saving_image);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            out.flush();
                            out.close();

                            showToastBottom("Saved!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showToastBottom("Already saved!");
                    }

                } else {
                    showToastBottom("Permission denied. Please, try again!");
                }

                break;

            case R.id.option_exit:
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure you want to close the app?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }

                        }).setNegativeButton("No", null).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public Boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    public void showToastBottom(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, getResources().getInteger(R.integer.toast_offset));
        toast.show();
    }
}