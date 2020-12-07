package pt.ipleiria.taes.shush.utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class SharedMeasurements {
    private static final String TAG = SharedMeasurements.class.getSimpleName();
    FirebaseFirestore db;

    public SharedMeasurements()
    {
        this.db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentReference> add(Measurement measurement)
    {
        return db.collection("records")
                .add(measurement.toHashMap());
    }

    public Task<QuerySnapshot> getMeasurements()
    {
        return db.collection("records")
                .get();
    }
}
