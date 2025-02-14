package com.yourcompany;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

public class BeaconDetector extends CordovaPlugin {
    private BluetoothLeScanner scanner;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("startScanning")) {
            startScanning();
            return true;
        }
        return false;
    }

    private void startScanning() {
        BluetoothManager bluetoothManager = (BluetoothManager) cordova.getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        scanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                String address = device.getAddress();
                int rssi = result.getRssi();
                byte[] scanRecord = result.getScanRecord().getBytes();

                // Parse beacon data from scanRecord
                // ...

                JSONObject beaconData = new JSONObject();
                try {
                    beaconData.put("address", address);
                    beaconData.put("rssi", rssi);
                    // Add other beacon data
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, beaconData);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }
}
