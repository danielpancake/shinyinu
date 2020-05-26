package net.danielpancake.shinyinu;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;

public class GridViewActivity extends BasicActivity {

    private ImageAdapter gridAdapter;

    private PhotoView photoViewer;
    private View photoViewerContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);

        // Set up a toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle("> Bookmarked");
        setSupportActionBar(toolbar);

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

        // Create grid view
        GridView gridView = findViewById(R.id.grid_view);
        gridAdapter = new ImageAdapter(this, dbHelper, metrics.widthPixels);
        gridView.setAdapter(gridAdapter);

        // If there's nothing to show, put an image of sleeping Shiba
        final ImageView nothingToShow = findViewById(R.id.nothing_here);
        if (gridAdapter.getCount() > 0) {
            nothingToShow.setVisibility(View.INVISIBLE);
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
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                final String code = gridAdapter.getItem(position);

                PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.gallery_menu, popupMenu.getMenu());

                int[] icons = {R.drawable.ic_share, R.drawable.ic_remove, R.drawable.ic_delete};

                for (int i = 0; i < icons.length; i++) {

                    MenuItem item = popupMenu.getMenu().getItem(i);
                    SpannableStringBuilder newMenuTitle = new SpannableStringBuilder("*   " + item.getTitle());
                    newMenuTitle.setSpan(new CenteredImageSpan(view.getContext(), icons[i]), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    item.setTitle(newMenuTitle);

                }

                if (!new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                        "/ShinyInu/" + code + ".jpg").exists()) {
                    popupMenu.getMenu().getItem(1).setEnabled(false);
                }

                MenuItem item = popupMenu.getMenu().getItem(2);
                SpannableStringBuilder newMenuTitle = new SpannableStringBuilder(item.getTitle());
                newMenuTitle.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, newMenuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                newMenuTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.design_default_color_error)), 0, newMenuTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                item.setTitle(newMenuTitle);

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.option_share:
                                File shared_image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                                        "/ShinyInu/" + code + ".jpg");

                                Intent share = new Intent(Intent.ACTION_SEND);

                                share.setType("image/jpg");
                                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shared_image));
                                share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));

                                startActivity(Intent.createChooser(share, getString(R.string.share_title)));
                                break;

                            case R.id.option_remove:
                                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                                    gridAdapter.removeItem(position);
                                }
                                break;

                            case R.id.option_delete:
                                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                                    new AlertDialog.Builder(view.getContext())
                                            .setMessage("Are you sure?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    gridAdapter.deleteItem(position);

                                                    if (gridAdapter.getCount() == 0) {
                                                        nothingToShow.setVisibility(View.VISIBLE);
                                                    }
                                                }

                                            }).setNegativeButton("No", null).show();
                                }
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            MainActivity.memoryCache.removeAllFromMemoryCache();
            gridAdapter.notifyDataSetChanged();
        }
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

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
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
}