package com.example.prakashs.sleepdeep;

import android.Manifest;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

//TODO : Set the map to current location

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>, NumberPicker.OnValueChangeListener, View.OnClickListener {

    final private String TAG = MapsActivity.class.getSimpleName();

    private static final int PENDING_INTENT_REQUEST_CODE = 99;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 653;
    private static final int DEFAULT_ZOOM = 8;

    private PlaceAutocompleteFragment autocompleteFragment;

    private GoogleMap mMap;
    private LatLng destinationCoordinates;
    private String destinationAddress;
    private int zoomLevel = 14;
    private int radius = 1000;

    private LatLng defaultCoordinates;
    private Circle borderCircle;

    private Button setBtn;
    private Button cancelActionBtn;
    private TextView radius_text;
    private int setBtnState;

    private BottomSheetBehavior mbottomSheetBehavior;

    private String called_from;


    /*Geofencing*/
    ArrayList<Geofence> geofenceList = new ArrayList<>();
    Geofence fence;
    GeofencingRequest geoRequest;
    String reqId;

    //Pendingintent given to Location service on behalf of our app
    PendingIntent pendingIntent;

    /*Google API client*/
    GoogleApiClient googleClient;


    // Number Picker value Array
    String[] radiusValues = {"50", "100", "200", "500", "750", "1000", "1500", "2000", "2500", "3000", "5000"};


    //Database support class

    DatabaseLayer dbLayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        Intent intent = getIntent();

        called_from = intent.getStringExtra("TYPE");

        //This activity can be called from two places on MainActivity.
        //1. By clicking '+' button
        //2. By clicking on an item in listview
        //If this activity was started by second method, get all the extra String, so that we can draw the circle & if the user
        //presses 'DELETE' we can delete this item from DB

        if(called_from.equals("existing_alarm")){

            Log.d(TAG,"Type is existiing alarm");

            reqId = intent.getStringExtra("REQ_ID");
            radius = intent.getIntExtra("RADIUS",-1);
            destinationAddress = intent.getStringExtra("ADDRESS");

            //Reconstruct a latlng object from the strings lat and lon. First convert to double and then to LatLng
            String lat = intent.getStringExtra("LAT");
            String lon = intent.getStringExtra("LON");
            Double l1 = Double.parseDouble(lat);
            Double l2 = Double.parseDouble(lon);
            destinationCoordinates = new LatLng(l1,l2);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        //Set background color of the placeSelector
        View vk = findViewById(R.id.place_autocomplete_fragment);
        vk.setBackgroundColor(Color.parseColor("#ffffff"));


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());

                destinationCoordinates = place.getLatLng();
                destinationAddress = place.getAddress().toString();

                mMap.addMarker(new MarkerOptions().position(destinationCoordinates)
                        .title(destinationAddress)
                        .draggable(true));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationCoordinates, zoomLevel));

                setBtn.setEnabled(true);
                setBtn.setText("SET LOCATION");
                setBtn.setTextColor(Color.parseColor("#000000"));
                setBtnState = 1;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        //Initialise and handle bottomsheet using {@link android.support.design.widget.BottomSheetBehavior}
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mbottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        //mbottomSheetBehavior.setHideable(true);
        mbottomSheetBehavior.setPeekHeight(100);


        //  SET LOCATION BUTTON

        //Customise some UI elements which are easier to do in code rather than xml
        setBtn = (Button) findViewById(R.id.setBtn);
        setBtn.setEnabled(false);
        setBtnState = 0;

        cancelActionBtn = (Button) findViewById(R.id.cancel_action_btn);

        radius_text = (TextView) findViewById(R.id.radius_text);

        radius_text.setOnClickListener(this);


        //Build google API client. This is required for Geofencing
        createGoogleApi();


        setBtn.setOnClickListener(this);


        //Cancel button listener
        cancelActionBtn.setOnClickListener(this);
    }


    //    Function to change the Radius using Radius Picker
    private void showRadiusPicker() {

        final Dialog d = new Dialog(MapsActivity.this);
        d.setTitle("NumberPicker");

        d.setContentView(R.layout.radius_dialog);

        Button b1 = (Button) d.findViewById(R.id.button_set);
        Button b2 = (Button) d.findViewById(R.id.button_cancel);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);

        np.setMinValue(0);

        np.setMaxValue(10);


        np.setDisplayedValues(radiusValues);


        np.setWrapSelectorWheel(true);

        np.setOnValueChangedListener(this);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int rad_val = Integer.parseInt(radiusValues[np.getValue()]);
                radius = rad_val;
                radius_text.setText("Radius " + rad_val + " m");
                d.dismiss();
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        d.show();
    }


    //    Callback for numberPicker Value changed
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        Log.i("value is", "" + newVal);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(called_from.equals("existing_alarm")){
            renderAlarmSetState();
            return;
        }

        // Set Default Coordinates as Bengaluru
        defaultCoordinates = new LatLng(12.9716, 77.5946);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCoordinates, DEFAULT_ZOOM));


    }


    private void createGoogleApi() {
        Log.i(TAG, "Creating googleee client");
        googleClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.w(TAG, "onConnectionFailed");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG, "onConnectionSuspended");

    }

    /**
     * Check if the app has Location Permission. If not request permission
     */
    private void checkLocationPermission() {

        Log.d(TAG, "checking permission");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            addGeoFence();

        } else {

            Log.d(TAG, "Requesting location permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    @Override
    /**
     * Called when the Location permission is either granted or denied
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Location Permission granted");
            addGeoFence();
        } else {
            Toast.makeText(this, "Please grant Location access to set alarm", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Location Permission denied");
        }
    }

    /**
     * What happens when Giofence intent is delivered from Location API
     */
    private void addGeoFence() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "adding geofence");

            pendingIntent = createGeoFencePendingIntent();

            PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(googleClient, geoRequest, pendingIntent);
            result.setResultCallback(this);

            drawBorder();
            finishAlarm();
            saveAlarmToDB();
        }
    }

    private PendingIntent createGeoFencePendingIntent() {

        Log.d(TAG, "createGeoFencePendingIntent");

        Intent intent = new Intent(this, GeoFenceTransitionService.class);

        return PendingIntent.getService(this, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Draw a border around the target location
     */
    private void drawBorder() {

        borderCircle = mMap.addCircle(new CircleOptions()
                .center(destinationCoordinates)
                .radius(radius)
                .strokeColor(Color.GREEN)
                .fillColor(Color.BLUE));

    }

    /**
     * Handle the bottomsheet UI after alarm is set
     */
    private void finishAlarm() {

        setBtnState = 3;
        setBtn.setText("DELETE ALARM");
        setBtn.setEnabled(true);
        setBtn.setBackgroundColor(Color.parseColor("#000000"));
        setBtn.setTextColor(Color.parseColor("#ffffff"));
        mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        Log.d(TAG, "Hide autocomplete fragment");

        findViewById(R.id.place_autocomplete_fragment).setVisibility(View.GONE);
    }


    /**
     * Add alarm to the database
     */
    private void saveAlarmToDB() {

        Double l1 = destinationCoordinates.latitude;
        Double l2 = destinationCoordinates.longitude;
        String lat = l1.toString();
        String lon = l2.toString();

        LocationDetails details = new LocationDetails(destinationAddress, reqId, radius, lat, lon);

        dbLayer = new DatabaseLayer(this);

        dbLayer.insertLocationDetails(details);

    }

    @Override
    public void onResult(@NonNull Status status) {

        Log.d(TAG, "Callback heard from result");
    }

    //    Set button click listener
    @Override
    public void onClick(View v) {


        //SetLocation is clicked. Change btnstate to 2
        if (v.getId() == R.id.setBtn) {

            Log.d(TAG, "SET BUTTON TOUCHED");
            Log.d(TAG, Integer.toString(mbottomSheetBehavior.getState()));

            if (setBtnState == 1) {

                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                setBtn.setText("CONFIRM ALARM LOCATION");
                setBtnState = 2;

            }
            //Location is confirmed. Proceed to build a geofence
            else if (setBtnState == 2) {
                buildFence();
            }
            //Delete alarm
            else if (setBtnState == 3) {
                deleteAlarm();
            }
        }

        //Callback for radius selector field
        else if (v.getId() == R.id.radius_text) {
            showRadiusPicker();
        }


        //Callback for cancel_action_button
        else if (v.getId() == R.id.cancel_action_btn) {


            mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            setBtn.setText("TYPE DESTINATION LOCATION");
            setBtn.setEnabled(false);
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultCoordinates, DEFAULT_ZOOM));
            autocompleteFragment.setText("");

        }

    }

    private void buildFence(){

        Log.d(TAG, "Gonna build a fence");

        reqId = String.valueOf(System.currentTimeMillis());

        fence = new Geofence.Builder()
                .setRequestId(reqId)
                .setCircularRegion(destinationCoordinates.latitude, destinationCoordinates.longitude, radius)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(2000)
                .setExpirationDuration(1000000000)
                .build();

        geofenceList.add(fence);

        Log.d(TAG, "Gonna employ a gaurd");


        geoRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                .addGeofences(geofenceList)
                .build();

        Log.d(TAG, "Check permission to add Geofence");
        checkLocationPermission();
    }

    private void deleteAlarm(){

        Log.d(TAG,"Deleting alarm");

        //Remove geofencing
        List<String> geofences_to_remove = new ArrayList<>();
        geofences_to_remove.add(reqId);
        LocationServices.GeofencingApi.removeGeofences(googleClient,geofences_to_remove);

        //Show place autocomplete fragment
        findViewById(R.id.place_autocomplete_fragment).setVisibility(View.VISIBLE);

        //Remove border circle
        mMap.clear();

        //Remove from DB
        if(dbLayer == null){
            dbLayer = new DatabaseLayer(this);
        }
        if(dbLayer.deleteLocation(reqId)){
            Log.d(TAG,"Deleted successfully");
        }else{
            Log.d(TAG,"Err in deleting the alarm from database");
            Toast.makeText(this,"Error in deleting, Try Again",Toast.LENGTH_LONG).show();
        }

        //Change bottomsheet buttons
        setBtnState = 0;
        setBtn.setText("TYPE DESTINATION LOCATION");
        setBtn.setBackgroundColor(Color.parseColor("#FFFFFF"));
        setBtn.setTextColor(Color.parseColor("#CCCCCC"));
        mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }


    private void renderAlarmSetState(){

        Log.d(TAG,"Render alarm set state");

        mMap.addMarker(new MarkerOptions().position(destinationCoordinates)
                .title(destinationAddress)
                .draggable(true));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationCoordinates, zoomLevel));

        drawBorder();
        finishAlarm();
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();  //Connect GoogleApiClient when starting activity
    }


    @Override
    protected void onStop() {
        super.onStop();
        googleClient.disconnect();  //Disconnect GoogleApiClient when starting activity
    }
}
