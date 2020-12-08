package pt.ipleiria.taes.shush.utils;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class Measurement
{
    private double dB;
    private Date date;
    private double latitude;
    private double longitude;

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private Measurement() {}

    public Measurement(double dB, Date date, double latitude, double longitude)
    {
        this.dB = dB;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getdB() {
        return dB;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setdB(double dB) {
        this.dB = dB;
    }

    public Date getDate() {
        return date;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public JSONObject toJSONObject()
            throws JSONException
    {
        JSONObject jsonMeasurement = new JSONObject();
        jsonMeasurement.put("intensity", dB);
        jsonMeasurement.put("date", DATE_FORMAT.format(date));
        jsonMeasurement.put("lat", latitude);
        jsonMeasurement.put("lon", longitude);

        return jsonMeasurement;
    }

    public Map<String, Object> toHashMap()
    {
        Map<String, Object> hashmapMeasurement = new HashMap<>();
        hashmapMeasurement.put("intensity", dB);
        hashmapMeasurement.put("date", date);
        hashmapMeasurement.put("lat", latitude);
        hashmapMeasurement.put("lon", longitude);

        return hashmapMeasurement;
    }

    public static Measurement fromJSONObject(JSONObject jsonObject)
            throws JSONException, ParseException
    {
        Measurement measurement = new Measurement();
        measurement.date = DATE_FORMAT.parse(jsonObject.getString("date"));
        measurement.dB = jsonObject.getDouble("intensity");
        measurement.longitude = jsonObject.getDouble("lon");
        measurement.latitude = jsonObject.getDouble("lat");

        return measurement;
    }

    public static Measurement fromHashMap(Map<String, Object> hashmap)
    {
        Measurement measurement = new Measurement();
        measurement.date = (Date) hashmap.get("date");
        measurement.dB = (Double) hashmap.get("intensity");
        measurement.longitude = (Double) hashmap.get("lon");
        measurement.latitude = (Double) hashmap.get("lat");

        return measurement;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String toString() {
        return String.format("%2.0f dB", dB) + ", lat:" + latitude + ", lon:" + longitude + " (" + DATE_FORMAT.format(date) + ")";
    }
}
