package com.tuempresa.beacondetector;

import android.Manifest;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.altbeacon.beacon.*;

import java.util.Collection;

public class BeaconDetector extends CordovaPlugin implements BeaconConsumer {

    private static final String TAG = "BeaconDetector";
    private BeaconManager beaconManager;
    private CallbackContext beaconCallbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("startScanning".equals(action)) {
            startScanning(callbackContext);
            return true;
        } else if ("stopScanning".equals(action)) {
            stopScanning(callbackContext);
            return true;
        } else if ("setBeaconCallback".equals(action)) {
            setBeaconCallback(callbackContext);
            return true;
        }
        return false;
    }

    private void startScanning(CallbackContext callbackContext) {
        // Verificar permiso de ubicación en runtime para Android 6+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!cordova.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                cordova.requestPermission(this, 0, Manifest.permission.ACCESS_FINE_LOCATION);
                callbackContext.error("Permiso de ubicación no concedido");
                return;
            }
        }
        if (beaconManager == null) {
            beaconManager = BeaconManager.getInstanceForApplication(cordova.getActivity());
            // Configurar el parser para iBeacon
            beaconManager.getBeaconParsers().add(new BeaconParser().
                    setBeaconLayout(BeaconParser.IBEACON_LAYOUT));
            beaconManager.bind(this);
        }
        callbackContext.success("Escaneo iniciado");
    }

    private void stopScanning(CallbackContext callbackContext) {
        if (beaconManager != null && beaconManager.isBound(this)) {
            beaconManager.unbind(this);
            beaconManager = null;
        }
        callbackContext.success("Escaneo detenido");
    }

    private void setBeaconCallback(CallbackContext callbackContext) {
        this.beaconCallbackContext = callbackContext;
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beaconCallbackContext != null && !beacons.isEmpty()) {
                    for (Beacon beacon : beacons) {
                        try {
                            JSONObject jsonBeacon = new JSONObject();
                            jsonBeacon.put("uuid", beacon.getId1().toString());
                            jsonBeacon.put("major", beacon.getId2().toInt());
                            jsonBeacon.put("minor", beacon.getId3().toInt());
                            jsonBeacon.put("rssi", beacon.getRssi());
                            PluginResult result = new PluginResult(PluginResult.Status.OK, jsonBeacon);
                            result.setKeepCallback(true);
                            beaconCallbackContext.sendPluginResult(result);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error construyendo JSON para beacon", e);
                        }
                    }
                }
            }
        });
        try {
            // Inicia ranging sin filtrar (para detectar todos los beacons iBeacon)
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "Error iniciando ranging", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (beaconManager != null && beaconManager.isBound(this)) {
            beaconManager.unbind(this);
        }
    }
}
