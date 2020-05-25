package net.danielpancake.shinyinu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;

public class GridViewActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_STORAGE = 0;

    private ImageAdapter gridAdapter;
    private View root_view;

    private PhotoView photoViewer;
    private View photoViewerContainer;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        // Set up a view
        root_view = findViewById(R.id.root_view);

        // Set up a toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("> Bookmarked");
        setSupportActionBar(toolbar);

        // Set screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set up database
        DBHelper dbHelper = new DBHelper(this);

        // Set up home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get screen width
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Create photo viewer
        photoViewerContainer = findViewById(R.id.photo_viewer_container);
        photoViewer = findViewById(R.id.photo_viewer);

        ImageView nothingToShow = findViewById(R.id.nothing_here);

        // Create grid view
        GridView gridView = findViewById(R.id.grid_view);
        gridAdapter = new ImageAdapter(this, dbHelper, metrics.widthPixels);
        gridView.setAdapter(gridAdapter);

        if (gridAdapter.getCount() > 0) {
            nothingToShow.setVisibility(View.GONE);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    if (photoViewerContainer.getVisibility() == View.INVISIBLE) {
                        String code = gridAdapter.getItem(position);

                        if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                "/ShinyInu/" + code + ".jpg").exists()) {

                            ImageView imageView = view.findViewById(position);
                            ImageLoader imageLoader = new ImageLoader(view.getContext(), view, imageView, false);
                            imageLoader.execute("[\"" + code + "\"]");
                        } else {
                            PhotoViewerLoader photoViewerLoader = new PhotoViewerLoader(photoViewer);
                            photoViewerLoader.execute(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                    "/ShinyInu/" + code + ".jpg");

                            setPhotoViewerVisible();
                        }
                    }
                }
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String code = gridAdapter.getItem(position);

                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.gallery_menu, popupMenu.getMenu());

                int[] icons = {R.drawable.ic_share, R.drawable.ic_remove};

                for (int i = 0; i < icons.length; i++) {

                    MenuItem item = popupMenu.getMenu().getItem(i);
                    SpannableStringBuilder newMenuTitle = new SpannableStringBuilder("*   " + item.getTitle());
                    newMenuTitle.setSpan(new CenteredImageSpan(view.getContext(), icons[i]), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    item.setTitle(newMenuTitle);

                }

                MenuItem item = popupMenu.getMenu().getItem(1);
                SpannableStringBuilder newMenuTitle = new SpannableStringBuilder(item.getTitle());
                newMenuTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, newMenuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                newMenuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.design_default_color_error)), 0, newMenuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                item.setTitle(newMenuTitle);

                final Context context = view.getContext();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.option_share:
                                File shared_image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                        "/ShinyInu/" + code + ".jpg");

                                Intent share = new Intent(Intent.ACTION_SEND);

                                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                share.setType("image/jpg");
                                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shared_image));
                                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));

                                startActivity(Intent.createChooser(share, getString(R.string.share_title)));
                                break;

                            case R.id.option_remove:
                                // TODO: removing from database, grid and memory
                                break;
                        }

                        return true;
                    }
                });

                popupMenu.show();

                return true;
            }
        });
    }

    void setPhotoViewerInvisible() {
        getSupportActionBar().show();
        photoViewerContainer.animate().alpha(0f).setDuration(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                photoViewerContainer.setVisibility(View.INVISIBLE);
                photoViewer.setImageBitmap(null);
            }
        });
    }

    void setPhotoViewerVisible() {
        photoViewerContainer.setVisibility(View.VISIBLE);
        photoViewerContainer.animate().alpha(1f).setDuration(150).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                photoViewerContainer.setVisibility(View.VISIBLE);
                getSupportActionBar().hide();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar("Permission granted. Awof!", getDrawable(R.drawable.ic_shiba_status_ok));

                MainActivity.memoryCache.removeAllFromMemoryCache();
                gridAdapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {
        if (photoViewerContainer.getVisibility() == View.VISIBLE) {
            setPhotoViewerInvisible();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void showSnackbar(String string, Drawable icon) {
        CustomSnackbar.make(root_view, string, icon, Snackbar.LENGTH_LONG).show();
    }
}