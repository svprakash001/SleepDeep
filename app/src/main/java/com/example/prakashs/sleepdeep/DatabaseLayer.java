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
        int radius = details.getGeofenceRadius();
        String lat = details.getGeofenceLat();
        String lon = details.getGeofenceLong();

        ContentValues contentValues = new ContentValues();

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_TARGET_ADDRESS,address);

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_REQUEST_ID,reqId);

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_RADIUS,radius);

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_LAT,lat);

        contentValues.put(AlarmLocationContract.AlarmDetails.COLUMN_NAME_LONG,lon);

        long row_id = db.insert(AlarmLocationContract.AlarmDetails.TABLE_NAME,null,contentValues);

        Log.d(TAG,"The row id is "+ row_id);

        return true;
    }

    /**
     * Delete the row iwth give request id (not _id which is added by the db)
     * @param req_id
     * @return true if deletion successfull, else false
     */
    public boolean deleteLocation(String req_id){

        SQLiteDatabase db = msqlHelper.getWritableDatabase();

        String where_clause = AlarmLocationContract.AlarmDetails.COLUMN_NAME_REQUEST_ID+"=?";
        String[] where_args = new String[]{req_id};

        //Output will contain the no of rows affected
        int out = db.delete(AlarmLocationContract.AlarmDetails.TABLE_NAME,where_clause,where_args);

        if(out == 1){
            return true;
        }
        return false;
    }
}
