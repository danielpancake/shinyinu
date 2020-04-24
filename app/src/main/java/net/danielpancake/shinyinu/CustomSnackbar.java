package net.danielpancake.shinyinu;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

/*

    I didn't figure out how to extend BaseTransientBottomBar class
        so I created this instead...

*/

public class CustomSnackbar {

    private static Snackbar snackbar;

    static Snackbar make(View view, String text, Drawable icon, int duration) {

        snackbar = Snackbar.make(view, "", duration);

        View custom = LayoutInflater.from(view.getContext()).inflate(R.layout.custom_snackbar, null);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);

        ((TextView) custom.findViewById(R.id.snackbar_text)).setText(text);
        ((ImageView) custom.findViewById(R.id.snackbar_icon)).setImageDrawable(icon);

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);

        snackbarLayout.addView(custom, 0);

        return snackbar;
    }

    public static void show() {
        snackbar.show();
    }

}