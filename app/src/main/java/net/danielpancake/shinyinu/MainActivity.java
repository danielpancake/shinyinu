package net.danielpancake.shinyinu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 0;

    private View root_view;

    private ShibaLoader shiba = null;

    private Bitmap bitmap;
    private String shibacode;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up a view
        root_view = findViewById(R.id.root_view);

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
        final ProgressBar progressBar = findViewById(R.id.loadingBar);

        loadShibaInu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                shiba = new ShibaLoader(getApplicationContext(), root_view, showShibaInu, loadShibaInu, progressBar);
                shiba.execute();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        int[] icons = {R.drawable.ic_save, R.drawable.ic_exit};

        for (int i = 0; i < icons.length; i++) {

            MenuItem item = menu.getItem(i);
            SpannableStringBuilder newMenuTitle = new SpannableStringBuilder("*     " + item.getTitle());
            newMenuTitle.setSpan(new CenteredImageSpan(this, icons[i]), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(newMenuTitle);

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.option_save:
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {

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

                            showSnackbar("Saved!", getDrawable(R.drawable.ic_shiba_status_ok));

                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saving_image)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        showSnackbar("Already saved!", getDrawable(R.drawable.ic_shiba_status));
                    }
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
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar("Permission granted. Awof!", getDrawable(R.drawable.ic_shiba_status_ok));
            } else {
                showSnackbar("Permission denied.", getDrawable(R.drawable.ic_shiba_status_bad));
            }
        }
    }

    public void showSnackbar(String string, Drawable icon) {
        CustomSnackbar.make(root_view, string, icon, Snackbar.LENGTH_LONG).show();
    }
}