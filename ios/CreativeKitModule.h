#import <React/RCTBridgeModule.h>
#import <SCSDKCreativeKit/SCSDKCreativeKit.h>

@interface CreativeKitModule : NSObject <RCTBridgeModule>

- (SCSDKSnapSticker*)createSnapSticker:(NSDictionary*)stickerMap;

@end

