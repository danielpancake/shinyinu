package net.danielpancake.shinyinu;

import android.graphics.Bitmap;

/*

    This class handles information from shibe.online about the image
    Code is used to save images

*/

public class Shiba {

    String code;
    Bitmap bitmap;

    Shiba(String code, Bitmap bitmap) {
        this.code = code;
        this.bitmap = bitmap;
    }

}
