package net.danielpancake.shinyinu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/*
    Fit image inside image view

    Author: danielpancake
*/

public class ImageAdjuster {

    ImageAdjuster(final Context context, final ImageView imageView, Bitmap bitmap) {
        final RelativeLayout parent = (RelativeLayout) imageView.getParent();
        final BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), bitmap);

        parent.post(new Runnable() {
            @Override
            public void run() {
                int boundWidth = parent.getWidth();
                int boundHeight = parent.getHeight();

                int originalWidth = bitmapDrawable.getBitmap().getWidth();
                int originalHeight = bitmapDrawable.getBitmap().getHeight();
                int newWidth = originalWidth;
                int newHeight = originalHeight;

                if (newWidth < boundWidth || newWidth > boundWidth) {
                    newWidth = boundWidth;
                    newHeight = newWidth * originalHeight / originalWidth;
                }

                if (newHeight > boundHeight) {
                    newHeight = boundHeight;
                    newWidth = newHeight * originalWidth / originalHeight;
                }

                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                layoutParams.height = newHeight;
                layoutParams.width = newWidth;

                imageView.setLayoutParams(layoutParams);
                imageView.setBackground(bitmapDrawable);
            }
        });
    }

}
