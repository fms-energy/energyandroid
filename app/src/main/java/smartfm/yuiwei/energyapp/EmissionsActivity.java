package smartfm.yuiwei.energyapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class EmissionsActivity extends ActionBarActivity implements EmissionsFragment.OnFragmentInteractionListener {

    SocketService mSocketService;
    Socket mSocket;
    boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emissions);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.emissions_info, new EmissionsFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_emissions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        String forecast_data = getResources().getString(R.string.forecast_data);
        String emissions_data = getResources().getString(R.string.emissions_data);
        //mSocketService.mSocket.on(forecast_data, onEmissionsMsg);
    }

    public void onEmissionsFragmentInteraction(Uri uri) {

    }

    Emitter.Listener onEmissionsMsg = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray forecastArr;
                    String date; String[] dateArr;
                    int month; int day; int year;
                    float latitude; float longitude;
                    double[] dataArr;  int[] timeArr;

                    Bundle forecastData = new Bundle();


                    try {
                        forecastArr = data.getJSONArray("forecast");
                        date = data.getString("date");
                        dateArr = date.split("/");
                        month = Integer.parseInt(dateArr[0]);
                        day = Integer.parseInt(dateArr[1]);
                        year = Integer.parseInt(dateArr[2]);
                        latitude = Float.parseFloat(data.getString("latitude"));
                        longitude = Float.parseFloat(data.getString("longitude"));


                        dataArr = new double[forecastArr.length()];
                        timeArr = new int[forecastArr.length()];
                        forecastData.putInt("numData", forecastArr.length());

                        for (int i = 0; i < forecastArr.length(); i++) {
                            JSONObject hourlyData = forecastArr.getJSONObject(i);
                            long time = hourlyData.getLong("time")*1000;
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(time);
                            int hours = cal.get(Calendar.HOUR_OF_DAY);
                            int mins = cal.get(Calendar.MINUTE);
                            int timeVal = hours*100 + mins;
                            double temperature = hourlyData.getDouble("temperature");
                            timeArr[i] = timeVal;
                            dataArr[i] = temperature;
                        }
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    // addMessage(username, message);
                    forecastData.putFloat("latitude", Float.valueOf(latitude));
                    forecastData.putFloat("longitude", Float.valueOf(longitude));
                    forecastData.putDoubleArray("temperatureArr", dataArr);
                    forecastData.putIntArray("timeArr", timeArr);
                    TemperatureFragment fragobj = new TemperatureFragment();
                    fragobj.setArguments(forecastData);
                    getFragmentManager().beginTransaction().replace(R.id.container, fragobj).commit();

                    TextView tv1 = (TextView)findViewById(R.id.tv1);
                    tv1.setText(date);
                }
            });

        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.MyBinder myBinder = (SocketService.MyBinder) service;
            mSocketService = myBinder.getService();
            mServiceBound = true;
        }
    };


}

