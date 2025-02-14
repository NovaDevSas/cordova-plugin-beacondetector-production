import Foundation
import CoreLocation
import Cordova

@objc(BeaconDetector) class BeaconDetector: CDVPlugin, CLLocationManagerDelegate {

    var locationManager: CLLocationManager?
    var beaconCallbackId: String?
    var isScanning = false

    override func pluginInitialize() {
        super.pluginInitialize()
        locationManager = CLLocationManager()
        locationManager?.delegate = self
        locationManager?.allowsBackgroundLocationUpdates = true
        locationManager?.pausesLocationUpdatesAutomatically = false
    }

    @objc func startScanning(_ command: CDVInvokedUrlCommand) {
        if isScanning {
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Ya se está escaneando")
            self.commandDelegate.send(result, callbackId: command.callbackId)
            return
        }
        // Solicitar permiso siempre (para foreground y background)
        locationManager?.requestAlwaysAuthorization()
        
        // Definir la región iBeacon (reemplaza el UUID por el real)
        let uuidString = "00000000-0000-0000-0000-000000000000"
        if let uuid = UUID(uuidString: uuidString) {
            let region = CLBeaconRegion(uuid: uuid, identifier: "MyBeaconRegion")
            locationManager?.startMonitoring(for: region)
            // En iOS 13+ se usa ranging basado en CLBeaconIdentityConstraint
            let constraint = CLBeaconIdentityConstraint(uuid: uuid)
            locationManager?.startRangingBeacons(satisfying: constraint)
        }
        isScanning = true
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Escaneo iniciado")
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func stopScanning(_ command: CDVInvokedUrlCommand) {
        let uuidString = "00000000-0000-0000-0000-000000000000"
        if let uuid = UUID(uuidString: uuidString) {
            let region = CLBeaconRegion(uuid: uuid, identifier: "MyBeaconRegion")
            locationManager?.stopMonitoring(for: region)
            let constraint = CLBeaconIdentityConstraint(uuid: uuid)
            locationManager?.stopRangingBeacons(satisfying: constraint)
        }
        isScanning = false
        let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Escaneo detenido")
        self.commandDelegate.send(result, callbackId: command.callbackId)
    }

    @objc func setBeaconCallback(_ command: CDVInvokedUrlCommand) {
        self.beaconCallbackId = command.callbackId
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_NO_RESULT)
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    // MARK: - CLLocationManagerDelegate

    func locationManager(_ manager: CLLocationManager, didRange beacons: [CLBeacon], satisfying beaconConstraint: CLBeaconIdentityConstraint) {
        guard let callbackId = beaconCallbackId, !beacons.isEmpty else {
            return
        }
        for beacon in beacons {
            let beaconData: [String: Any] = [
                "uuid": beacon.uuid.uuidString,
                "major": beacon.major.intValue,
                "minor": beacon.minor.intValue,
                "rssi": beacon.rssi
            ]
            let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: beaconData)
            result?.setKeepCallbackAs(true)
            self.commandDelegate.send(result, callbackId: callbackId)
        }
    }
}
