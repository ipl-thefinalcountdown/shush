package pt.ipleiria.taes.shush;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import pt.ipleiria.taes.shush.utils.Measurement;

public class MeasurementRecyclerViewAdapter extends RecyclerView.Adapter<MeasurementRecyclerViewAdapter.ViewHolder> {

    private final List<Measurement> measurements;

    public MeasurementRecyclerViewAdapter(List<Measurement> items) {
        measurements = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_measurement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = measurements.get(position);
        String coordStr = measurements.get(position).getLatitude() + " " + measurements.get(position).getLongitude();
        holder.coords.setText(coordStr);
        holder.intensity.setText(String.format("%2.0f dB", measurements.get(position).getdB()));
        holder.date.setText(Measurement.DATE_FORMAT.format(measurements.get(position).getDate()));
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView coords;
        public final TextView intensity;
        public final TextView date;

        public Measurement item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            coords = (TextView) view.findViewById(R.id.coords);
            intensity = (TextView) view.findViewById(R.id.intensity);
            date = (TextView) view.findViewById(R.id.date);
        }
    }
}