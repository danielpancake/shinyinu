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

public class CustomSnackbar { //Custom snackbar class

    private static Snackbar snackbar; //Create new snackbar object

    static Snackbar make(View view, String text, Drawable icon, int duration) { //Set snackbar values

        snackbar = Snackbar.make(view, "", duration); //Show snackbar in given viewport for a given duration

        View custom = LayoutInflater.from(view.getContext()).inflate(R.layout.custom_snackbar, null); //Create custom view
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT); //Set color of snackbar to transparent to set custom

        ((TextView) custom.findViewById(R.id.snackbar_text)).setText(text); //Set snackbar text
        ((ImageView) custom.findViewById(R.id.snackbar_icon)).setImageDrawable(icon); //Set sanckbar image

        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView(); //Create layout for snackbar
        snackbarLayout.setPadding(0, 0, 0, 0); //Set padding of snackbar to 0

        snackbarLayout.addView(custom, 0); //Add custom view

        return snackbar; //return the snackbar
    }

    public static void show() { //Wrapper show method
        snackbar.show();
    }

}