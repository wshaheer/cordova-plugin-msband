#import "MSBandPlugin.h"

@interface MSBandPlugin ()<MSBClientManagerDelegate>
{

}

@property (nonatomic, weak) MSBClient *client;
@property (nonatomic, retain) NSString *connectCallbackId;

@end

@implementation MSBandPlugin

- (void) initialize:(CDVInvokedUrlCommand *) command
{
  NSDictionary *returnObj = nil;
  CDVPluginResult *pluginResult = nil;

  [MSBClientManager sharedManager].delegate = self;
  NSArray *attachedClients = [[MSBClientManager sharedManager] attachedClients];

  _client = [attachedClients firstObject];

  if (_client)
  {
    returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSString stringWithString:[_client name]], @"name", [NSString stringWithString:[[_client connectionIdentifier] UUIDString]], @"address", nil];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
  }
  else
  {
    returnObj = [NSDictionary dictionaryWithObjectsAndKeys: @"notInitialized", @"error", @"No paired devices found", @"message", nil];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:returnObj];
  }

  [pluginResult setKeepCallbackAsBool:NO];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) connect:(CDVInvokedUrlCommand *)command
{
  self.connectCallbackId = [NSString stringWithString:command.callbackId];
  [[MSBClientManager sharedManager] connectClient:_client];
}

- (void) clientManager:(MSBClientManager *)manager clientDidConnect:(MSBClient *)client
{
  self.client = client;
  NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSString stringWithString:client.name], @"name", [NSString stringWithString:[client.connectionIdentifier UUIDString]], @"address", @"CONNECTED", @"status", nil];
  CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
  [pluginResult setKeepCallbackAsBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:self.connectCallbackId];
}

- (void) clientManager:(MSBClientManager *)manager clientDidDisconnect:(MSBClient *)client
{
  NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSString stringWithString:client.name], @"name", [NSString stringWithString:[client.connectionIdentifier UUIDString]], @"address", @"DISCONNECTED", @"status", nil];
  CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
  [pluginResult setKeepCallbackAsBool:NO];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:self.connectCallbackId];
}

- (void) clientManager:(MSBClientManager *)manager client:(MSBClient *)client didFailToConnectWithError:(NSError *)error
{
  NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: @"notConnected", @"error", [error localizedDescription], @"message", nil];
  CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:returnObj];
  [pluginResult setKeepCallbackAsBool:NO];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:self.connectCallbackId];
}

- (void) disconnect:(CDVInvokedUrlCommand *)command
{
  if (self.client.isDeviceConnected)
  {
    [[MSBClientManager sharedManager] cancelClientConnection:self.client];
    NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSString stringWithString:self.client.name], @"name", [NSString stringWithString:[self.client.connectionIdentifier UUIDString]], @"address", nil];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
    [pluginResult setKeepCallbackAsBool:NO];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }
}

- (void) contact:(CDVInvokedUrlCommand *)command
{
  if (self.client.isDeviceConnected)
  {
    [self.client.sensorManager startBandContactUpdatesToQueue:nil errorRef:nil withHandler:^(MSBSensorBandContactData *contactData, NSError *error) {
      NSMutableDictionary *returnObj = [NSMutableDictionary dictionaryWithCapacity:1];
      if (contactData.wornState == 0)
      {
        [returnObj setValue:@"NOT_WORN" forKey:@"status"];
      }
      else if (contactData.wornState == 1)
      {
        [returnObj setValue:@"WORN" forKey:@"status"];
      }
      else
      {
        [returnObj setValue:@"UNKNOWN" forKey:@"status"];
      }

      CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
      [pluginResult setKeepCallbackAsBool:[NSNumber numberWithBool:YES]];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
  }
}

- (void) consent:(CDVInvokedUrlCommand *)command
{
  if (self.client.isDeviceConnected)
  {
    __block NSDictionary *returnObj = nil;
    __block CDVPluginResult *pluginResult = nil;
    if ([self.client.sensorManager heartRateUserConsent] == MSBUserConsentGranted)
    {
      returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSNumber numberWithBool:true], @"isGranted", nil];
      pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
      [pluginResult setKeepCallbackAsBool:NO];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
    else
    {
      [self.client.sensorManager requestHRUserConsentWithCompletion:^(BOOL userConsent, NSError *error) {
        returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSNumber numberWithBool:userConsent], @"isGranted", nil];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
        [pluginResult setKeepCallbackAsBool:NO];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
      }];
    }
  }
}

- (void) subscribe:(CDVInvokedUrlCommand *)command
{
  if (self.client.isDeviceConnected)
  {
    __block NSString *callbackId = [NSString stringWithString:command.callbackId];
    __block NSString *event = [NSString stringWithString:(NSString *)command.arguments[0]];

    if ([event compare:@"HEART_RATE"] == 0)
    {
      if ([self.client.sensorManager heartRateUserConsent] == MSBUserConsentGranted)
      {
        [self.client.sensorManager startHeartRateUpdatesToQueue:nil errorRef:nil withHandler:^(MSBSensorHeartRateData *heartRateData, NSError *error) {
          NSMutableDictionary *returnObj = [NSMutableDictionary dictionaryWithCapacity:2];
          [returnObj setValue:[NSNumber numberWithDouble:heartRateData.heartRate] forKey:@"heartRate"];
          [returnObj setValue:[NSNumber numberWithDouble:([[NSDate date] timeIntervalSince1970] * 1000)] forKey:@"timeStamp"];

          CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
          [pluginResult setKeepCallbackAsBool:YES];
          [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
        }];
      }
    }
    else if ([event compare:@"RR_INTERVAL"] == 0)
    {
      [self.client.sensorManager startRRIntervalUpdatesToQueue:nil errorRef:nil withHandler:^(MSBSensorRRIntervalData *rrIntervalData, NSError *error) {
        NSMutableDictionary *returnObj = [NSMutableDictionary dictionaryWithCapacity:2];
        [returnObj setValue:[NSNumber numberWithDouble:rrIntervalData.interval] forKey:@"interval"];
        [returnObj setValue:[NSNumber numberWithDouble:([[NSDate date] timeIntervalSince1970] * 1000)] forKey:@"timeStamp"];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
      }];
    }
    else if ([event compare:@"GSR"] == 0)
    {
      [self.client.sensorManager startGSRUpdatesToQueue:nil errorRef:nil withHandler:^(MSBSensorGSRData *gsrData, NSError *error) {
        NSMutableDictionary *returnObj = [NSMutableDictionary dictionaryWithCapacity:2];
        [returnObj setValue:[NSNumber numberWithDouble:gsrData.resistance] forKey:@"resistance"];
        [returnObj setValue:[NSNumber numberWithDouble:([[NSDate date] timeIntervalSince1970] * 1000)] forKey:@"timeStamp"];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
      }];
    }
    else if ([event compare:@"SKIN_TEMPERATURE"] == 0)
    {
      [self.client.sensorManager startSkinTempUpdatesToQueue:nil errorRef:nil withHandler:^(MSBSensorSkinTemperatureData *skinTempData, NSError *error) {
        NSMutableDictionary *returnObj = [NSMutableDictionary dictionaryWithCapacity:2];
        [returnObj setValue:[NSNumber numberWithDouble:skinTempData.temperature] forKey:@"temperature"];
        [returnObj setValue:[NSNumber numberWithDouble:([[NSDate date] timeIntervalSince1970] * 1000)] forKey:@"timeStamp"];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
        [pluginResult setKeepCallbackAsBool:YES];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
      }];
    }
  }
}

- (void) unsubscribe:(CDVInvokedUrlCommand *)command
{
  if (self.client.isDeviceConnected)
  {
    NSString *callbackId = [NSString stringWithString:command.callbackId];
    NSString *event = [NSString stringWithString:(NSString *)command.arguments[0]];

    if ([event compare:@"HEART_RATE"] == 0)
    {
      [self.client.sensorManager stopHeartRateUpdatesErrorRef:nil];
      NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: @"HeartRateSensor", @"sensor", @"unsubscribed", @"status", nil];
      CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
      [pluginResult setKeepCallbackAsBool:NO];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
    else if ([event compare:@"RR_INTERVAL"] == 0)
    {
      [self.client.sensorManager stopRRIntervalUpdatesErrorRef:nil];
      NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: @"RRIntervalSensor", @"sensor", @"unsubscribed", @"status", nil];
      CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
      [pluginResult setKeepCallbackAsBool:NO];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
    else if ([event compare:@"GSR"] == 0)
    {
      [self.client.sensorManager stopGSRUpdatesErrorRef:nil];
      NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: @"GSRSensor", @"sensor", @"unsubscribed", @"status", nil];
      CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
      [pluginResult setKeepCallbackAsBool:NO];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
    else if ([event compare:@"SKIN_TEMPERATURE"] == 0)
    {
      [self.client.sensorManager stopSkinTempUpdatesErrorRef:nil];
      NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: @"SkinTemperatureSensor", @"sensor", @"unsubscribed", @"status", nil];
      CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
      [pluginResult setKeepCallbackAsBool:NO];
      [self.commandDelegate sendPluginResult:pluginResult callbackId:callbackId];
    }
  }
}

- (void) isConnected:(CDVInvokedUrlCommand *)command
{
  NSDictionary *returnObj = [NSDictionary dictionaryWithObjectsAndKeys: [NSNumber numberWithBool:self.client.isDeviceConnected], @"isConnected", nil];
  CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnObj];
  [pluginResult setKeepCallbackAsBool:NO];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
