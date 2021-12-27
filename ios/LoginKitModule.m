#import "LoginKitModule.h"
#import <React/RCTBridgeModule.h>
#import <React/RCTUtils.h>
#import <SCSDKLoginKit/SCSDKLoginKit.h>

@implementation LoginKitModule  {
    BOOL _hasListeners;
}

#pragma mark - Initialization

- (instancetype)init {
    self = [super init];
    
    if (self) {
      [SCSDKLoginClient addLoginStatusObserver:self];
    }

    return self;
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"LOGIN_KIT_LOGIN_STARTED", @"LOGIN_KIT_LOGIN_SUCCEEDED", @"LOGIN_KIT_LOGIN_FAILED", @"LOGIN_KIT_LOGOUT"];
}

#pragma mark - Login State Updates

- (void)scsdkLoginLinkDidStart
{
    [self dispatchEvent:@"LOGIN_KIT_LOGIN_STARTED"];
}

- (void)scsdkLoginLinkDidFail
{
    [self dispatchEvent:@"LOGIN_KIT_LOGIN_FAILED"];
}

- (void)scsdkLoginLinkDidSucceed
{
    [self dispatchEvent:@"LOGIN_KIT_LOGIN_SUCCEEDED"];
}

- (void)scsdkLoginDidUnlink
{
    [self dispatchEvent:@"LOGIN_KIT_LOGOUT"];
}

#pragma mark Sending Events to JavaScript

// See https://reactnative.dev/docs/native-modules-ios#sending-events-to-javascript
// Will be called when this module's first listener is added.
- (void)startObserving
{
    _hasListeners = YES;
}

// Will be called when this module's last listener is removed, or on dealloc.
- (void)stopObserving
{
    _hasListeners = NO;
    [SCSDKLoginClient removeLoginStatusObserver:self];
}

// See https://reactnative.dev/docs/native-modules-ios#threading
- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

- (void)dispatchEvent:(NSString *) eventName
{
    if (_hasListeners) {
        [self sendEventWithName:eventName body: nil];
    }
}

#pragma mark Public APIs

RCT_EXPORT_MODULE(LoginKit)

RCT_EXPORT_METHOD(login:(RCTPromiseResolveBlock)resolve
                  loginReject:(RCTPromiseRejectBlock)reject)
{
    UIViewController *presentedViewController = RCTPresentedViewController();
    if (presentedViewController != NULL) {
        [SCSDKLoginClient loginFromViewController:presentedViewController
                                       completion:^(BOOL success, NSError * _Nullable error)
         {
            if (success) {
                resolve(nil);
            } else {
                // TODO: Get matching error codes for Android
                reject(error.domain, error.localizedDescription, nil);
            }
        }];
    }
}

RCT_REMAP_METHOD(isUserLoggedIn,
                 isUserLoggedInResolver:(RCTPromiseResolveBlock)resolve
                 isUserLoggedInRejector:(RCTPromiseRejectBlock)reject)
{
    resolve(@([SCSDKLoginClient isUserLoggedIn]));
}


RCT_REMAP_METHOD(getAccessToken,
                 getAccessTokenResolver:(RCTPromiseResolveBlock)resolve
                 getAccessTokenRejecter:(RCTPromiseRejectBlock)reject)
{
    resolve([SCSDKLoginClient getAccessToken]);
}

RCT_REMAP_METHOD(refreshAccessToken,
                 refreshAccessTokenResolver:(RCTPromiseResolveBlock)resolve
                 refreshAccessTokenRejector:(RCTPromiseRejectBlock)reject)
{
    [SCSDKLoginClient refreshAccessTokenWithCompletion:^(NSString *_Nullable accessToken, NSError *_Nullable error) {
        if (accessToken == nil) {
            // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
            reject(error.domain, error.description, nil);
        } else {
            resolve(accessToken);
        }
    }];
}

RCT_EXPORT_METHOD(clearToken)
{
    [SCSDKLoginClient clearToken];
}

RCT_REMAP_METHOD(hasAccessToScope,
                 scopeToCheck:(nonnull NSString *)scope
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)
{
    resolve(@([SCSDKLoginClient hasAccessToScope:scope]));
}

RCT_EXPORT_METHOD(fetchUserData:(nonnull NSString *)query
                  variables:(nullable NSDictionary *)variables
                  fetchUserDataResolver:(RCTPromiseResolveBlock)resolve
                  fetchUserDataRejector:(RCTPromiseRejectBlock)reject)
{
    [SCSDKLoginClient fetchUserDataWithQuery:query variables:variables success:^(NSDictionary *resources) {
        NSMutableDictionary *userDataDict = [NSMutableDictionary new];
        NSDictionary *dataDict = [resources objectForKey:@"data"];
        NSDictionary *meDict = [dataDict objectForKey:@"me"];
        NSDictionary *bitmojiDict = [meDict objectForKey:@("bitmoji")];

        if ([meDict objectForKey:@("displayName")] != nil) {
            [userDataDict setObject:[meDict objectForKey:@"displayName"] forKey:@("displayName")];
        }
        if ([meDict objectForKey:@("externalId")] != nil) {
            [userDataDict setObject:[meDict objectForKey:@"externalId"] forKey:@("externalId")];
        }
        if ([meDict objectForKey:@("profileLink")] != nil) {
            [userDataDict setObject:[meDict objectForKey:@"profileLink"] forKey:@("profileLink")];
        }
        if ([bitmojiDict objectForKey:@("id")] != nil) {
            [userDataDict setObject:[bitmojiDict objectForKey:@"id"] forKey:@("bitmojiId")];
        }
        if ([bitmojiDict objectForKey:@("selfie")] != nil) {
            [userDataDict setObject:[bitmojiDict objectForKey:@"selfie"] forKey:@("bitmojiSelfie")];
        }
        if ([bitmojiDict objectForKey:@("avatar")] != nil) {
            [userDataDict setObject:[bitmojiDict objectForKey:@"avatar"] forKey:@("bitmojiAvatar")];
        }
        if ([bitmojiDict objectForKey:@("packs")] != nil) {
            [userDataDict setObject:[bitmojiDict objectForKey:@"packs"] forKey:@("bitmojiPacksJson")];
        }
        resolve(userDataDict);
    } failure:^(NSError * error, BOOL isUserLoggedOut) {
        // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
        reject(error.domain, error.localizedDescription, nil);
    }];
}

RCT_EXPORT_METHOD(verify:(nonnull NSString *)phoneNumber
                  countryCode:(nullable NSString *)countryCode
                  verifyResolver:(RCTPromiseResolveBlock)resolve
                  verifyRejector:(RCTPromiseRejectBlock)reject)
{
    UIViewController *presentedViewController = RCTPresentedViewController();
    [SCSDKVerifyClient verifyFromViewController:presentedViewController phone:phoneNumber region:countryCode completion:^(NSString * _Nullable phoneId, NSString * _Nullable verifyId, NSError * _Nullable error) {
        if (error != nil) {
            // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
            reject(error.domain, error.localizedDescription, nil);
        } else {
            NSDictionary *verifyResponse = [[NSDictionary alloc] initWithObjectsAndKeys: phoneId, @"phoneId", verifyId, @"verifyId", nil];
            resolve(verifyResponse);
        }
    }];
}

RCT_EXPORT_METHOD(verifyAndLogin:(nonnull NSString *)phoneNumber
                  countryCode:(nullable NSString *)countryCode
                  verifyAndLoginResolver:(RCTPromiseResolveBlock)resolve
                  verifyAndLoginRejector:(RCTPromiseRejectBlock)reject)
{
    UIViewController *presentedViewController = RCTPresentedViewController();
    [SCSDKVerifyClient verifyAndLoginFromViewController:presentedViewController phone:phoneNumber region:countryCode completion:^(BOOL success, NSString * _Nullable phoneId, NSString * _Nullable verifyId, NSError * _Nullable error) {
        if (error != nil) {
            // TODO(Error_Types): Define specific Error Types rather than throwing generic free flow errors
            reject(error.domain, error.localizedDescription, nil);
        } else {
            NSDictionary *verifyResponse = [[NSDictionary alloc] initWithObjectsAndKeys: phoneId, @"phoneId", verifyId, @"verifyId", nil];
            resolve(verifyResponse);
        }
    }];
}

@end

