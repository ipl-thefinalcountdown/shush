package pt.ipleiria.taes.shush;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pt.ipleiria.taes.shush.activities.MainActivity;
import pt.ipleiria.taes.shush.utils.Locator;
import pt.ipleiria.taes.shush.utils.Measurement;
import pt.ipleiria.taes.shush.utils.SharedMeasurements;

public class NotificationService extends Service {
    public static final String TAG = NotificationService.class.getSimpleName();
    public static final String CHANNEL_ID = TAG;
    public static final String ALERT_CHANNEL_ID = "10001";
    private final static String DEFAULT_CHANNEL_ID = "default";
    public static final int POLLING_TIME_SECS = 5;
    public static final float TRIGGER_DISTANCE = 1000;
    public static final float TRIGGER_DECIBEL = 100;

    // Using a custom BackgroundLocator
    private FusedLocationProviderClient fusedLocationProviderClient;

    private List<String> previouslyTriggered;

    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        createNotificationChannel();
        createAlertNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Locator")
                .setContentText("SHuSH Locator is running...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        previouslyTriggered = new ArrayList<>();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permission == PackageManager.PERMISSION_GRANTED)
            requestLocationUpdates();

        return START_NOT_STICKY;
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates()
    {
        LocationRequest request = new LocationRequest();
        request.setInterval(POLLING_TIME_SECS * 1000);
        request.setFastestInterval(POLLING_TIME_SECS * 1000 / 2);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(request, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if(location != null)
                {
                    Log.d(TAG, "Location " + location.toString());
                    runTask(location);
                }
            }
        }, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    void runTask(Location location)
    {
        SharedMeasurements sharedMeasurements = new SharedMeasurements();
        sharedMeasurements.getMeasurements().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                for (DocumentSnapshot document : documents)
                {
                    Measurement measurement = Measurement.fromHashMap(document.getData());
                    Location measurementLocation = new Location("");
                    measurementLocation.setLatitude(measurement.getLatitude());
                    measurementLocation.setLongitude(measurement.getLongitude());
                    float distance = location.distanceTo(measurementLocation);

                    String measurementToken = measurement.toString();
                    if(distance <= TRIGGER_DISTANCE
                            && measurement.getdB() >= TRIGGER_DECIBEL
                            && !previouslyTriggered.contains(measurementToken)) {
                        previouslyTriggered.add(measurementToken);
                        createNotification(measurement);
                    }
                }
            }
        });
    }

    private void createNotification (Measurement measurement) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, ALERT_CHANNEL_ID);
        mBuilder.setContentTitle("SHuSH Alert");
        String contentText = "You are near a noisy area.";
        mBuilder.setContentText(contentText);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(measurement.toString())
                .setBigContentTitle(contentText));
        mBuilder.setSmallIcon(R.drawable.ic_stat_name);
        mBuilder.setAutoCancel(true);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void createAlertNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    ALERT_CHANNEL_ID,
                    "Noise Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}