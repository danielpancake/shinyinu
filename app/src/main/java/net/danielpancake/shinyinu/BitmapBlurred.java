package net.danielpancake.shinyinu;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BitmapBlurred {

    Context context;
    Bitmap bitmap;
    int blurSize;

    BitmapBlurred(Context context, Bitmap bitmap, int blurSize) {
        this.context = context;
        this.bitmap = bitmap;
        this.blurSize = blurSize;
    }

    Bitmap create() {
        RenderScript renderScript = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
        Allocation output = Allocation.createTyped(renderScript, input.getType());

        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setRadius(blurSize);
        scriptIntrinsicBlur.setInput(input);
        scriptIntrinsicBlur.forEach(output);

        output.copyTo(bitmap);

        return bitmap;
    }
}
