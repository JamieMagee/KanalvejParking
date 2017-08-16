package dk.jamiemagee.kanalvejparking.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Timer;

import dk.jamiemagee.kanalvejparking.GeofenceTransitionsIntentService;
import dk.jamiemagee.kanalvejparking.GetParkingTask;
import dk.jamiemagee.kanalvejparking.ParkingSpacesTimerTask;
import dk.jamiemagee.kanalvejparking.R;
import dk.jamiemagee.kanalvejparking.databinding.ActivityMainBinding;
import dk.jamiemagee.kanalvejparking.models.ParkingSpaces;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    protected GoogleApiClient mGoogleApiClient;
    protected Geofence geofence;
    SwipeRefreshLayout refreshLayout;
    ParkingSpaces parkingSpaces = new ParkingSpaces();
    Timer timer = new Timer();
    ParkingSpacesTimerTask parkingSpacesTimerTask = new ParkingSpacesTimerTask(this);
    int delay;
    private PendingIntent geofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setParkingSpaces(parkingSpaces);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);

        new GetParkingTask(this).execute();

        geofence = null;
        geofencePendingIntent = null;
        generateGeofence();
        buildGoogleApiClient();

        setTimer();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main);
        refreshLayout.setOnRefreshListener(this);
    }

    private void setTimer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        delay = Integer.parseInt(prefs.getString("sync_frequency", "0"));
        if (delay > 0 && parkingSpacesTimerTask == null) {
            try {
                parkingSpacesTimerTask = new ParkingSpacesTimerTask(this);
                timer.schedule(parkingSpacesTimerTask, 0, delay * 1000);
            } catch (IllegalStateException e) {
                System.out.print(e);
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        parkingSpacesTimerTask.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        setTimer();
        super.onResume();
    }

    @Override
    public void onRefresh() {
        new GetParkingTask(this).execute();
    }

    public ParkingSpaces getParkingSpaces() {
        return parkingSpaces;
    }

    public void setParkingSpaces(ParkingSpaces parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
    }

    public SwipeRefreshLayout getRefreshLayout() {
        return refreshLayout;
    }

    public void setRefreshLayout(SwipeRefreshLayout refreshLayout) {
        this.refreshLayout = refreshLayout;
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void generateGeofence() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Integer distance = 1000; //Integer.parseInt(prefs.getString("distance", "1000"));

        geofence = new Geofence.Builder()
                .setRequestId("MSFT")
                .setCircularRegion(
                        55.7731139,
                        12.5086641,
                        distance
                )
                .setExpirationDuration(12 * 60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofence(geofence);

        // Return a GeofencingRequest.
        return builder.build();
    }

    private void addGeoFences(){
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            //logSecurityException(securityException);
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        addGeoFences();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Toast.makeText(
                    this,
                    "Added geofence",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
