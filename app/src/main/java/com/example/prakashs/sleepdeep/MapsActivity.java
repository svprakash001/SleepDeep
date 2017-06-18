package com.example.prakashs.sleepdeep;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.Geofence;

import java.util.ArrayList;

//TODO : Set the map to current location

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.OnConnectionFailedListener,GoogleApiClient.ConnectionCallbacks,ResultCallback<Status>{

    private static final int RADIUS = 1000;
    private static final int PENDING_INTENT_REQUEST_CODE = 99;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 653;

    private GoogleMap mMap;
    private String TAG = "Gmaps";
    private  LatLng destinationCoordinates;
    String destinationAddress;
    private int zoomLevel = 16;

    Button setBtn;
    Button okButton;

    private BottomSheetBehavior mbottomSheetBehavior;

    /*Geofencing*/
    ArrayList<Geofence> geofenceList = new ArrayList<>();
    Geofence fence;
    GeofencingRequest geoRequest;


    /*Google API client*/
    GoogleApiClient googleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
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

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationCoordinates,zoomLevel));
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

        okButton = (Button) findViewById(R.id.ok_btn);


        //Build google API client. This is required for Geofencing
        createGoogleApi();


        setBtn.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Log.i(TAG,"SET BUTTON TOUCHED");
                        Log.i(TAG,Integer.toString(mbottomSheetBehavior.getState()));

                        if(mbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){

                            Log.i(TAG,"entering collapsing process");

                            mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        }
                        else {
                            mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                            Log.i(TAG,"entering expanding process");

                        }
                    }
                }
        );

        okButton.setOnClickListener(

                new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {

                        Log.i(TAG,"OK BTN CLICKED");

                        if (mbottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){


                            mbottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                            Log.i(TAG,"Gonna build a fence");

                            fence = new Geofence.Builder()
                                    .setRequestId("KARUR")
                                    .setCircularRegion(destinationCoordinates.latitude,destinationCoordinates.longitude,RADIUS)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER)
                                    .setLoiteringDelay(2000)
                                    .setExpirationDuration(1000000000)
                                    .build();

                            geofenceList.add(fence);

                            Log.i(TAG,"Gonna employ a gaurd");


                            geoRequest = new GeofencingRequest.Builder()
                                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL)
                                    .addGeofences(geofenceList)
                                    .build();

                            Log.i(TAG,"Gonna call addGeoFence");
                            addGeofence();

                        }

                    }
                }
        );
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
    }


    private void createGoogleApi(){
        Log.i(TAG,"Creating googleee client");
        googleClient = new  GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.w(TAG,"onConnectionFailed");
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.i(TAG,"onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

        Log.i(TAG,"onConnectionSuspended");

    }


    private void addGeofence(){

        Log.i(TAG,"addGeofence");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
        }

        PendingResult<Status>  result = LocationServices.GeofencingApi.addGeofences(googleClient,geoRequest,createGeoFencePendingIntent());

        result.setResultCallback(this);
    }


    private PendingIntent createGeoFencePendingIntent(){

        Log.d(TAG,"createGeoFencePendingIntent");

        Intent intent = new Intent(this,GeoFenceTransitionService.class);

        return PendingIntent.getService(this,PENDING_INTENT_REQUEST_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }



    @Override
    public void onResult(@NonNull Status status) {

        Log.i(TAG,"Callback heard from result");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

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
