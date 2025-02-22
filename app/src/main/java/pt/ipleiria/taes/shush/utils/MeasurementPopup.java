package pt.ipleiria.taes.shush.utils;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import pt.ipleiria.taes.shush.R;

public class MeasurementPopup {

    private PopupWindow popupWindow;
    private Measurement measurement;

    public MeasurementPopup(Measurement measurement)
    {
        this.measurement = measurement;
    }

    public PopupWindow getPopupWindow() {
        return popupWindow;
    }

    public void showPopupWindow(final View view) {


        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.map_popup, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        TextView coords = popupView.findViewById(R.id.coords);
        String coordsStr = measurement.getLatitude() + " " + measurement.getLongitude();
        coords.setText(coordsStr);

        TextView date = popupView.findViewById(R.id.date);
        date.setText(Measurement.DATE_FORMAT.format(measurement.getDate()));
        TextView intensity = popupView.findViewById(R.id.intensity);
        intensity.setText(String.format("%2.0f dB", measurement.getdB()));

        //Handler for clicking on the inactive zone of the window

        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

}