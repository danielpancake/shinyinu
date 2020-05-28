package net.danielpancake.shinyinu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BasicActivity {

    public static MemoryCache memoryCache = new MemoryCache();

    private SharedPreferences settings;

    private View root_view;
    private Bitmap bitmap;
    private String shibacode;
    private ShibaLoader shibaLoader = null;
    private DBHelper dbHelper;

    private long doubleClickLastTime = 0;

    public static MemoryCache getMemoryCache() {
        return memoryCache;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up a view
        root_view = findViewById(R.id.root_view);

        // Set up a toolbar
        Toolbar toolbar = findViewById(R.id.actual_toolbar);
        setSupportActionBar(toolbar);

        // Set up database
        dbHelper = new DBHelper(this);

        // Set default image and code
        //     in case you want to save it
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.original);
        shibacode = "Original";

        // Load UI elements
        final Button loadShibaInu = findViewById(R.id.loadShibaInu);
        final ImageView imageAVD = findViewById(R.id.imageAVD);
        final ImageView showShibaInu = findViewById(R.id.imageShibaInu);
        final ProgressBar progressBar = findViewById(R.id.loadingBar);

        // Set up settings
        settings = getSharedPreferences("Settings", 0);

        // Show tutorial
        if (settings.getBoolean("showcase", true)) {
            loadShibaInu.setVisibility(View.INVISIBLE);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showIntro(getString(R.string.intro_title_01), getString(R.string.intro_text_01), new View[]{showShibaInu, showShibaInu, loadShibaInu}, 0);
                }
            }, 100);
        }

        new ImageAdjuster(this, showShibaInu, BitmapFactory.decodeResource(getResources(), R.drawable.original));

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
                // This is double click realization
                if (System.currentTimeMillis() - doubleClickLastTime < 300) {
                    doubleClickLastTime = 0;

                    if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                        // Setup heart vector animation
                        Drawable heart = getDrawable(R.drawable.ic_favourite_adv);
                        imageAVD.setImageDrawable(heart);

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
                            showSnackbar(getString(R.string.favourite_already), getDrawable(R.drawable.ic_shiba_status));
                        } else {
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

                            showSnackbar(getString(R.string.favourite), getDrawable(R.drawable.ic_shiba_favourite));
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
                Drawable avdshare = getDrawable(R.drawable.ic_share_adv);
                imageAVD.setImageDrawable(avdshare);

                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) avdshare;
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

    @SuppressLint("ClickableViewAccessibility")
    void showIntro(String title, String text, final View[] view, final int phase) {
        ShowcaseView.Builder showcase = showcase(title, text, view[phase]);

        final ShowcaseView build = showcase.build();
        build.hideButton();

        build.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    switch (phase) {
                        case 0:
                            showIntro(getString(R.string.intro_title_02), getString(R.string.intro_text_02), view, 1);
                            build.hide();
                            break;

                        case 1:
                            View button = view[2];
                            button.setVisibility(View.VISIBLE);
                            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) button.getLayoutParams();

                            TranslateAnimation animation = new TranslateAnimation(0, 0, button.getHeight() + marginLayoutParams.bottomMargin, 0);
                            animation.setDuration(1000);
                            animation.setFillAfter(true);

                            button.startAnimation(animation);

                            showIntro(getString(R.string.intro_title_03), getString(R.string.intro_text_03), view, 2);
                            build.hide();
                            break;

                        case 2:
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("showcase", false);
                            editor.apply();

                            build.hide();
                            break;
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    ShowcaseView.Builder showcase(String title, String text, View view) {
        ShowcaseView.Builder showcase = new ShowcaseView.Builder(this)
                .setTarget(new ViewTarget(view))
                .setStyle(R.style.CustomShowcaseTheme)
                .setContentTitle(title)
                .setContentText(text)
                .hideOnTouchOutside();

        return showcase;
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

            case R.id.option_about:
                showSnackbar(getString(R.string.made) + " danielpancake", getDrawable(R.drawable.ic_shiba_status));
                break;

            case R.id.option_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
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
                    showSnackbar(getString(R.string.saved), getDrawable(R.drawable.ic_shiba_status_ok));
                }

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(saving_image)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (showMessage) {
            showSnackbar(getString(R.string.saved_already), getDrawable(R.drawable.ic_shiba_status));
        }
    }

    private void closeApp() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.close_app)
                .setPositiveButton(R.string.option_yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                }).setNegativeButton(R.string.option_no, null).show();
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
}