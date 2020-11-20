package pt.ipleiria.taes.shush;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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

import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private RecordButtonListener listener;
    private Chronometer chronometer;
    private TextView soundDecibel;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActivityCompat.requestPermissions(getActivity(), permissions, REQUEST_AUDIO_PERMISSION);
        file = new File(getContext().getExternalCacheDir().getAbsolutePath() + "/shushaudio.3gp");
        Log.d(TAG, file.getAbsolutePath());

        recorder = getActivity().findViewById(R.id.record_toggle);
        listener = new RecordButtonListener();
        recorder.setOnClickListener(listener);

        chronometer = getActivity().findViewById(R.id.chronometer_time);
        soundDecibel = getActivity().findViewById(R.id.sound_dB);

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

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
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
        soundDecibel.post(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                double avg = mesurements.size() > 1
                    ? mesurements.stream().reduce((a, b) -> a + b).get() / mesurements.size()
                    : 0;
                soundDecibel.setText(String.format("%2.0f dB", avg));
            }
        });
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