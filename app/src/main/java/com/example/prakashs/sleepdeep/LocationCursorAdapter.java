package com.example.prakashs.sleepdeep;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by prakash.s on 16/08/17.
 */

/**
 * Responsible for populating the listview with items.
 * The cursor for this adapter is passed int the constructor
 */
public class LocationCursorAdapter extends CursorAdapter {

    public LocationCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    /**
     * This will create a new view base on the layout file we specify and pass it to bind view.
     * Called automatically
     */
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_row,parent,false);
    }

    @Override
    /**
     * Takes the view created by {@link LocationCursorAdapter#newView(Context, Cursor, ViewGroup)} and fill it with
     * data provided by the cursor parameter
     */
    public void bindView(View view, Context context, Cursor cursor) {


        TextView addressView = (TextView)view.findViewById(R.id.alarm_address);

        String address = cursor.getString(cursor.getColumnIndex(AlarmLocationContract.AlarmDetails.COLUMN_NAME_TARGET_ADDRESS));

        addressView.setText(address);
    }
}
