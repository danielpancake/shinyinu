package net.danielpancake.shinyinu;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

/*
    This class defines basic functions for Activity
    such as permission requesting and showing messages

    Author: danielpancake
*/

public class BasicActivity extends AppCompatActivity {

    static final int PERMISSION_REQUEST_STORAGE = 0;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set screen orientation to portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar(getString(R.string.permission_granted), getDrawable(R.drawable.ic_shiba_status_ok));
            } else {
                showSnackbar(getString(R.string.permission_denied), getDrawable(R.drawable.ic_shiba_status_bad));
            }
        }
    }

    boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        } else {
            return true;
        }
    }

    void showSnackbar(String string, Drawable icon) {
        // Get root view of layout
        View root_view = findViewById(R.id.root_view);
        if (root_view != null) {
            CustomSnackbar.make(root_view, string, icon, Snackbar.LENGTH_LONG).show();
        }
    }
}
