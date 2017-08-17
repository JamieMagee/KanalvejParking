package dk.jamiemagee.kanalvejparking.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;

import dk.jamiemagee.kanalvejparking.GetParkingTask;
import dk.jamiemagee.kanalvejparking.ParkingSpacesTimerTask;
import dk.jamiemagee.kanalvejparking.R;
import dk.jamiemagee.kanalvejparking.databinding.ActivityMainBinding;
import dk.jamiemagee.kanalvejparking.models.ParkingSpaces;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout refreshLayout;
    ParkingSpaces parkingSpaces = new ParkingSpaces();
    final Timer timer = new Timer();
    ParkingSpacesTimerTask parkingSpacesTimerTask = new ParkingSpacesTimerTask(this);
    int delay;



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

        setTimer();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main);
        refreshLayout.setOnRefreshListener(this);
    }

    private void setTimer() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        delay = Integer.parseInt(prefs.getString("sync_frequency", ""));
        if (delay > 0 && parkingSpacesTimerTask == null) {
            try {
                parkingSpacesTimerTask = new ParkingSpacesTimerTask(this);
                timer.schedule(parkingSpacesTimerTask, 0, delay * 1000);
            } catch (IllegalStateException e) {
                System.out.print(e);
            }
        }
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
    protected void onPause() {
        parkingSpacesTimerTask.cancel();
        parkingSpacesTimerTask = null;
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
}
