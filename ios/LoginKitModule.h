#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#import <SCSDKLoginKit/SCSDKLoginKit.h>

@interface LoginKitModule : RCTEventEmitter <RCTBridgeModule, SCSDKLoginStatusObserver>

- (void)dispatchEvent:(NSString *) eventName;

- (void)scsdkLoginLinkDidStart;

- (void)scsdkLoginLinkDidFail;

- (void)scsdkLoginLinkDidSucceed;

- (void)scsdkLoginDidUnlink;

@end
