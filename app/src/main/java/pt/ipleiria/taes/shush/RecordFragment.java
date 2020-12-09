package pt.ipleiria.taes.shush;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pt.ipleiria.taes.shush.activities.MainActivity;
import pt.ipleiria.taes.shush.utils.LocalMeasurements;
import pt.ipleiria.taes.shush.utils.Locator;
import pt.ipleiria.taes.shush.utils.Measurement;
import pt.ipleiria.taes.shush.utils.SharedMeasurements;

public class RecordFragment extends Fragment {
    private static final String TAG = RecordFragment.class.getSimpleName();
    private static final int REQUEST_AUDIO_PERMISSION = 200;
    private static File file;
    private static final double REF_AMP = 10*Math.exp(-7);

    private MediaRecorder mediaRecorder;

    private boolean permissionToRecord;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private List<Double> mesurements;

    private FloatingActionButton recorder;
    private FloatingActionButton listButton;

    private RecordButtonListener listener;
    private Chronometer chronometer;
    private TextView soundDecibel;
    private Button saveButton;
    private Button shareButton;
    private LocalMeasurements localMeasurements;
    private SharedMeasurements sharedMeasurements;
    private Locator locator;
    private ProgressDialog dialog;

    private double average;

    private class SharedMeasurementListener implements OnCompleteListener<DocumentReference> {
        @Override
        public void onComplete(@NonNull Task<DocumentReference> task) {
            if(task.isSuccessful())
            {
                Toast.makeText(getContext(), "Measurement saved!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Measurement can't be saved!", Toast.LENGTH_LONG).show();
            }
            setButtonsVisibility(View.INVISIBLE);
        }
    }

    /**
     * Listener for LocationResult task
     */
    private class LocationResultListener implements OnCompleteListener<Location> {

        boolean shareMeasurement;

        public LocationResultListener(boolean share) {
            this.shareMeasurement = share;
        }

        @Override
        public void onComplete(@NonNull Task<Location> task) {
            if (task.isSuccessful()) {
                dialog.dismiss();
                Location location = locator.getLastKnownLocation();
                if(location != null) {
                    Log.d(TAG,"Location acquired");
                    Measurement measurement = new Measurement(average, new Date(), location.getLatitude(), location.getLongitude());
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.dialog_confirmation_message) + "\n\n" + measurement.toString())
                            .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.dismiss();
                                    if(shareMeasurement)
                                    {
                                        sharedMeasurements.add(measurement)
                                                .addOnCompleteListener(new SharedMeasurementListener());
                                    } else {
                                        try {
                                            localMeasurements.add(measurement);
                                        } catch(IOException | JSONException ex)
                                        {
                                            Toast.makeText(getContext(), "Can't save the measurement!", Toast.LENGTH_LONG).show();
                                        }

                                        Toast.makeText(getContext(), "Measurement saved!", Toast.LENGTH_LONG).show();
                                        setButtonsVisibility(View.INVISIBLE);
                                    }
                                }
                            })
                            .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.dismiss();
                                }
                            });
                    // Create the AlertDialog object and return it
                    builder.create().show();
                }
                else {
                    Toast.makeText(getContext(), "Can't get location!", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        }
    }

    private class MeasurementsButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Navigation.findNavController(v).navigate(R.id.action_recordFragment_to_measurementFragment);
        }
    }

    private class SaveButtonListener implements View.OnClickListener {
        boolean shareMeasurement;

        public SaveButtonListener(boolean share)
        {
            this.shareMeasurement = share;
        }

        @Override
        public void onClick(View view) {
            if(localMeasurements != null)
            {
                dialog = ProgressDialog.show(getActivity(), "",
                        "Getting device location...", true);
                locator.getLocationPermission();
                locator.getDeviceLocation(new LocationResultListener(shareMeasurement));
            }
        }
    }

    public RecordFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).getFab().setVisibility(View.INVISIBLE);

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_AUDIO_PERMISSION);
        file = new File(getContext().getExternalCacheDir().getAbsolutePath() + "/shushaudio.3gp");
        Log.d(TAG, file.getAbsolutePath());

        recorder = getActivity().findViewById(R.id.record_toggle);
        listener = new RecordButtonListener();
        recorder.setOnClickListener(listener);

        chronometer = getActivity().findViewById(R.id.chronometer_time);
        soundDecibel = getActivity().findViewById(R.id.sound_dB);
        saveButton = getActivity().findViewById(R.id.save_btn);
        saveButton.setOnClickListener(new SaveButtonListener(false));
        shareButton = getActivity().findViewById(R.id.share_btn);
        shareButton.setOnClickListener(new SaveButtonListener(true));
        setButtonsVisibility(View.INVISIBLE);

        try {
            localMeasurements = new LocalMeasurements(getContext());
        } catch(IOException | JSONException ex)
        {
            Toast.makeText(getContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }
        sharedMeasurements = new SharedMeasurements();

        locator = new Locator(getActivity());
        locator.getLocationPermission();

        listButton = getActivity().findViewById(R.id.measurement_list_btn);
        listButton.setOnClickListener(new MeasurementsButtonListener());

        mesurements = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSION:
                permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }

    private void setButtonsVisibility(int visibility)
    {
        saveButton.setVisibility(visibility);
        shareButton.setVisibility(visibility);
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        locator.getDeviceLocation();
        saveButton.post(new Runnable() {
            @Override
            public void run() {
                setButtonsVisibility(View.INVISIBLE);
            }
        });
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mediaRecorder.setAudioSource(
                audioManager.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) != null
                        ? MediaRecorder.AudioSource.UNPROCESSED
                        : MediaRecorder.AudioSource.VOICE_RECOGNITION
        );
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(file.getAbsolutePath());
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed!");
        }

        mediaRecorder.start();
        recorder.setImageResource(R.drawable.ic_baseline_stop_24);
        chronometer.setFormat("%s");
        Log.d(TAG, String.valueOf(chronometer.getBase()));
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        Log.d(TAG, "Recording started!");
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
        } catch(RuntimeException e) {
            file.delete();
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        recorder.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
        chronometer.stop();
        average = mesurements.size() > 1
                ? mesurements.stream().reduce((a, b) -> a + b).get() / mesurements.size()
                : 0;
        soundDecibel.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                soundDecibel.setText(String.format("%2.0f dB", average));
            }
        });

        if(mesurements.size() > 1)
        {
            mesurements.clear();
            saveButton.post(new Runnable() {
                @Override
                public void run() {
                    setButtonsVisibility(View.VISIBLE);
                }
            });
        }
        Log.d(TAG, "Recording stopped!");
    }


    private double getAmplitude() {
        return mediaRecorder == null
                ? 0
                : mediaRecorder.getMaxAmplitude();
    }


    private double getSoundDecibel() {
        return 20 * Math.log10(getAmplitude() / REF_AMP);
    }


    private void updateDecibelCounter() {
        soundDecibel.post(new Runnable() {
            public void run() {
                double sdb = getSoundDecibel();
                if (!Double.isInfinite(sdb)) {
                    soundDecibel.setText(String.format("%2.0f db", sdb));
                    mesurements.add(sdb);
                }
            }
        });
    }


    private void getPermission() {
        if (ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(),
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            permissionToRecord = true;
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_PERMISSION);
        }
    }


    class RecordButtonListener implements View.OnClickListener {
        private boolean record = false;
        private Timer timer;
        private Timer handler;

        @Override
        public void onClick(View v) {
            getPermission();
            if (permissionToRecord)
                toggle();
        }

        private void toggle()
        {
            record = !record;
            onRecord(record);

            if (timer != null) {
                handler.cancel();
                handler.purge();
                timer.cancel();
                timer = null;
                handler = null;
                Log.d(TAG, "Timer finished!");
            } else {
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d(TAG, "TimerTask started ...");
                        toggle();
                    }
                }, 30 * 1000 + 250);

                handler = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateDecibelCounter();
                    }
                }, 250, 100);
            }
        }
    }
}