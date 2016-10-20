package dk.jamiemagee.kanalvejparking;

import android.os.Handler;

import java.util.TimerTask;

import dk.jamiemagee.kanalvejparking.activities.MainActivity;

public class ParkingSpacesTimerTask extends TimerTask {

    private final Handler handler = new Handler();
    private GetParkingTask getParkingTask;
    private final MainActivity mainActivity;

    public ParkingSpacesTimerTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

    }

    public GetParkingTask getGetParkingTask() {
        return getParkingTask;
    }

    @Override
    public void run() {
        getParkingTask = new GetParkingTask(mainActivity);
        handler.post(new Runnable() {
            public void run() {
                getParkingTask.execute();
            }
        });
    }
}
