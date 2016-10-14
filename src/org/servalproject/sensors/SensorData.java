package org.servalproject.sensors;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorData {
    public static double[] accelerometerData;
    public static double   lightData;
    public static double[] gravityData;
    public static double[] gyroData;

    public SensorData() {
        accelerometerData = new double[3];
        lightData = 0.0f;
        gravityData = new double[3];
        gyroData = new double[3];
    }

    public String toJSONString() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("aX", accelerometerData[0]);
            jo.put("aY", accelerometerData[1]);
            jo.put("aZ", accelerometerData[2]);
            jo.put("l", lightData);
            jo.put("grX", gravityData[0]);
            jo.put("grY", gravityData[1]);
            jo.put("grZ", gravityData[2]);
            jo.put("gyX", gyroData[0]);
            jo.put("gyY", gyroData[1]);
            jo.put("gyZ", gyroData[2]);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return jo.toString();
    }

    public static void fromJSONString(String jsonString) {
        try {
            JSONObject jo = new JSONObject(jsonString);
            accelerometerData[0] = jo.getDouble("aX");
            accelerometerData[1] = jo.getDouble("aY");
            accelerometerData[2] = jo.getDouble("aZ");
            lightData = jo.getDouble("l");
            gravityData[0] = jo.getDouble("grX");
            gravityData[1] = jo.getDouble("grY");
            gravityData[2] = jo.getDouble("grZ");
            gyroData[0] = jo.getDouble("gyX");
            gyroData[1] = jo.getDouble("gyY");
            gyroData[2] = jo.getDouble("gyZ");

        } catch(JSONException e){
            e.printStackTrace();
        }
    }
}