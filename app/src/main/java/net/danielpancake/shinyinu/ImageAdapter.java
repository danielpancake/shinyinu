package net.danielpancake.shinyinu;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context context;

    private ArrayList<String> imagesList = new ArrayList<>();
    private ArrayList<byte[]> imagesPreviewList = new ArrayList<>();

    private int size;

    public ImageAdapter(Context context, DBHelper dbHelper, int size) {
        this.context = context;

        // Count how many squares 240x240 can fit in one row of grid
        int count = (int) Math.floor(size / 240);
        this.size = size / count;

        GridView gridView = ((Activity) context).findViewById(R.id.grid_view);
        gridView.setNumColumns(count);

        // Get all codes and previews from database
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor resultSet = database.query(DBHelper.TABLE_SHINY, new String[]{DBHelper.KEY_CODE, DBHelper.KEY_BITMAP_PREVIEW}, null, null, null, null, null);

        if (resultSet.moveToFirst() != false) {
            for (int i = 0; i < resultSet.getCount(); i++) {
                imagesList.add(resultSet.getString(resultSet.getColumnIndex(DBHelper.KEY_CODE)));
                imagesPreviewList.add(resultSet.getBlob(resultSet.getColumnIndex(DBHelper.KEY_BITMAP_PREVIEW)));

                if (resultSet.moveToNext() == false) {
                    break;
                }
            }
        }

        resultSet.close();
        database.close();
    }

    @Override
    public int getCount() {
        return imagesList.size();
    }

    @Override
    public String getItem(int position) {
        return imagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public FrameLayout getView(int position, View convertView, ViewGroup parent) {
        FrameLayout frameLayout = new FrameLayout(context);

        ImageView imageView = new ImageView(context);
        ImageView imageViewOverlay = new ImageView(context);

        imageView.setId(position);
        imageView.setPadding(4, 4, 4, 4);
        imageView.setCropToPadding(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(size, size));

        imageViewOverlay.setPadding(4, 4, 4, 4);
        imageViewOverlay.setLayoutParams(new GridView.LayoutParams(size, size));

        imageView.setImageDrawable(context.getDrawable(R.drawable.ic_shiba_placeholder));

        String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                "/ShinyInu/" + imagesList.get(position) + ".jpg";

        if (MainActivity.getMemoryCache().getBitmapFromMemoryCache(imagesList.get(position)) == null) {
            ImageAsyncLoader imageAsyncLoader = new ImageAsyncLoader(imageView, imagesList.get(position), imagesPreviewList.get(position));
            imageAsyncLoader.execute(imagePath);
        } else {
            imageView.setImageBitmap(MainActivity.getMemoryCache().getBitmapFromMemoryCache(imagesList.get(position)));
        }

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
        imageViewOverlay.setBackgroundResource(typedValue.resourceId);

        if (!new File(imagePath).exists()) {
            frameLayout.setForeground(context.getDrawable(R.drawable.ic_load));
        }

        frameLayout.addView(imageView);
        frameLayout.addView(imageViewOverlay);

        return frameLayout;
    }
}

