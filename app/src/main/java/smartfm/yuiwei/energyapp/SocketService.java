package smartfm.yuiwei.energyapp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

public class SocketService extends Service {


    private static String LOG_TAG = "HELP";

    public Socket mSocket;

    private IBinder mBinder = new MyBinder();

//    private static final SocketService instance = new SocketService();
//    public static SocketService getInstance() { return instance; }

    public SocketService() {}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "in onCreate");
        try {
            mSocket = IO.socket("http://10.0.98.176:3000");
        } catch (URISyntaxException e) {}
        mSocket.connect();

        String forecast_data = getResources().getString(R.string.forecast_data);
        String emissions_data = getResources().getString(R.string.emissions_data);
        mSocket.on(forecast_data, onForecastMsg);
        mSocket.on(emissions_data, onEmissionsMsg);

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "in onDestroy");
        mSocket.disconnect();
       // mChronometer.stop();
    }


    public class MyBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }



    //Requests forecast data from the server
    public void requestForecast(String lat, String lon) {
        mSocket.emit("forecast", lat + "_" + lon);
    }
    Emitter.Listener onForecastMsg= new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            JSONArray forecastArr;
            String date;
            String[] dateArr;
            int month;
            int day;
            int year;
            float latitude;
            float longitude;
            double[] dataArr;
            int[] timeArr;

            Intent forecastData = new Intent("forecast-data");
            //Bundle forecastData = new Bundle();

            try {
                forecastArr = data.getJSONArray("forecast");
                date = data.getString("date");
                latitude = Float.parseFloat(data.getString("latitude"));
                longitude = Float.parseFloat(data.getString("longitude"));
                forecastData.putExtra("date", date);

                dataArr = new double[forecastArr.length()];
                timeArr = new int[forecastArr.length()];
                forecastData.putExtra("numData", forecastArr.length());

                for (int i = 0; i < forecastArr.length(); i++) {
                    JSONObject hourlyData = forecastArr.getJSONObject(i);
                    long time = hourlyData.getLong("time") * 1000;
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(time);
                    int hours = cal.get(Calendar.HOUR_OF_DAY);
                    int mins = cal.get(Calendar.MINUTE);
                    int timeVal = hours * 100 + mins;
                    double temperature = hourlyData.getDouble("temperature");
                    timeArr[i] = timeVal;
                    dataArr[i] = temperature;
                }
            } catch (JSONException e) {
                return;
            }

            // add the message to view
            // addMessage(username, message);
            forecastData.putExtra("latitude", Float.valueOf(latitude));
            forecastData.putExtra("longitude", Float.valueOf(longitude));
            forecastData.putExtra("temperatureArr", dataArr);
            forecastData.putExtra("timeArr", timeArr);
            sendMessage(forecastData);
        }
    };



    public void requestEmissions() {
        mSocket.emit("emissions", "nothing for now");
        Log.d("JSON0", "requested");
    }
    Emitter.Listener onEmissionsMsg= new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONArray data = (JSONArray) args[0];
            ArrayList<EmissionDataPoint> dataPoints = new ArrayList<>();
            boolean flag = false;
            for (int i=0; i<data.length(); i++) {
                try {
                    JSONObject obj = data.getJSONObject(i);
                    if (obj.getString("userID").equals("472") ) {

                        //&& obj.getInt("dateMonth")==4

                        EmissionDataPoint edp = new EmissionDataPoint();
                        edp.userID = obj.getString("userID");
                        //edp.dayNew = (obj.getInt("dayNew") == 1) ?  true : false;     //1 = first stop of current day
                        edp.dayNew = obj.getInt("dayNew");     //1 = first stop of current day
                        edp.dayNum = obj.getInt("dayNum");            //1 = Sunday; 2 = Monday; etc. This dataset only contains weekdays, for now.
                        edp.dayStart = obj.getInt("dayStart");        //time of day (in minutes after midnight) of first stop activity, if user started FMS after midnight
                        edp.dateMonth = obj.getInt("dateMonth");        //month of 2013
                        edp.dateDay = obj.getInt("dateDay");

                        //STOP/ACTIVITY
                        String stopid = obj.getString("stopID");
                        int stopType = obj.getInt("activityCode");
                        float lat = Float.parseFloat(obj.getString("stopLat"));
                        float lon = Float.parseFloat(obj.getString("stopLon"));
                        float stopDur = Float.parseFloat(obj.getString("stopDuration"));
                        float startTime = Float.parseFloat(obj.getString("activityStart"));
                        float stopTime = Float.parseFloat(obj.getString("activityStop"));
                        float sen = Float.parseFloat(obj.getString("activityEn"));
                        float sem = Float.parseFloat(obj.getString("activityEm"));
                        edp.stop = new Stop(stopid, stopType, lat, lon, stopDur, startTime, stopTime, sen, sem);

                        //TRAVEL
                        float dist = Float.parseFloat(obj.getString("travelDistance"));
                        float travDur = Float.parseFloat(obj.getString("travelDuration"));
                        float start = Float.parseFloat(obj.getString("travelStart"));
                        float ten = Float.parseFloat(obj.getString("travelEn"));
                        float tem = Float.parseFloat(obj.getString("travelEm"));
                        int travelType = obj.getInt("modeCode");
                        edp.travel = new Travel(dist, travDur, start, ten, tem, travelType);

                        Log.d("JSON1", edp.toString());
                        dataPoints.add(edp);
                    }

                } catch (Exception e) { Log.d("JSON99", "EXCEPTION????"); }
            }
            Bundle emissionsData = new Bundle();
            emissionsData.putParcelableArrayList("emissionsData", dataPoints);
            Intent intent = new Intent("emissions-data");
            intent.putExtra("emissionsData", emissionsData);
            sendMessage(intent);


        }
    };
/*
userID: 1714,
    stopID: 18642687,
    travelMode: 'Foot',
    stopType: 'Personal Errand/Task',
    travelDistance: 3791.012749,
    travelDuration: 34.16666667,
    stopLat: 1.303986187,
    stopLon: 103.8350986,
    stopDuration: 89.65,
    dayNew: 0,
    dayNum: 4,
    dayStart: 0,
    dateMonth: 9,
    dateDay: 18,
    activityCode: 6,
    modeCode: 26,
    travelEn: 0.439757479,
    travelEm: 0.051178672,
    activityEn: 3.586,
    activityEm: 0.421907674,
    travelStart: 1000.7834,
    activityStart: 1034.950067,
    activityStop: 1124.600067
 */


    //helper

    private void sendMessage(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
