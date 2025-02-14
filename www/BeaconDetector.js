var exec = require('cordova/exec');

var BeaconDetector = {
    startScanning: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BeaconDetector", "startScanning", []);
    },
    stopScanning: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BeaconDetector", "stopScanning", []);
    },
    setBeaconCallback: function(callback, errorCallback) {
        exec(callback, errorCallback, "BeaconDetector", "setBeaconCallback", []);
    }
};

module.exports = BeaconDetector;
