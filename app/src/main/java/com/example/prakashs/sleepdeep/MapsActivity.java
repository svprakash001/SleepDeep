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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
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

//TODO : Set the map to current location

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>, NumberPicker.OnValueChangeListener, View.OnClickListener {


    private static final int PENDING_INTENT_REQUEST_CODE = 99;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 653;
    private static final int DEFAULT_ZOOM = 8;

    private PlaceAutocompleteFragment autocompleteFragment;

    private GoogleMap mMap;
    private String TAG = "Gmaps";
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

    /*Geofencing*/
    ArrayList<Geofence> geofenceList = new ArrayList<>();
    Geofence fence;
    GeofencingRequest geoRequest;
    String reqId;


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


    private void addGeofence() {

        Log.i(TAG, "addGeofence");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(googleClient, geoRequest, createGeoFencePendingIntent());

        result.setResultCallback(this);

        drawBorder();
        finishAlarm();

    }


    private PendingIntent createGeoFencePendingIntent() {

        Log.d(TAG, "createGeoFencePendingIntent");

        Intent intent = new Intent(this, GeoFenceTransitionService.class);

        return PendingIntent.getService(this, PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void drawBorder() {

        borderCircle = mMap.addCircle(new CircleOptions()
                .center(destinationCoordinates)
                .radius(radius)
                .strokeColor(Color.GREEN)
                .fillColor(Color.BLUE));

    }

    private void finishAlarm(){

        setBtnState = 3;
        setBtn.setText("DELETE ALARM");
        setBtn.setBackgroundColor(Color.parseColor("#000000"));
        setBtn.setTextColor(Color.parseColor("#ffffff"));
        mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

    }

    private void exportLocationAlarm(){


        LocationDetails details = new LocationDetails(destinationAddress,reqId);

        dbLayer = new DatabaseLayer(this);

        dbLayer.insertLocationDetails(details);

    }


    @Override
    public void onResult(@NonNull Status status) {

        Log.i(TAG, "Callback heard from result");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }


    //    Set button click listener
    @Override
    public void onClick(View v) {


        if (v.getId() == R.id.setBtn) {

            Log.d(TAG, "SET BUTTON TOUCHED");
            Log.d(TAG, Integer.toString(mbottomSheetBehavior.getState()));

            if (setBtnState == 2) {

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

                Log.i(TAG, "Gonna employ a gaurd");


                geoRequest = new GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                        .addGeofences(geofenceList)
                        .build();

                Log.i(TAG, "Gonna call addGeoFence");
                addGeofence();

            } else if (setBtnState == 1) {

                mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                setBtn.setText("CONFIRM ALARM LOCATION");
                setBtnState = 2;
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
