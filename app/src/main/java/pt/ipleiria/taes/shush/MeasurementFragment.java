package pt.ipleiria.taes.shush;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import pt.ipleiria.taes.shush.activities.MainActivity;
import pt.ipleiria.taes.shush.utils.LocalMeasurements;
import pt.ipleiria.taes.shush.utils.Locator;

/**
 * A fragment representing a list of Items.
 */
public class MeasurementFragment extends Fragment {
    private static final String TAG = MeasurementFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private LocalMeasurements localMeasurements;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MeasurementFragment() { }

    @SuppressWarnings("unused")
    public static MeasurementFragment newInstance(int columnCount) {
        MeasurementFragment fragment = new MeasurementFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_measurement_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setHasFixedSize(true);
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL));

            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            try {
                if(localMeasurements == null)
                {
                    localMeasurements = new LocalMeasurements(getContext());
                    recyclerView.setAdapter(new MeasurementRecyclerViewAdapter(localMeasurements.getMeasurements()));
                }
            } catch (IOException | JSONException | ParseException ex)
            {
                Toast.makeText(getContext(), "Can't load measurements!", Toast.LENGTH_LONG).show();
                Log.d(TAG, ex.getMessage());
            }
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((MainActivity) getActivity()).getFab().setVisibility(View.VISIBLE);
    }
}