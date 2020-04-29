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

        final Button loadShibaInu = findViewById(R.id.loadShibaInu);        // Create a button to lead new dogs
        final ImageView showShibaInu = findViewById(R.id.imageShibaInu);    //Create a new viewport to view the images
        final ProgressBar progressBar = findViewById(R.id.loadingBar);      //Create new progress bar

        loadShibaInu.setOnClickListener(new View.OnClickListener() { //Add click listener to the button
            public void onClick(View v) { //On click
                shiba = new ShibaLoader(getApplicationContext(), root_view, showShibaInu, loadShibaInu, progressBar); //Create new shiba class with parameters
                shiba.execute(); //Launch shiba loading process
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
    public boolean onOptionsItemSelected(MenuItem item) { //When user tapped on options menu
        switch (item.getItemId()) { //See which item the user pressed
            case R.id.option_save: //If the user decided to save
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) { //If permission to write to storeage is granted
                    if (shiba != null) { //If not saved
                        try {
                            bitmap = shiba.get().bitmap; //Get image from the current shiba
                            shibacode = shiba.get().code; //Get code from shiba
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Create a folder where all images are stored
                    File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu"); //Create folder
                    if (!directory.isDirectory()) { //If directory doesnt exist
                        directory.mkdirs(); //Create directory
                    }

                    File saving_image = new File(directory, shibacode + ".jpg"); //Create file with jpg extension

                    if (!saving_image.exists()) { //If image does not exist
                        try {
                            FileOutputStream out = new FileOutputStream(saving_image); //Create file stream
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); //Compress with jpeg algorithm
                            out.flush(); //Flush file
                            out.close(); //Close file

                            showSnackbar("Saved!", getDrawable(R.drawable.ic_shiba_status_ok)); //Notify user that everything is saved
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saving_image))); //Broadcast that file is saved
                        } catch (IOException e) { //If error occurs
                            e.printStackTrace(); //Print the stack trace
                        }
                    } else { //If image exists
                        showSnackbar("Already saved!", getDrawable(R.drawable.ic_shiba_status)); //Notify that already saved
                    }
                }
                break;
            case R.id.option_exit: //If user decided to exit
                new AlertDialog.Builder(this) //Create alert
                        .setMessage("Are you sure you want to close the app?") //Set message alert
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() { //Set positive answer
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish(); //Exit application
                            }
                        }).setNegativeButton("No", null).show(); //Otherwise nothing happens
                break;
        }
        return super.onOptionsItemSelected(item); //Return selected item
    }

    public Boolean checkPermission(String permission, int requestCode) { //Check permissions
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) { //If permission is denied
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode); //Request permission again
            return false; //Return false
        } else {
            return true; //Return true if permission is granted
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //Request permission
        if (requestCode == PERMISSION_REQUEST_STORAGE) { //Request storage permissions
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //If granted
                showSnackbar("Permission granted. Awof!", getDrawable(R.drawable.ic_shiba_status_ok)); //Notify user
            } else { //If not
                showSnackbar("Permission denied.", getDrawable(R.drawable.ic_shiba_status_bad)); //Notify user that permission is not granted
            }
        }
    }

    public void showSnackbar(String string, Drawable icon) { //Show snackbar
        CustomSnackbar.make(root_view, string, icon, Snackbar.LENGTH_LONG).show(); //Make a custom snackbar
    }
}