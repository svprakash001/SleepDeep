package com.example.prakashs.sleepdeep;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by prakash.s on 21/06/17.
 */


/*SQLiteOpenHelper class to get a readable/writable instance of db*/
public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String name = "SleepDeepDB";
    private static final int version = 1;


    private static final String TABLE_CREATE = "create table " + AlarmLocationContract.AlarmDetails.TABLE_NAME + " ( "
            + AlarmLocationContract.AlarmDetails._ID + " integer primary key autoincrement, "
            + AlarmLocationContract.AlarmDetails.COLUMN_NAME_TARGET_ADDRESS + " text not null, "
            + AlarmLocationContract.AlarmDetails.COLUMN_NAME_REQUEST_ID + " text not null);";


    public MySQLiteHelper(Context context) {
        super(context, name, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
