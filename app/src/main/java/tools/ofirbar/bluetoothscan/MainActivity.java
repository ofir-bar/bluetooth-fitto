package tools.ofirbar.bluetoothscan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int LOCATION_PERMISSION = 99;

    private final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private BluetoothDevice mDeviceToConnect = null;

    // Stops scanning after 15 seconds.
    private static final long SCAN_PERIOD = 15000;
    private Context mContext;

    BluetoothGatt fittoServer;
    List<BluetoothGattService> fittoServices;
    List<BluetoothGattCharacteristic> fittoCharacterics;

    private static final String FITTO_MAC_ADDRESS = "EE:F0:EA:17:69:B4";

    private String deviceManufacturer;
    private String deviceModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;
        mHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        // Initializes Bluetooth adapter.
        bluetoothAdapter = bluetoothManager.getAdapter();

        isBluetoothSupportAndAvailable();
        isLocationPermitted();

        Button connectToFitto = findViewById(R.id.btn_connect);
        connectToFitto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "Sending connection request");
                scanLeDevice(true);
            }
        });

        Button disconnectFromFitto = findViewById(R.id.btn_disconnect);
        disconnectFromFitto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fittoServer != null){
                    fittoServer.disconnect();
                    fittoServer.close();

                    Log.w(TAG,"Disconnected from Fitto");
                }
                else {
                    Log.w(TAG,"Device is not connected to the Fitto");
                }
            }
        });



    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop scanning if the Activity is paused
        mScanning = false;
        scanLeDevice(false);
    }

    // Scan LE Bluetooth device
    private void scanLeDevice(final boolean enable) {

        if (enable) {

             // Stops scanning after a SCAN_PERIOD seconds.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);

        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }

    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            // Check to see if FITTO is found
                            if (device.getAddress().equals(FITTO_MAC_ADDRESS) && device.getAddress() != null ){

                                // Stop scanning once the FITTO is found
                                scanLeDevice(false);

                                // Connect to the Fitto server
                                fittoServer = device.connectGatt(mContext, false, serverCallback);

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                }

                                // After the server is connected,
                                Log.w(TAG, "Discovering services...");
                                fittoServer.discoverServices(); //TODO: DONT DELETE THIS!!!!

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                }

                                fittoServices = fittoServer.getServices();

                                for(BluetoothGattService service : fittoServices){
                                    Log.w(TAG, service.getUuid().toString());
                                    if (service.getUuid().toString().equals("0000180a-0000-1000-8000-00805f9b34fb")){
                                        Log.w(TAG, "UUID 180a Found, getting characteristics.. ");
                                        fittoCharacterics = service.getCharacteristics();
                                        try {
                                            Thread.sleep(10000);
                                        } catch (InterruptedException e) {
                                            Log.w(TAG, e);
                                        } // Wait 5 sec

                                        for (BluetoothGattCharacteristic characteristic : fittoCharacterics){
                                        fittoServer.readCharacteristic(characteristic);
                                            try {
                                                Thread.sleep(3000);
                                            } catch (InterruptedException e) {
                                                Log.w(TAG, e);
                                            } // Wait 3 sec
                                        }

                                    }
                                }
                            }
                        }
                    });


                }
            };




    // Fitto Server callback
    BluetoothGattCallback serverCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState){
                case BluetoothProfile.STATE_CONNECTING: {
                    Log.w(TAG, "Connecting...");
                    break;
                }
                case BluetoothProfile.STATE_CONNECTED:{
                    Log.w(TAG, "Connected to Fitto bottle");
                    break;
                }
                case BluetoothProfile.STATE_DISCONNECTING: {
                    Log.w(TAG, "Disconnecting from Fitto bottle...");
                    break;
                }
                case BluetoothProfile.STATE_DISCONNECTED: {
                    Log.w(TAG, "Disconnected from Fitto bottle");
                    break;
                }
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.w(TAG,"Found device: " + gatt.getDevice().getName());
            Log.w(TAG, "Extracting services from the device");
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            deviceManufacturer = new String(characteristic.getValue());
            Log.w(TAG,deviceManufacturer);

            if (characteristic.getUuid().toString().equals("")){
                deviceManufacturer = new String(characteristic.getValue());
                Log.w(TAG,deviceManufacturer);
            }

            if (characteristic.getUuid().toString().equals("")){
                deviceModel = new String(characteristic.getValue());
            }


        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT){
            if (resultCode == RESULT_OK){
                Log.e(TAG, "USER ACCEPTED REQUEST TO ENABLE BLUETOOTH");
            }
            else if (resultCode == RESULT_CANCELED){
                Log.e(TAG, "USER DENIED REQUEST TO ENABLE BLUETOOTH");
            }
        }
    }


    private void isBluetoothSupportAndAvailable() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private boolean isLocationPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


                new AlertDialog.Builder(this)
                        .setTitle("Location permission")
                        .setMessage("Location permission is mandatory")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "User accepted location permission.");
                    }

                } else {

                    Log.w(TAG, "User denied location permission.");
                    finish();

                }
                return;
            }

        }
    }
}
