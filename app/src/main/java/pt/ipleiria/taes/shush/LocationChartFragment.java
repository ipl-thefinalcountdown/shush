package pt.ipleiria.taes.shush;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.google.android.material.tabs.TabLayout;

import pt.ipleiria.taes.shush.utils.ChartRenderer;

public class LocationChartFragment extends Fragment
{
    private static final String TAG = LocationChartFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_location_chart, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        BarChart barChart = getActivity().findViewById(R.id.location_any_chart_view);
        TabLayout tabs = getActivity().findViewById(R.id.tab_layout_location_chart);
        ChartRenderer chartRenderer = new ChartRenderer(getContext(), true);
        chartRenderer.render(tabs, barChart);

    }
}
