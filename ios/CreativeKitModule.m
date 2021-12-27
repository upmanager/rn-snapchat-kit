#import "CreativeKitModule.h"
#import <SCSDKCreativeKit/SCSDKCreativeKit.h>

@implementation CreativeKitModule
{
    SCSDKSnapAPI *_scSdkSnapApi;
}

- (instancetype)init {
    self = [super init];
    
    _scSdkSnapApi = [SCSDKSnapAPI new];
    
    return self;
}

- (SCSDKSnapSticker*)createSnapSticker:(NSDictionary*)stickerMap {
    NSString *stickerData = nil;
    if (stickerMap != nil) {
        stickerData = stickerMap[@"uri"];
    }
    
    if (stickerData == nil) {
        return nil;
    }
    
    NSURL *url = [NSURL URLWithString:stickerData];
    NSData *imageData = [NSData dataWithContentsOfURL:url];
    UIImage *stickerImage = [UIImage imageWithData:imageData];
    
    SCSDKSnapSticker *snapSticker = [[SCSDKSnapSticker alloc] initWithStickerImage:stickerImage];
    
    if (stickerMap[@"width"]) {
        snapSticker.width = [[stickerMap valueForKey:@"width"] floatValue];
    }
    
    if (stickerMap[@"height"]) {
        snapSticker.height = [[stickerMap valueForKey:@"height"] floatValue];
    }
    
    if (stickerMap[@"posX"]) {
        snapSticker.posX = [[stickerMap valueForKey:@"posX"] floatValue];
    }
    
    if (stickerMap[@"posY"]) {
        snapSticker.posY = [[stickerMap valueForKey:@"posY"] floatValue];
    }
    
    if (stickerMap[@"rotationDegreesInClockwise"]) {
        snapSticker.rotation = [[stickerMap valueForKey:@"rotationDegreesInClockwise"] floatValue];
    }
    
    return snapSticker;
}

RCT_EXPORT_MODULE(CreativeKit)

RCT_EXPORT_METHOD(sharePhoto:(nonnull NSDictionary *)photoContent
                  sharePhotoResolver:(RCTPromiseResolveBlock)resolve
                  sharePhotoRejector:(RCTPromiseRejectBlock)reject)
{
    NSData *imageData = nil;
    if (photoContent[@"content"]) {
        NSDictionary *contentDict = photoContent[@"content"];
        
        if (contentDict[@"raw"]) {
            imageData = [[NSData alloc] initWithBase64EncodedString:contentDict[@"raw"] options:NSDataBase64DecodingIgnoreUnknownCharacters];
        } else {
            NSURL *url = [NSURL URLWithString:contentDict[@"uri"]];
            imageData = [NSData dataWithContentsOfURL:url];
        }
    }
    
    if (imageData != nil) {
        UIImage *snapImage = [UIImage imageWithData:imageData];
        SCSDKSnapPhoto *photo = [[SCSDKSnapPhoto alloc] initWithImage:snapImage];
        SCSDKPhotoSnapContent *photoSnapContent = [[SCSDKPhotoSnapContent alloc] initWithSnapPhoto:photo];
        
        if (photoContent[@"caption"]) {
            photoSnapContent.caption = photoContent[@"caption"];
        }
        
        if (photoContent[@"attachmentUrl"]) {
            photoSnapContent.attachmentUrl = photoContent[@"attachmentUrl"];
        }
        
        if (photoContent[@"sticker"]) {
            SCSDKSnapSticker *snapSticker = [self createSnapSticker:photoContent[@"sticker"]];
            if (snapSticker != nil) {
                photoSnapContent.sticker = snapSticker;
            }
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [self->_scSdkSnapApi startSendingContent:photoSnapContent completionHandler:^(NSError *error) {

                if (error != nil) {
                    NSLog(@"%@", [NSString stringWithFormat:@"%@/%@", @"Unable to share photo - ", error.localizedDescription]);
                    reject(@"error", error.localizedDescription, nil);
                } else {
                    resolve(@YES);
                }
            }];
        });
    } else {
        reject(@"error", @"Invalid photo data", nil);
        return;
    }
}

RCT_EXPORT_METHOD(shareVideo:(nonnull NSDictionary *)videoContent
                  sharePhotoResolver:(RCTPromiseResolveBlock)resolve
                  sharePhotoRejector:(RCTPromiseRejectBlock)reject)
{
    NSString *videoData = nil;
    if (videoContent[@"content"]) {
        NSDictionary *contentDict = videoContent[@"content"];
        videoData = contentDict[@"uri"];
    }
    
    if (videoData == nil || [[videoData stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]]  isEqual: @""]) {
        reject(@"error", @"Invalid video provided.", nil);
        return;
    }
    
    NSURL *url = [NSURL URLWithString:videoData];
    if (url != nil) {
        SCSDKSnapVideo *video = [[SCSDKSnapVideo alloc] initWithVideoUrl:url];
        SCSDKVideoSnapContent *videoSnapContent = [[SCSDKVideoSnapContent alloc] initWithSnapVideo:video];
        
        if (videoContent[@"caption"]) {
            videoSnapContent.caption = videoContent[@"caption"];
        }
        
        if (videoContent[@"attachmentUrl"]) {
            videoSnapContent.attachmentUrl = videoContent[@"attachmentUrl"];
        }
        
        if (videoContent[@"sticker"]) {
            SCSDKSnapSticker *snapSticker = [self createSnapSticker:videoContent[@"sticker"]];
            
            if (snapSticker != nil) {
                videoSnapContent.sticker = snapSticker;
            }
        }
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [self->_scSdkSnapApi startSendingContent:videoSnapContent completionHandler:^(NSError *error) {
                
                if (error != nil) {
                    NSLog(@"%@", [NSString stringWithFormat:@"%@/%@", @"Unable to share video - ", error.localizedDescription]);
                    reject(@"error", error.localizedDescription, nil);
                } else {
                    resolve(@YES);
                }
            }];
        });
    } else {
        reject(@"error", @"Invalid video data", nil);
    }
}

RCT_EXPORT_METHOD(shareToCameraPreview:(nonnull NSDictionary *)metadata
                  shareToCameraPreviewResolver:(RCTPromiseResolveBlock)resolve
                  shareToCameraPreviewRejector:(RCTPromiseRejectBlock)reject)
{
    SCSDKNoSnapContent *snapContent = [[SCSDKNoSnapContent alloc] init];
    
    if (metadata[@"caption"]) {
        snapContent.caption = metadata[@"caption"];
    }
    
    if (metadata[@"attachmentUrl"]) {
        snapContent.attachmentUrl = metadata[@"attachmentUrl"];
    }
    
    if (metadata[@"sticker"]) {
        SCSDKSnapSticker *snapSticker = [self createSnapSticker:metadata[@"sticker"]];
        if (snapSticker != nil) {
            snapContent.sticker = snapSticker;
        }
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_scSdkSnapApi startSendingContent:snapContent completionHandler:^(NSError *error) {
            
            if (error != nil) {
                NSLog(@"%@", [NSString stringWithFormat:@"%@/%@", @"Unable to share to camera preview - ", error.localizedDescription]);
                reject(@"error", error.localizedDescription, nil);
            } else {
                resolve(@YES);
            }
        }];
    });
}

RCT_EXPORT_METHOD(shareLensToCameraPreview:(nonnull NSDictionary *)lensContent
                  shareLensToCameraPreviewResolver:(RCTPromiseResolveBlock)resolve
                  shareLensToCameraPreviewRejector:(RCTPromiseRejectBlock)reject)
{
    SCSDKLensSnapContent *snap = [[SCSDKLensSnapContent alloc] initWithLensUUID: lensContent[@"lensUUID"]];
    
    if (lensContent[@"caption"]) {
        snap.caption = lensContent[@"caption"];
    }
    
    if (lensContent[@"attachmentUrl"]) {
        snap.attachmentUrl = lensContent[@"attachmentUrl"];
    }
    

    if (lensContent[@"launchData"] != [NSNull null]) {
        SCSDKLensLaunchDataBuilder *launchDataBuilder = [[SCSDKLensLaunchDataBuilder alloc] init];
        NSDictionary *launchDataMap = lensContent[@"launchData"];

        for(id key in launchDataMap) {
            [launchDataBuilder addNSStringKeyPair:key value:[launchDataMap objectForKey:key]];
        }

        snap.launchData = [[SCSDKLensLaunchData alloc] initWithBuilder:launchDataBuilder];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [self->_scSdkSnapApi startSendingContent:snap completionHandler:^(NSError *error) {
            
            if (error != nil) {
                NSLog(@"%@", [NSString stringWithFormat:@"%@/%@", @"Unable to share lens to camera preview - ", error.localizedDescription]);
                reject(@"error", error.localizedDescription, nil);
            } else {
                resolve(@YES);
            }
        }];
    });
}

@end

