package com.example.prakashs.sleepdeep;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.common.zzo.getErrorString;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GeoFenceTransitionService extends IntentService {


    private String TAG = "Gmaps";
    private static final int NOTIFICATION_ID = 192;


    public GeoFenceTransitionService() {
        super("GeoFenceTransitionService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        Log.d(TAG,"SUCCESS!!");

        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        int typeOfTransition = geofencingEvent.getGeofenceTransition();

//        heck the transition type
        if (typeOfTransition == Geofence.GEOFENCE_TRANSITION_DWELL){

            //Get the geofence that were triggered
            List<Geofence> triggeredGeofences = geofencingEvent.getTriggeringGeofences();

            // TODO: 14/06/17 Get the detailsof the triggered geofence
            sendNotification("siva");

        }
    }


    private void sendNotification(String msg){

        Log.d(TAG,"sendNotification: "+msg);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle(msg)
                .setContentText("Work hard and you win");

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

//        Builds the notification and issues it
        mNotifyMgr.notify(NOTIFICATION_ID,mBuilder.build());

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        Ringtone R = RingtoneManager.getRingtone(this,notification);

        R.play();


    }

}
