package pt.ipleiria.taes.shush.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class MeasurementsLoader
{
    private LocalMeasurements localMeasurements;
    private SharedMeasurements sharedMeasurements;
    private List<Measurement> measurements;
    private Context context;

    public MeasurementsLoader(Context context)
    {
        this.context = context;
    }

    public void loadLocalMeasurements()
    {
        try {
            if(localMeasurements == null)
                localMeasurements = new LocalMeasurements(context);

            measurements = localMeasurements.getMeasurements();
        } catch(IOException | JSONException | ParseException ex)
        {
            Toast.makeText(context, "Can't load measurements!", Toast.LENGTH_LONG).show();
        }
    }

    public Task<QuerySnapshot> loadSharedMeasurements()
    {
        if(sharedMeasurements == null)
            sharedMeasurements = new SharedMeasurements();

        return sharedMeasurements.getMeasurements();
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public LocalMeasurements getLocalMeasurements() {
        return localMeasurements;
    }

    public SharedMeasurements getSharedMeasurements() {
        return sharedMeasurements;
    }
}
