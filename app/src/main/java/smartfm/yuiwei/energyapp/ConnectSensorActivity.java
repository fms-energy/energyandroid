package smartfm.yuiwei.energyapp;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import static java.lang.Math.pow;
import java.util.List;


public class ConnectSensorActivity extends ListActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    ArrayAdapter<String> adapter;
    ArrayList<String> addrList;
    ArrayList<BluetoothDevice> deviceList;
    BluetoothManager bluetoothManager;
    BluetoothGattCharacteristic dataC;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ListView lv = (ListView) findViewById(android.R.id.list);
        setContentView(R.layout.activity_connect_sensor);
        addrList = new ArrayList<String>();
        deviceList = new ArrayList<BluetoothDevice>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addrList);
        setListAdapter(adapter);

        mHandler = new Handler();
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        bluetoothManager  =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        //super.onListItemClick(l, v, position, id);
        String selection = l.getItemAtPosition(position).toString();
        int index = addrList.indexOf(selection);
        Toast.makeText(this, selection, Toast.LENGTH_LONG).show();
        connectToDevice(deviceList.get(index));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startScan(View view) {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        final TextView tv2 = (TextView)findViewById(R.id.tv2);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        tv2.setText("not scanning");
                    } else {
                        // mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                addrList.clear(); adapter.notifyDataSetChanged();
                deviceList.clear();
                tv2.setText("scanning");
            } else {
                //mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                tv2.setText("not scanning");
            } else {
                //mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!addrList.contains(device.getAddress())) {
                                addrList.add(device.getAddress());
                                deviceList.add(device);
                            }
                            adapter.notifyDataSetChanged();
//                            Log.i("onLeScan", device.toString());

                        }
                    });
                }
            };

    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mBluetoothAdapter.cancelDiscovery();
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);
            // will stop after first device detection
        }
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        final TextView tv3 = (TextView)findViewById(R.id.tv3);

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    tv3.setText("connected");

                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    tv3.setText("disconnected");

                    scanLeDevice(true);
                    break;
                default:
                    tv3.setText("???");

                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            for(int i=0; i<services.size();i++) {
                BluetoothGattService s = services.get(i);
                Log.d("DeviceActivity", "Configuring service with uuid : " + s.getUuid().toString());
                if(s.getUuid().toString().equals(SensorTagGatt.UUID_IRT_SERV.toString())) {

                    List<BluetoothGattCharacteristic> characteristics = s.getCharacteristics();
                    BluetoothGattCharacteristic configC = null;
                    dataC = null;

                    int loop_value = 0;
                    while(configC == null || dataC == null) {
                        BluetoothGattCharacteristic c = characteristics.get(loop_value);
                        if(c.getUuid().toString().equals(SensorTagGatt.UUID_IRT_CONF.toString())) {
                            configC = c;
                        }
                        if (c.getUuid().toString().equals(SensorTagGatt.UUID_IRT_DATA.toString())) {
                            dataC = c;
                        }
                            loop_value += 1;
                    }
                    byte[] val = new byte[1];
                    val[0] = (byte)1;
                    configC.setValue(val);
                    gatt.writeCharacteristic(configC);
                }
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            gatt.readCharacteristic(dataC);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            final TextView tv4 = (TextView)findViewById(R.id.tv4);
            Log.i("onCharacteristicRead", characteristic.getUuid().toString());
            if (characteristic.getUuid().toString().equals(SensorTagGatt.UUID_IRT_DATA.toString())) {

                byte[] value = characteristic.getValue();
                Log.i("char", characteristic.getValue().toString());
                double ambient = extractAmbientTemperature(value);
                double target = extractTargetTemperature(value, ambient);
                double targetNewSensor = extractTargetTemperatureTMP007(value);
                tv4.setText(Double.toString(ambient));
                tv4.append(Double.toString(target));
                tv4.append(Double.toString(targetNewSensor));

                Log.d("VALUES", Double.toString(ambient));
                Log.d("VALUES", Double.toString(target));
                Log.d("VALUES", Double.toString(targetNewSensor));

                //gatt.disconnect();
            }
        }

    };
    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }
    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }
    private double extractAmbientTemperature(byte [] v) {
        int offset = 2;
        return shortUnsignedAtOffset(v, offset) / 128.0;
    }

    private double extractTargetTemperature(byte [] v, double ambient) {
        Integer twoByteValue = shortSignedAtOffset(v, 0);

        double Vobj2 = twoByteValue.doubleValue();
        Vobj2 *= 0.00000015625;

        double Tdie = ambient + 273.15;

        double S0 = 5.593E-14; // Calibration factor
        double a1 = 1.75E-3;
        double a2 = -1.678E-5;
        double b0 = -2.94E-5;
        double b1 = -5.7E-7;
        double b2 = 4.63E-9;
        double c2 = 13.4;
        double Tref = 298.15;
        double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
        double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
        double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
        double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

        return tObj - 273.15;
    }
    private double extractTargetTemperatureTMP007(byte [] v) {
        int offset = 0;
        return shortUnsignedAtOffset(v, offset) / 128.0;
    }

}
