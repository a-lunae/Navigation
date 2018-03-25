package com.example.navigation;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends Activity implements LbsLocationListener, View.OnClickListener {


    private MainActivity instance;
    private WifiAndCellCollector wifiAndCellCollector;

    private Button btn_search;
    public ImageView imageView;
    public Bitmap pointer;
    private AlertDialog alert;
    private ProgressDialog progressDialog;
    static DBHelper dbHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        SharedPreferences settings = getPreferences(Activity.MODE_PRIVATE);
        String uuid = settings.getString("UUID", null);
        if (uuid == null) {
            uuid = generateUUID();
            SharedPreferences.Editor edit = settings.edit();
            edit.putString("UUID", uuid);
            edit.apply();
        }
        wifiAndCellCollector = new WifiAndCellCollector(this, this, uuid);

        setContentView(R.layout.activity_main);
        btn_search = findViewById(R.id.btn_search);
        imageView = findViewById(R.id.imageView);
        pointer = BitmapFactory.decodeResource(getResources(), R.drawable.pointer);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.hide();
                }
                progressDialog = ProgressDialog.show(instance, null, "Please wait");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.show();

                (new Thread() {
                    @Override
                    public void run() {
                        wifiAndCellCollector.requestMyLocation();
                    }
                }).start();


            }
        });

        ConstraintLayout linear = findViewById(R.id.linearlayout1);
        linear.setOnTouchListener(new SwipeTouchListener(this));

    /*    Bitmap plan = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        Bitmap mutableBitmap = plan.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        Matrix matrix = new Matrix();
        matrix.setRotate(-25, canvas.getWidth()/2, canvas.getHeight()/2);
        canvas.drawBitmap(plan, matrix, null);

        imageView.setImageBitmap(mutableBitmap);*/

        dbHelper = new DBHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wifiAndCellCollector.startCollect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        wifiAndCellCollector.stopCollect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLocationChange(final Info info) {
        if (info != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.hide();
                    }
                    if (info.isError) {
                        if (alert != null && alert.isShowing()) {
                            alert.hide();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(instance);
                        builder.setMessage(info.errorMessage)
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick (DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        alert = builder.create();
                        alert.show();
//                        lbsLatitude.setText("");
//                        lbsLongtitude.setText("");
//                        lbsPrecision.setText("");
                    } else {
//                        lbsLatitude.setText(String.format("Latitude=%s", info.lbsLatitude));
//                        lbsLongtitude.setText(String.format("Longtitude=%s", info.lbsLongtitude));
//                        lbsPrecision.setText(String.format("Precision=%s", info.lbsPrecision));

                        Bitmap plan = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                        Bitmap mutableBitmap = plan.copy(Bitmap.Config.ARGB_8888, true);
                        Canvas canvas = new Canvas(mutableBitmap);

                        int canvas_H = canvas.getHeight();
                        int canvas_W = canvas.getWidth();

                        float pixel_H = (float) (56.145795 - 56.146235) / (canvas_H/2);
                        float pixel_W = (float) (40.373987 - 40.373268) / (canvas_W/2);
                        float y = (float) ((56.145795 - Double.parseDouble(info.lbsLatitude)) / pixel_H);
                        float x = (float) ((40.373987 - Double.parseDouble(info.lbsLongtitude)) / pixel_W);
                        Log.d("PIXELS!!! ",x + " " + y);

                        Paint paint;
                        Path path = new Path();
                        paint = new Paint();
                        paint.setColor(Color.RED);
                        paint.setStyle(Paint.Style.STROKE);

                        path.addCircle(canvas_W/2-x-55+90, canvas_H/2-y+50+150, (float) (Double.parseDouble(info.lbsPrecision)/5+120), Path.Direction.CCW);
                        canvas.drawPath(path, paint);
                        canvas.drawBitmap(pointer, canvas_W/2-x-65+90, canvas_H/2-y+10+150, null);

                        Log.d("Canvas!!! ",canvas.getWidth() + " " + canvas.getHeight());
                        imageView.setImageBitmap(mutableBitmap);
                    }
                }
            });
        }
    }

    /**
     * RFC UUID generation
     */
    public String generateUUID() {
        UUID uuid = UUID.randomUUID();
        StringBuilder str = new StringBuilder(uuid.toString());
        int index = str.indexOf("-");
        while (index > 0) {
            str.deleteCharAt(index);
            index = str.indexOf("-");
        }
        return str.toString();
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, ActivityOut.class);
        startActivity(intent);
    }

    static class DBHelper extends SQLiteOpenHelper {
        private String LOG_TAG = "Logs";

        DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(LOG_TAG, "--- OnCreate database ---");
            db.execSQL(String.format("create table mytable (_id integer primary key autoincrement,name text,mac text, signalStrength text);"));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

}
