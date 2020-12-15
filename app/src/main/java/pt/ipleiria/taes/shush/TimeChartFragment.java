package pt.ipleiria.taes.shush;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.tabs.TabLayout;

import pt.ipleiria.taes.shush.utils.ChartRenderer;
import pt.ipleiria.taes.shush.utils.MeasurementsLoader;

public class TimeChartFragment extends Fragment
{
    private static final String TAG = TimeChartFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_chart, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LineChart lineChart = getActivity().findViewById(R.id.time_any_chart_view);
        TabLayout tabs = getActivity().findViewById(R.id.tab_layout_time_chart);
        ChartRenderer chartRenderer = new ChartRenderer(getContext(), false);
        chartRenderer.render(tabs, lineChart);
    }
}
