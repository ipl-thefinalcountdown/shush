package pt.ipleiria.taes.shush;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DashboardFragment extends Fragment {
    // Fragment Tag for Logging
    private static final String TAG = DashboardFragment.class.getSimpleName();

    public class RecordButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_recordFragment);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        FloatingActionButton fab = getActivity().findViewById(R.id.record_fab);
        fab.setOnClickListener(new RecordButtonListener());
    }
}