package com.ton_in.chapomatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    public static final String EXTRA_MESSAGE = "com.ton_in.chapomatic.MESSAGE";
    public static final String DEVICE_NAME = "Adafruit Bluefruit LE";
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    public static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice currentDevice = null;
    private BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
    private boolean isScanningForDevice = false;
    private boolean isConnected = false;
    private Handler handler = new Handler();

    private BluetoothLeService bluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        try
        {
            if(!bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE))
            {
                Toast.makeText(MainActivity.this, "bindService returned false", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onSendPositionClicked(View view)
    {
        EditText editTextLatitude = findViewById(R.id.editLatitude);
        EditText editTextLongitude = findViewById(R.id.editLongitude);

        String message;

        if (editTextLatitude.getText().length() == 0 || editTextLongitude.getText().length() == 0)
        {
            message = getString(R.string.coordinatesNotFound);
        }
        else
        {
            message = editTextLatitude.getText().toString() + " ; " + editTextLongitude.getText().toString();
        }

        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void onConnexionClicked(View view)
    {
        tryToConnect();
    }

    private void tryToConnect()
    {
        if (bluetoothAdapter == null)
        {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, getString(R.string.bluetoothNotSupported));
            startActivity(intent);
            return;
        }

        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        onBluetoothEnabled();
    }

    private void onBluetoothEnabled()
    {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices)
        {
            String name = device.getName();
            if (name.equals(DEVICE_NAME))
            {
                Log.d("Chap", "Paired to " + name);
                connectToDevice(device);
                return;
            }
        }

        if (!isScanningForDevice)
            toggleDiscoverDevice();
    }

    private void toggleDiscoverDevice()
    {
        if (!isScanningForDevice)
        {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run() {
                    isScanningForDevice = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                    if (!isConnected)
                        Toast.makeText(MainActivity.this, R.string.notFound, Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            Toast.makeText(MainActivity.this, R.string.lookingForDevice, Toast.LENGTH_SHORT).show();
            isScanningForDevice = true;
            bluetoothLeScanner.startScan(leScanCallback);
        }
        else
        {
            isScanningForDevice = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private ScanCallback leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            String name = result.getDevice().getName();
            if (name != null && name.equals(DEVICE_NAME))
            {
                if (isScanningForDevice)
                {
                    toggleDiscoverDevice();
                }

                connectToDevice(result.getDevice());
            }

        }
    };

    private void connectToDevice(BluetoothDevice device)
    {
        Toast.makeText(MainActivity.this, getString(R.string.connecting) + " " + device.getName(), Toast.LENGTH_SHORT).show();

        bluetoothLeService.connect(device.getAddress());

        currentDevice = device;
    }

    private void disconnectFromDevice()
    {
        bluetoothLeService.disconnect();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ENABLE_BT)
        {
            switch (resultCode)
            {
                case RESULT_OK:
                    onBluetoothEnabled();
                    break;
                case RESULT_CANCELED:
                    break;
                default:
                    Intent intent = new Intent(this, DisplayMessageActivity.class);
                    intent.putExtra(EXTRA_MESSAGE, getString(R.string.bluetoothConnexionError));
                    startActivity(intent);
            }
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service)
        {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bluetoothLeService.initialize())
            {
                Log.e("Chap", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            tryToConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            bluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                isConnected = true;
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                isConnected = false;
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                displayChapService();
            }
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {

            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothLeService != null) {
            final boolean result = bluetoothLeService.connect(currentDevice.getAddress());
            Log.d("Chap", "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private void displayChapService()
    {
        if (bluetoothLeService == null)
            return;

        List<BluetoothGattService> gattServices = bluetoothLeService.getSupportedGattServices();

        if (gattServices == null)
            return;

        for (BluetoothGattService gattService : gattServices)
        {
            String uuid = gattService.getUuid().toString();
            if (uuid.equals(SampleGattAttributes.CHAP))
            {
                String message = "Chap Service:\n";

                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                {
                    uuid = gattCharacteristic.getUuid().toString();
                    message += "  - " + (SampleGattAttributes.lookup(uuid,"unknownCharacteristic")) + " (" + uuid + ")\n";
                }

                Intent intent = new Intent(this, DisplayMessageActivity.class);
                intent.putExtra(EXTRA_MESSAGE, message);
                startActivity(intent);

                return;
            }
        }

        Toast.makeText(MainActivity.this, R.string.chapServiceNotFound, Toast.LENGTH_SHORT).show();
    }

    private void displayGattServices(List<BluetoothGattService> gattServices)
    {
        if (gattServices == null)
            return;

        String uuid = null;
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        gattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) 
        {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, "unknownService"));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) 
            {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid,"unknownCharacteristic"));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            gattCharacteristics.addAll(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
