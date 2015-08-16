package smartfm.yuiwei.energyapp;
import java.util.ArrayList;
import java.util.Arrays;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.location.Location;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.net.Uri;
import android.util.Log;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends ActionBarActivity
        implements ConnectionCallbacks, OnConnectionFailedListener,
                    TemperatureFragment.OnFragmentInteractionListener, EmissionsFragment.OnFragmentInteractionListener {
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected String mLatitudeText;
    protected String mLongitudeText;

    SocketService mSocketService;
    boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new TemperatureFragment()).commit();
        }

    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(forecastReceiver,
                new IntentFilter("forecast-data"));
        LocalBroadcastManager.getInstance(this).registerReceiver(emissionsReceiver,
                new IntentFilter("emissions-data"));
    }
    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this).unregisterReceiver(forecastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(emissionsReceiver);
        super.onPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
       // mSocketService.unbindService(mServiceConnection);
      //  mSocket.off("new message", );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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



    public void onTempFragmentInteraction(Uri uri) {}
    public void onEmissionsFragmentInteraction(Uri uri) {}

    //Requests forecast data from the server
    public void request(View view) {
        mSocketService.requestForecast(mLatitudeText, mLongitudeText);
    }

    private BroadcastReceiver forecastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            Bundle forecastData = new Bundle();

            try {
                float latitude = intent.getFloatExtra("latitude", -1);
                float longitude = intent.getFloatExtra("longitude", -1);
                double[] dataArr = intent.getDoubleArrayExtra("temperatureArr");
                int[] timeArr = intent.getIntArrayExtra("timeArr");

                int numData = intent.getIntExtra("numData", 0);
                String date = intent.getStringExtra("date");
                forecastData.putFloat("latitude", Float.valueOf(latitude));
                forecastData.putFloat("longitude", Float.valueOf(longitude));
                forecastData.putDoubleArray("temperatureArr", dataArr);
                forecastData.putIntArray("timeArr", timeArr);
                forecastData.putInt("numData", numData);
                TemperatureFragment fragobj = new TemperatureFragment();
                fragobj.setArguments(forecastData);
                getFragmentManager().beginTransaction().replace(R.id.container, fragobj).commit();

                TextView tv1 = (TextView) findViewById(R.id.tv1);
                tv1.setText(date);

                String[] dateArr = date.split("/");
                int month = Integer.parseInt(dateArr[0]);
                int day = Integer.parseInt(dateArr[1]);
                int year = Integer.parseInt(dateArr[2]);
            } catch (Exception e) { return; }
            Log.d("receiver", "Got message: " );
        }
    };





    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("VALUES", "connected");
        if (mLastLocation != null) {
            mLatitudeText = (String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText = (String.valueOf(mLastLocation.getLongitude()));
            Log.d("VALUES", mLatitudeText);
            Log.d("VALUES", mLongitudeText);
        } else {
            //Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.d("VALUES", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.d("VALUES", "Connection suspended");
        mGoogleApiClient.connect();
    }

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


    public void findSensor(View view) {
        Intent myIntent = new Intent(MainActivity.this, ConnectSensorActivity.class);
        myIntent.putExtra("key", ""); //Optional parameters
        MainActivity.this.startActivity(myIntent);
    }

    public void viewEmissions(View view) {
        mSocketService.requestEmissions(Math.floor(Math.random()*271));
        /*
        Intent myIntent = new Intent(MainActivity.this, EmissionsActivity.class);
        myIntent.putExtra("key", ""); //Optional parameters
        MainActivity.this.startActivity(myIntent);*/
    }
    private BroadcastReceiver emissionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            Bundle emissions = new Bundle();

            try {
                emissions = intent.getBundleExtra("emissionsData");
                ArrayList<EmissionDataPoint> edps = emissions.getParcelableArrayList("emissionsData");
                String user = edps.get(0).userID;
                TextView tv1 = (TextView) findViewById(R.id.tv1);
                tv1.setText("Viewing data for user " + user);
                EmissionsFragment fragobj = new EmissionsFragment();
                fragobj.setArguments(emissions);
                getFragmentManager().beginTransaction().replace(R.id.container, fragobj).commit();
            } catch (Exception e) { Log.d("ERROR", "receiver"); }
        }
    };




}


/*
//Bundle forecastData = new Bundle();

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
            forecastData.putFloat("latitude", Float.valueOf(latitude));
            forecastData.putFloat("longitude", Float.valueOf(longitude));
            forecastData.putDoubleArray("temperatureArr", dataArr);
            forecastData.putIntArray("timeArr", timeArr);
            TemperatureFragment fragobj = new TemperatureFragment();
            fragobj.setArguments(forecastData);
            getFragmentManager().beginTransaction().replace(R.id.container, fragobj).commit();

            TextView tv1 = (TextView) findViewById(R.id.tv1);
            tv1.setText(date);
        }
 */