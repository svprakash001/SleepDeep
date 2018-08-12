package com.example.prakashs.sleepdeep;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by prakash.s on 21/06/17.
 */

public class DatabaseLayer {

    final private String TAG = DatabaseLayer.class.getSimpleName();

    private MySQLiteHelper msqlHelper;

    public DatabaseLayer(Context context) {

        msqlHelper = new MySQLiteHelper(context);
    }

    public boolean insertLocationDetails(LocationDetails details){

        SQLiteDatabase db = msqlHelper.getWritableDatabase();

        String address = details.getGeofenceaddress() ;
        String reqId = details.getGeofenceId();

        ContentValues contentValues = new ContentValues();

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_TARGET_ADDRESS,address);

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_REQUEST_ID,reqId);

        long row_id = db.insert(AlarmLocationContract.AlarmDetails.TABLE_NAME,null,contentValues);

        Log.d(TAG,"The row id is "+ row_id);

        return true;

    }
}
