package pt.ipleiria.taes.shush.utils;

import android.content.Context;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class LocalMeasurements {
    JSONArray measurements;
    File file;

    public LocalMeasurements(Context context)
            throws IOException, JSONException
    {
        file = new File(context.getExternalFilesDir(null).getAbsolutePath() + "/local_measurements.json");

        if(!file.exists())
        {
            measurements = new JSONArray();
            writeFile("[]");
        } else {
            readFile();
        }
    }

    private void readFile()
            throws IOException, JSONException
    {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        fileReader.close();
        measurements = new JSONArray(stringBuilder.toString());
    }

    private void writeFile(String jsonStr)
            throws IOException
    {
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(jsonStr);
        bufferedWriter.close();
        fileWriter.close();
    }

    private void persist()
            throws IOException
    {
        writeFile(measurements.toString());
    }

    public void add(Measurement measurement)
            throws IOException, JSONException
    {
        measurements.put(measurement.toJSONObject());
        persist();
    }

    public List<Measurement> getMeasurements()
            throws JSONException, ParseException
    {
        List<Measurement> measurementList = new ArrayList<>(measurements.length());
        for(int i = 0; i < measurements.length(); i++)
        {
            Measurement m = Measurement.fromJSONObject(measurements.getJSONObject(i));
            measurementList.add(m);
        }

        return measurementList;
    }
}
