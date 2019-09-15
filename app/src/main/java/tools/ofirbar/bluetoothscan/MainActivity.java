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
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.UUID;

import static tools.ofirbar.bluetoothscan.Constants.FITTO_MAC_ADDRESS;
import static tools.ofirbar.bluetoothscan.Constants.Nordic_UART_Service;
import static tools.ofirbar.bluetoothscan.Constants.RX_Characteristic;
import static tools.ofirbar.bluetoothscan.Constants.TX_Characteristic;
import static tools.ofirbar.bluetoothscan.Constants.TX_Descriptor;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final int LOCATION_PERMISSION = 99;
    private final String TAG = "MainActivity";
    private BluetoothAdapter bluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 15 seconds.
    private static final long SCAN_PERIOD = 5000;
    private Context mContext;

    BluetoothGatt fittoServer;
    DeviceStatusModel deviceStatusModel;
    TextView deviceFirmwareVersion;

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

        deviceFirmwareVersion = findViewById(R.id.device_firmware_version);


        Button connectToFitto = findViewById(R.id.btn_connect);
        connectToFitto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w(TAG, "Sending connection request");
                scanForBluetoothDevices(true);
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
        scanForBluetoothDevices(false);
    }

    // Scan for LE Bluetooth devices
    private void scanForBluetoothDevices(final boolean enable) {

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
                                scanForBluetoothDevices(false);

                                // Connect to the Fitto server
                                fittoServer = device.connectGatt(mContext, false, serverCallback);

                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                }

                                // After the server is connected,
                                Log.w(TAG, "Discovering services...");
                                fittoServer.discoverServices();
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                } // Wait for services to be discovered.


                                BluetoothGattService nordicUartService = fittoServer.getService(UUID.fromString(Nordic_UART_Service));

                                BluetoothGattCharacteristic rxCharacteristic = nordicUartService.getCharacteristic(UUID.fromString(RX_Characteristic));
                                BluetoothGattCharacteristic txCharacteristic = nordicUartService.getCharacteristic(UUID.fromString(TX_Characteristic));
                                fittoServer.setCharacteristicNotification(txCharacteristic, true);

                                Log.w(TAG, "Enabling Characteristics notifications on TxCharacteristic..");
                                try {
                                    Thread.sleep(3);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                }


                                Log.w(TAG, "Subscribing to Tx Characteristic notifications");
                                BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(UUID.fromString(TX_Descriptor));

                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); // Set the descriptor to receive notifications
                                fittoServer.writeDescriptor(descriptor);

                                try {
                                    Thread.sleep(3);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                }


                                try {
                                    Thread.sleep(9000);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);
                                } // Wait for client to subscribe for notifications

                                Log.w(TAG, "Finished subscribing");
                                Log.w(TAG, "Writing to change the BLE color");
                                // Write another command
//
//                                rxCharacteristic.setValue(new byte[]{0x22, 0x01}); // add the value to the write command
//                                fittoServer.writeCharacteristic(rxCharacteristic); // Execute write command!

                                //




                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Log.w(TAG, e);

                                }



//                                This command should make the Bottle with colors.
//
//                                byte[] command = new byte[]{
//                                       0x23, // 0x23 request code
//                                       0x05, // 0x05 id (8 bits)
//                                       0x00, 0x00, 0x00, 0x00, // delay time in seconds: {0x00, 0x00, 0x00, 0x00}
//                                       0x05, 0x00, // (Turn on for 1280 ms)
//                                       0x05, 0x00, // (Turned off for 1280 ms)
//                                       0x00, 0x05, // time to repeat {0x00, 0x05}
//                                       0x06, // Color: 0x06 (Pink)
//                                       0x00, // 0x00 signal end of packet send
//                                };

//                                rxCharacteristic.setValue(command); // add the value to the write command
//                                fittoServer.writeCharacteristic(rxCharacteristic); // Execute write command!

                            }
                        }
                    });


                }
            };

    // Fitto Server callback
    BluetoothGattCallback serverCallback = new BluetoothGattCallback() {

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
            Log.w("onCharacteristicRead"," callback!");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.w(TAG, "onCharacteristicWrite callback executed");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.w("onCharacteristicChanged", " onCharacteristicChanged");

            byte[] result = characteristic.getValue();
            String resultAsHex = bytesToHex(result);
            String resultCode = resultAsHex.substring(0, resultAsHex.indexOf("-"));

            // Makes sure we get the data for our request
            if (resultCode.equals("10")){
                deviceStatusModel = new DeviceStatusModel(characteristic.getValue());

                String firmwareVersionValue = deviceStatusModel.getVersionStr();
                Log.w("Found firmware version:", firmwareVersionValue);
            }
            else if (resultCode.equals("23")){
                Log.w("Result code value:", resultCode);
                Log.w("resultAsHex", resultAsHex);
            }


        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.w("onDescriptorWrite", "onDescriptorWrite");
        }

    };

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
            sb.append("-");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String hex) {
        int l = hex.length();
        byte[] data = new byte[l/2];
        for (int i = 0; i < l; i += 2) {
            data[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }


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
