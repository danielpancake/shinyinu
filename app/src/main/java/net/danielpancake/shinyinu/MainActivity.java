package net.danielpancake.shinyinu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 0;

    private View root_view;
    private SharedPreferences settings;

    public static MemoryCache memoryCache = new MemoryCache();

    private Bitmap bitmap;
    private String shibacode;
    private ShibaLoader shibaLoader = null;
    private DBHelper dbHelper;

    private long doubleClickLastTime = 0;

    public static MemoryCache getMemoryCache() {
        return memoryCache;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Load settings
        settings = getSharedPreferences("Settings", 0);

        if (settings.getBoolean("ShowSplashScreen", true)) {
            setContentView(R.layout.control_hint);

            final Button splash_screen = findViewById(R.id.imageHint);
            final CheckBox skip = findViewById(R.id.showAgainBox);

            splash_screen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    splash_screen.setEnabled(false);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("ShowSplashScreen", !skip.isChecked());
                    editor.apply();

                    final Animation fadeout = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_out);
                    final Animation fadein = AnimationUtils.loadAnimation(v.getContext(), android.R.anim.fade_in);

                    findViewById(R.id.root_view).startAnimation(fadeout);
                    fadeout.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setContentView(R.layout.activity_main);
                            init();
                            findViewById(R.id.root_view).startAnimation(fadein);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                }
            });
        } else {
            setContentView(R.layout.activity_main);
            init();
        }
    }

    protected void init() {
        // Set up a view
        root_view = findViewById(R.id.root_view);

        // Set up a toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up database
        dbHelper = new DBHelper(this);

        // Set default image and code
        //     in case you want to save it
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.original);
        shibacode = "Original";

        // Load UI elements
        final Button loadShibaInu = findViewById(R.id.loadShibaInu);
        final ImageView showShibaInu = findViewById(R.id.imageShibaInu);
        final ImageView imageHeart = findViewById(R.id.imageHeart);
        final ProgressBar progressBar = findViewById(R.id.loadingBar);

        loadShibaInu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                shibaLoader = new ShibaLoader(v.getContext(), root_view, showShibaInu, loadShibaInu, progressBar);
                shibaLoader.execute();

                // Don't use shiba.get here because it causing app to lag
            }

        });

        showShibaInu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (System.currentTimeMillis() - doubleClickLastTime < 300) {
                    doubleClickLastTime = 0;

                    if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                        Drawable heart = getDrawable(R.drawable.ic_favourite_adv);
                        imageHeart.setImageDrawable(heart);

                        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) heart;
                        animatedVectorDrawable.stop();
                        animatedVectorDrawable.start();

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
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false);
                            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
                            byte[] image = byteArrayOutputStream.toByteArray();

                            // Put small scaled down image to the database
                            contentValues.put(DBHelper.KEY_BITMAP_PREVIEW, image);
                            database.insert(DBHelper.TABLE_SHINY, null, contentValues);
                        }

                        cursor.close();
                        database.close();
                    }
                } else {
                    doubleClickLastTime = System.currentTimeMillis();
                }
            }
        });

        showShibaInu.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Drawable heart = getDrawable(R.drawable.ic_share_adv);
                imageHeart.setImageDrawable(heart);

                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) heart;
                animatedVectorDrawable.stop();
                animatedVectorDrawable.start();

                shibaGet(); // Get information about the image
                // This is cached storage for our image
                File cachePath = new File(getCacheDir() + "/cache");

                if (!cachePath.isDirectory()) {
                    cachePath.mkdirs();
                }

                File cachedImage = new File(cachePath + "/shared_image.jpg");

                try {
                    FileOutputStream out = new FileOutputStream(cachedImage);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Uri contentUri = FileProvider.getUriForFile(v.getContext(), getPackageName() + ".fileprovider", cachedImage);

                if (contentUri != null) {
                    Intent share = new Intent(Intent.ACTION_SEND);

                    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    share.putExtra(Intent.EXTRA_STREAM, contentUri);
                    share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
                    share.setDataAndType(contentUri, getContentResolver().getType(contentUri));

                    startActivity(Intent.createChooser(share, getString(R.string.share_title)));
                }

                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        int[] icons = {R.drawable.ic_open, R.drawable.ic_save, R.drawable.ic_settings, R.drawable.ic_about, R.drawable.ic_exit};

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
            case R.id.option_open:
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    Intent gallery = new Intent(this, GridViewActivity.class);
                    startActivity(gallery);
                }
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

                memoryCache.removeBitmapFromMemoryCache(name);

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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar("Permission granted. Awof!", getDrawable(R.drawable.ic_shiba_status_ok));
            } else {
                showSnackbar("Permission denied.", getDrawable(R.drawable.ic_shiba_status_bad));
            }
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

    @Override
    public void onBackPressed() {
        closeApp();
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