var exec = require('cordova/exec');

var BeaconDetector = {
    startScanning: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, "BeaconDetector", "startScanning", []);
    }
};

module.exports = BeaconDetector;
