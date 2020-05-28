package net.danielpancake.shinyinu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Stack;

public class SettingsActivity extends BasicActivity {

    private TextView total;
    private TextView clear;
    private TextView delete;
    private DBHelper dbHelper;

    public static Stack<String> getFromDatabase(DBHelper dbHelper) {
        Stack<String> codeStack = new Stack<>();

        SQLiteDatabase database = dbHelper.getWritableDatabase();
        Cursor resultSet = database.query(DBHelper.TABLE_SHINY, new String[]{DBHelper.KEY_CODE, DBHelper.KEY_BITMAP_PREVIEW}, null, null, null, null, null);

        if (resultSet.moveToFirst()) {
            for (int i = 0; i < resultSet.getCount(); i++) {
                codeStack.add(resultSet.getString(resultSet.getColumnIndex(DBHelper.KEY_CODE)));

                if (!resultSet.moveToNext()) {
                    break;
                }
            }
        }

        resultSet.close();
        database.close();

        return codeStack;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Set up toolbars
        Toolbar toolbar = findViewById(R.id.toolbar).findViewById(R.id.actual_toolbar);
        toolbar.setTitle(R.string.title_settings);
        setSupportActionBar(toolbar);

        // Set up home buttons
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Set up database
        dbHelper = new DBHelper(this);

        // Get all buttons
        total = findViewById(R.id.shiny_size).findViewById(R.id.clear_size);

        View button_clear = findViewById(R.id.button_clear);
        View button_delete = findViewById(R.id.button_delete);

        clear = button_clear.findViewById(R.id.clear_size);
        delete = button_delete.findViewById(R.id.clear_size);

        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
            updateSize();
        }

        button_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    final Stack<String> imagesStack = getFromDatabase(dbHelper);

                    new AlertDialog.Builder(v.getContext())
                            .setMessage(R.string.are_you_sure)
                            .setPositiveButton(R.string.option_yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    while (!imagesStack.isEmpty()) {
                                        File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu/" +
                                                imagesStack.pop() + ".jpg");

                                        image.delete();
                                    }
                                    updateSize();
                                }

                            }).setNegativeButton(R.string.option_no, null).show();
                }
            }
        });

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE)) {
                    new AlertDialog.Builder(v.getContext())
                            .setMessage(R.string.are_you_sure)
                            .setPositiveButton(R.string.option_yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbHelper.drop();

                                    updateSize();
                                }

                            }).setNegativeButton(R.string.option_no, null).show();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    void updateSize() {
        DataCalculator dataCalculator = new DataCalculator(total, null);
        dataCalculator.execute(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu/");

        dataCalculator = new DataCalculator(clear, dbHelper);
        dataCalculator.execute(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu/");

        delete.setText(new DecimalFormat("##.##").format(dbHelper.getDatabaseSize() / 1000.00) + getString(R.string.kilobytes));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackbar(getString(R.string.permission_granted), getDrawable(R.drawable.ic_shiba_status_ok));
                updateSize();
            } else {
                showSnackbar(getString(R.string.permission_denied), getDrawable(R.drawable.ic_shiba_status_bad));
                checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_STORAGE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();

        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

class DataCalculator extends AsyncTask<String, Void, Long> {

    TextView textView;
    DBHelper dbHelper;

    DataCalculator(TextView textView, DBHelper dbHelper) {
        this.textView = textView;
        this.dbHelper = dbHelper;
    }

    @Override
    protected Long doInBackground(String[] directory) {
        if (dbHelper == null) {
            return directorySize(directory[0]);
        } else {
            return directorySize(SettingsActivity.getFromDatabase(dbHelper));
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onPostExecute(Long result) {
        super.onPostExecute(result);
        textView.setText(new DecimalFormat("##.##").format(result / 1000000.00) + textView.getContext().getString(R.string.megabytes));
    }

    long directorySize(String directory) {
        Stack<File> directoryList = new Stack<>();
        File dir = new File(directory);

        long result = 0;

        directoryList.clear();
        directoryList.push(dir);

        while (!directoryList.isEmpty()) {
            File dirCurrent = directoryList.pop();
            File[] fileList = dirCurrent.listFiles();

            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) {
                        directoryList.push(file);
                    } else {
                        result += file.length();
                    }
                }
            }
        }

        return result;
    }

    long directorySize(Stack<String> stack) {
        long result = 0;

        while (!stack.isEmpty()) {
            File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ShinyInu/" +
                    stack.pop() + ".jpg");

            result += image.length();
        }

        return result;
    }
}
