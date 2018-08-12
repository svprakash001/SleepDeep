package com.example.prakashs.sleepdeep;

import android.provider.BaseColumns;

/**
 * Created by prakash.s on 20/06/17.
 */



/*Contract class for the database*/
public final class AlarmLocationContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private AlarmLocationContract() {
    }

    /* Inner class that defines the table contents */
    public static class AlarmDetails implements BaseColumns{

        public static final String TABLE_NAME = "ALARM_DETAILS";
        public static final String COLUMN_NAME_TARGET_ADDRESS  = "TARGET_ADDRESS";
        public static final String COLUMN_NAME_REQUEST_ID = "REQUEST_ID";
    }

}
