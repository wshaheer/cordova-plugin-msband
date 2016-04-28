#import <MicrosoftBandKit_iOS/MicrosoftBandKit_iOS.h>
#import <Cordova/CDVPlugin.h>

@interface MSBandPlugin : CDVPlugin<MSBClientManagerDelegate>
{

}

- (void) initialize:(CDVInvokedUrlCommand *)command;
- (void) connect:(CDVInvokedUrlCommand *)command;
- (void) disconnect:(CDVInvokedUrlCommand *)command;
- (void) contact:(CDVInvokedUrlCommand *)command;
- (void) consent:(CDVInvokedUrlCommand *)command;
- (void) subscribe:(CDVInvokedUrlCommand *)command;
- (void) unsubscribe:(CDVInvokedUrlCommand *)command;
- (void) isConnected:(CDVInvokedUrlCommand *)command;

@end
