package net.danielpancake.shinyinu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 0;

    private View root_view;

    public static MemoryCache memoryCache = new MemoryCache();

    private Bitmap bitmap;
    private String shibacode;
    private ShibaLoader shibaLoader = null;
    private DBHelper dbHelper;

    public static MemoryCache getMemoryCache() {
        return memoryCache;
    }

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

        // Set up database
        dbHelper = new DBHelper(this);

        // Set default image and code
        //     in case you want to save it
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.original);
        shibacode = "Original";

        // Load UI elements
        final Button loadShibaInu = findViewById(R.id.loadShibaInu);
        final ImageView showShibaInu = findViewById(R.id.imageShibaInu);
        final ProgressBar progressBar = findViewById(R.id.loadingBar);

        loadShibaInu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                shibaLoader = new ShibaLoader(getApplicationContext(), root_view, showShibaInu, loadShibaInu, progressBar);
                shibaLoader.execute();

                // Don't use shiba.get here because it causing app to lag
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        int[] icons = {R.drawable.ic_favourite, R.drawable.ic_open, R.drawable.ic_save, R.drawable.ic_exit};

        for (int i = 0; i < icons.length; i++) {

            MenuItem item = menu.getItem(i);
            SpannableStringBuilder newMenuTitle = new SpannableStringBuilder("*    " + item.getTitle());
            newMenuTitle.setSpan(new CenteredImageSpan(this, icons[i]), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            item.setTitle(newMenuTitle);

        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_favourite:
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    shibaGet(); // Get information about the image
                    // Save the image to external storage
                    bitmapSaveToFile(shibacode, bitmap, false);

                    // and then to the database
                    SQLiteDatabase database = dbHelper.getWritableDatabase();

                    // Find any matches
                    Cursor cursor = database.query(DBHelper.TABLE_SHINY,
                            new String[]{DBHelper.KEY_CODE}, DBHelper.KEY_CODE + " =?", new String[]{shibacode},
                            null, null, null, "1");

                    if (cursor.getCount() > 0) {
                        showSnackbar("Already added to favourite.", getDrawable(R.drawable.ic_shiba_status));
                    } else {
                        showSnackbar("Added to favourite!", getDrawable(R.drawable.ic_shiba_favourite));

                        ContentValues contentValues = new ContentValues();

                        // Put image code to the database
                        contentValues.put(DBHelper.KEY_CODE, shibacode);

                        // Scale down image to store is as a preview
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, false);
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                        byte[] image = byteArrayOutputStream.toByteArray();

                        // Put small scaled down image to the database
                        contentValues.put(DBHelper.KEY_BITMAP_PREVIEW, image);

                        database.insert(DBHelper.TABLE_SHINY, null, contentValues);
                    }

                    cursor.close();
                    database.close();
                }
                break;

            case R.id.option_open:
                Intent intent = new Intent(this, GridViewActivity.class);
                startActivity(intent);
                break;

            case R.id.option_save:
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    shibaGet();
                    bitmapSaveToFile(shibacode, bitmap, true);
                }
                break;

            case R.id.option_exit:
                closeApp();
                break;
        }

        return super.onOptionsItemSelected(item);
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

    private void bitmapSaveToFile(String name, Bitmap bitmap, boolean showMessage) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu");
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        File saving_image = new File(directory, name + ".jpg");

        if (!saving_image.exists()) {
            try {
                FileOutputStream out = new FileOutputStream(saving_image);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                memoryCache.removeBitmapFromMemoryCache(shibacode);

                if (showMessage) {
                    showSnackbar("Saved!", getDrawable(R.drawable.ic_shiba_status_ok));
                }

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saving_image)));

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (showMessage) {
            showSnackbar("Already saved.", getDrawable(R.drawable.ic_shiba_status));
        }
    }

    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    private void closeApp() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to close the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                }).setNegativeButton("No", null).show();
    }

    private void shibaGet() {
        try {
            Shiba shiba = shibaLoader.get();

            if (shiba.bitmap != null && shiba.code != null) {
                bitmap = shiba.bitmap;
                shibacode = shiba.code;
            }

        } catch (NullPointerException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void showSnackbar(String string, Drawable icon) {
        CustomSnackbar.make(root_view, string, icon, Snackbar.LENGTH_LONG).show();
    }
}