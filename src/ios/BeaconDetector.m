#import "BeaconDetector.h"
#import <Cordova/CDV.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface BeaconDetector () <CBCentralManagerDelegate, CBPeripheralDelegate>
@property (nonatomic, strong) CBCentralManager *centralManager;
@property (nonatomic, strong) CBPeripheral *peripheral;
@property (nonatomic, copy) NSString *callbackId;
@end

@implementation BeaconDetector

- (void)pluginInitialize {
    self.centralManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
}

- (void)startScanning:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    [self.centralManager scanForPeripheralsWithServices:nil options:nil];
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central {
    if (central.state == CBManagerStatePoweredOn) {
        [self.centralManager scanForPeripheralsWithServices:nil options:nil];
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {
    NSMutableDictionary *beaconData = [NSMutableDictionary dictionary];
    beaconData[@"name"] = peripheral.name;
    beaconData[@"rssi"] = RSSI;
    // Add other beacon data

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:beaconData];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

@end
