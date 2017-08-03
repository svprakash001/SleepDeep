package com.example.prakashs.sleepdeep;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by prakash.s on 21/06/17.
 */

public class DatabaseLayer {


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

        db.insert(AlarmLocationContract.AlarmDetails.TABLE_NAME,null,contentValues);

        return true;

    }

}
