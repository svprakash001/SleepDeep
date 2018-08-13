package com.example.prakashs.sleepdeep;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private String TAG = MainActivity.class.getSimpleName();

    private MySQLiteHelper msqlHelper;


    ListView alarmList;

    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("TYPE","new_alarm");
                startActivity(intent);

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        displayListOfAlarms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayListOfAlarms(){

        msqlHelper = new MySQLiteHelper(getBaseContext());

        db = msqlHelper.getReadableDatabase();

        String query = "SELECT * FROM "+ AlarmLocationContract.AlarmDetails.TABLE_NAME + " ORDER BY "
                +AlarmLocationContract.AlarmDetails._ID + " DESC";

        Cursor cursor = db.rawQuery(query,null);

        LocationCursorAdapter cursorAdapter = new LocationCursorAdapter(getBaseContext(),cursor);

        alarmList = (ListView) findViewById(R.id.alarm_list);


        //This is required to work along with AppBar Layout
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmList.setNestedScrollingEnabled(true);
        }

        //Every listview should be backed by an adapter. we are backing our listview with the cursor adapter
        //we created earlier. The cursor adapter is responsible for populating the listview with data from the database.
        //Everytime a new data is added, it adds a new item to the list view. The layout of individual item is described in
        //a seperate layout file.
        alarmList.setAdapter(cursorAdapter);

        alarmList.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        String req_id = cursor.getString(
                cursor.getColumnIndex(AlarmLocationContract.AlarmDetails.COLUMN_NAME_REQUEST_ID));
        String lat = cursor.getString(
                cursor.getColumnIndex(AlarmLocationContract.AlarmDetails.COLUMN_NAME_LAT));
        String lon = cursor.getString(
                cursor.getColumnIndex(AlarmLocationContract.AlarmDetails.COLUMN_NAME_LONG));
        int radius = cursor.getInt(
                cursor.getColumnIndex(AlarmLocationContract.AlarmDetails.COLUMN_NAME_RADIUS));
        String address = cursor.getString(
                cursor.getColumnIndex(AlarmLocationContract.AlarmDetails.COLUMN_NAME_TARGET_ADDRESS));

        Log.d(TAG,"The req_id is "+req_id);

        Intent intent = new Intent(MainActivity.this,MapsActivity.class);

        intent.putExtra("TYPE","existing_alarm");
        intent.putExtra("REQ_ID",req_id);
        intent.putExtra("LAT",lat);
        intent.putExtra("LON",lon);
        intent.putExtra("RADIUS",radius);
        intent.putExtra("ADDRESS",address);

        startActivity(intent);

    }
}
