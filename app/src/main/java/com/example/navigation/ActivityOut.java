package com.example.navigation;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ActivityOut extends ListActivity implements View.OnClickListener {

    private String LOG_TAG = "OUT";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();

        Cursor c = db.query("mytable", null, null, null, null, null, null);
        List<DbRow> dbValues = new ArrayList<>();
        DbRow dbRow;

        if (c.moveToFirst()) {
            int idColIndex = c.getColumnIndex("_id");
            int nameColIndex = c.getColumnIndex("name");
            int macColIndex = c.getColumnIndex("mac");
            int signalStrength = c.getColumnIndex("signalStrength");

            do {
                dbRow = new DbRow(c.getInt(idColIndex), c.getString(nameColIndex), c.getString(macColIndex), c.getString(signalStrength));
                dbValues.add(dbRow);
                Log.d(LOG_TAG, "ID = " + dbRow.getId() +
                        ", name = " + dbRow.getName() +
                        ", mac = " + dbRow.getMac() +
                        ", signal = " + dbRow.getSignalStrength());
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();

        setContentView(R.layout.activity_out);
        ListView list = findViewById(android.R.id.list);
        ArrayAdapter<DbRow> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dbValues);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(
                        getApplicationContext(),
                        "itemClick: " + parent.getAdapter().getItem(position),
                        Toast.LENGTH_SHORT).show();
            }
        });

        ConstraintLayout linear = findViewById(R.id.linearlayout2);
        linear.setOnTouchListener(new SwipeTouchListener(this));
    }


    public void onClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void clear(View view) {
        SQLiteDatabase db = MainActivity.dbHelper.getWritableDatabase();
        int clearCount = db.delete("mytable", null, null);
        Log.d(LOG_TAG, "deleted rows count = " + clearCount);

        ListView list = findViewById(android.R.id.list);
        List<DbRow> dbValues = new ArrayList<>();
        ArrayAdapter<DbRow> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dbValues);
        list.setAdapter(adapter);

        Toast.makeText(getApplicationContext(), "Table cleared", Toast.LENGTH_SHORT).show();
    }
}
