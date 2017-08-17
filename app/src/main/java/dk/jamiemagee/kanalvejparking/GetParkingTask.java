package dk.jamiemagee.kanalvejparking;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import dk.jamiemagee.kanalvejparking.activities.MainActivity;
import dk.jamiemagee.kanalvejparking.models.ParkingSpaces;

public final class GetParkingTask extends AsyncTask<Void, Void, ParkingSpaces> {

    private static final String parkingServiceUrl = "https://kanalvejparking.azurewebsites.net/api/ParkingSpaces";

    private final MainActivity mainActivity;

    private final int NOTIFICATION_ID = 1;

    private NotificationManager notificationmanager;

    public GetParkingTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    protected ParkingSpaces doInBackground(Void... voids) {
        ParkingSpaces parkingSpaces = new ParkingSpaces();
        try {
            URL url = new URL(parkingServiceUrl);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();


            try {
                parkingSpaces = readStream(urlConnection.getInputStream());
            }
            finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {

        }
        return parkingSpaces;
    }

    private ParkingSpaces readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Gson gson = new Gson();
        return gson.fromJson(response.toString(), ParkingSpaces.class);
    }


    @Override
    protected void onPreExecute() {
        setVisibility(View.GONE, View.VISIBLE);
    }

    @Override
    protected void onPostExecute(ParkingSpaces result) {
        ParkingSpaces parkingSpaces = mainActivity.getParkingSpaces();

        parkingSpaces.setEmployeeParking(result.getEmployeeParking());
        parkingSpaces.setGuestParking(result.getGuestParking());
        parkingSpaces.setPublicParking(result.getPublicParking());
        parkingSpaces.setTotalParking(result.getTotalParking());

        mainActivity.setParkingSpaces(parkingSpaces);

        SwipeRefreshLayout refreshLayout = mainActivity.getRefreshLayout();
        refreshLayout.setRefreshing(false);
        mainActivity.setRefreshLayout(refreshLayout);

        setVisibility(View.VISIBLE, View.GONE);

        sendNotification(parkingSpaces);
    }

    private void sendNotification(ParkingSpaces parkingSpaces) {
        notificationmanager = (NotificationManager) mainActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        //Build the notification using Notification.Builder
        Notification.Builder builder = new Notification.Builder(mainActivity.getApplicationContext())
                .setSmallIcon(R.drawable.ic_logo)
                .setAutoCancel(true)
                .setContentTitle("Test")
                .setContentText(String.valueOf(parkingSpaces.getEmployeeParking()));


        //Show the notification
        notificationmanager.notify(NOTIFICATION_ID, builder.build());
    }

    private void setVisibility(int textViewVisibility, int progressBarVisibility) {
        ProgressBar employeeProgressBar = (ProgressBar) mainActivity.findViewById(R.id.employee_spaces_progress_bar);
        employeeProgressBar.setVisibility(progressBarVisibility);
        TextView employeeParkingSpaces = (TextView) mainActivity.findViewById(R.id.employee_spaces_number);
        employeeParkingSpaces.setVisibility(textViewVisibility);

        ProgressBar guestProgressBar = (ProgressBar) mainActivity.findViewById(R.id.guest_spaces_progress_bar);
        guestProgressBar.setVisibility(progressBarVisibility);
        TextView guestParkingSpaces = (TextView) mainActivity.findViewById(R.id.guest_spaces_number);
        guestParkingSpaces.setVisibility(textViewVisibility);

        ProgressBar publicProgressBar = (ProgressBar) mainActivity.findViewById(R.id.public_spaces_progress_bar);
        publicProgressBar.setVisibility(progressBarVisibility);
        TextView publicParkingSpaces = (TextView) mainActivity.findViewById(R.id.public_spaces_number);
        publicParkingSpaces.setVisibility(textViewVisibility);
    }
}
