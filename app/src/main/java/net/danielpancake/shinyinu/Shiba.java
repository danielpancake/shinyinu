package net.danielpancake.shinyinu;

import android.graphics.Bitmap;

/*

    This class handles information from shibe.online about the image
    Code is used to save images

*/

public class Shiba {

    String code; //Shiba code
    Bitmap bitmap; //Bitmap data for image

    Shiba(String code, Bitmap bitmap) { //Setter method
        this.code = code;
        this.bitmap = bitmap;
    }

}
