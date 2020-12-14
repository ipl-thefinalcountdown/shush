package pt.ipleiria.taes.shush.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChartRenderer implements TabLayout.OnTabSelectedListener
{
    private static final String TAG = ChartRenderer.class.getSimpleName();

    Boolean isBarChart;
    private MeasurementsLoader measurementsLoader;
    private Chart chart;
    private TabLayout tabs;
    private Context context;

    public ChartRenderer(Context context, Boolean isBarChart)
    {
        this.context = context;
        this.isBarChart = isBarChart;
        measurementsLoader = new MeasurementsLoader(context);
    }

    public void render(TabLayout tabLayout, Chart chart)
    {
        tabs = tabLayout;
        this.chart = chart;
        tabs.addOnTabSelectedListener(this);

        onTabSelected(tabs.getTabAt(tabs.getSelectedTabPosition()));
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        if(tab.getPosition() == 0)
            loadLocalChart();
        else
            loadSharedChart();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) { }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {
        onTabSelected(tab);
    }

    public void createChart()
    {
        List<Measurement> measurements = measurementsLoader.getMeasurements();
        Log.d(TAG, measurements.toString());

        if(isBarChart)
        {
            BarChart barChart = (BarChart) chart;
            ArrayList<BarEntry> data = new ArrayList<>();
            int idx = 0;
            for(Measurement measurement : measurements)
            {
                data.add(new BarEntry((float) idx, (float) measurement.getdB()));
                idx++;
            }
            BarDataSet barDataSet = new BarDataSet(data, "Location");
            barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
            barDataSet.setValueTextColor(Color.BLACK);
            barDataSet.setValueTextSize(16f);
            barChart.setFitBars(true);
            barChart.clear();
            barChart.setData(new BarData(barDataSet));
            barChart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    // the only way to add string labels on this charts
                    int length = measurementsLoader.getMeasurements().size();
                    Measurement measurement = measurementsLoader.getMeasurements().get(Math.max(0, Math.min(Math.round(value), length - 1)));
                    return String.format("%2.2f, %2.2f", measurement.getLatitude(), measurement.getLongitude());
                }
            });

        } else {
            LineChart lineChart = (LineChart) chart;
            List<Measurement> sortedMeasurements = new ArrayList<>(measurements);

            Collections.sort(sortedMeasurements, new Comparator<Measurement>() {
                @Override
                public int compare(Measurement o1, Measurement o2) {
                    return Long.compare(o1.getDate().getTime(), o2.getDate().getTime());
                }
            });

            ArrayList<Entry> data = new ArrayList<>();
            int idx = 0;
            for(Measurement measurement : sortedMeasurements)
            {
                data.add(new Entry((float) idx, (float) measurement.getdB()));
                idx++;
            }
            LineDataSet lineDataSet = new LineDataSet(data, "Time");
            lineDataSet.setColor(Color.BLACK);
            lineDataSet.setValueTextColor(Color.BLACK);
            lineDataSet.setValueTextSize(16f);

            lineChart.clear();
            lineChart.setData(new LineData(lineDataSet));
            lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    // the only way to add string labels on this charts
                    int length = sortedMeasurements.size();
                    Measurement measurement = sortedMeasurements.get(Math.max(0, Math.min(Math.round(value), length - 1)));
                    return new SimpleDateFormat("dd/MM").format(measurement.getDate());
                }
            });
        }
    }

    void loadLocalChart()
    {
        measurementsLoader.loadLocalMeasurements();
        createChart();
    }

    void loadSharedChart()
    {
        measurementsLoader.loadSharedMeasurements().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    measurementsLoader.setMeasurements(new ArrayList<>(documents.size()));
                    for (DocumentSnapshot document : documents)
                    {
                        measurementsLoader.getMeasurements().add(Measurement.fromHashMap(document.getData()));
                    }
                    createChart();
                } else {
                    Toast.makeText(context, "Measurements can't be loaded!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
