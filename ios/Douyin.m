#import "Douyin.h"
#import <DouyinOpenSDK/DouyinOpenSDKAuth.h>
#import "DouyinOpenSDKShare.h"
#import<DouyinOpenSDK/DouyinOpenSDKApplicationDelegate.h>
#import<DouyinOpenSDK/DouyinOpenSDKShare.h>
#import <Photos/Photos.h>



@implementation Douyin

RCT_EXPORT_MODULE(DouYinModule)





RCT_EXPORT_METHOD(auth:(NSString *)scope
                  state:(NSString *)state
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
      
      DouyinOpenSDKAuthRequest *req = [[DouyinOpenSDKAuthRequest alloc] init];
      
      req.permissions = [NSOrderedSet orderedSetWithObject:scope];
     // req.state=state;
      
      UIViewController *vc =  [UIApplication sharedApplication].keyWindow.rootViewController;

     [req sendAuthRequestViewController:vc completeBlock:^(DouyinOpenSDKAuthResponse * _Nonnull resp) {
         if (resp.errCode == 0) {
             NSDictionary *data = @{
                 @"authCode": resp.code,
                 @"state": resp.state
             };
             resolve(@{
                @"code": [NSNumber numberWithInt:(int)resp.errCode],
                @"msg": resp.errString,
                @"data": data
             });
            } else{
                [NSString stringWithFormat:@"Author failed code : %@, msg : %@",@(resp.errCode), resp.errString];
                reject([NSString stringWithFormat:@"%@",@(resp.errCode)],resp.errString,nil);
            }
        }];
   
  });
}


RCT_EXPORT_METHOD(init:(NSString *)appid
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
  dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
      
     [[DouyinOpenSDKApplicationDelegate sharedInstance] registerAppId:appid];
      [DouyinOpenSDKApplicationDelegate sharedInstance].logDelegate =self;
  });
}

RCT_EXPORT_METHOD(isAppInstalled:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    BOOL installed = [[DouyinOpenSDKApplicationDelegate sharedInstance] isAppInstalled];
    
    NSDictionary *dic = @{
        @"code": @0,
        @"data": installed ? @YES : @NO
    };
    resolve(dic);
}

RCT_EXPORT_METHOD(shareVideo:(NSDictionary*) config
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    
    NSArray* filePaths = [config valueForKey:@"videos"];
    
    BOOL isPublish = config[@"isPublish"];
    NSString* shortTitle = config[@"shortTitle"];
    NSString* title = config[@"title"];
    
    DouyinOpenSDKShareRequest *req = [[DouyinOpenSDKShareRequest alloc] init];
    req.mediaType = DouyinOpenSDKShareMediaTypeVideo;
    req.landedPageType = isPublish ? DouyinOpenSDKLandedPagePublish : DouyinOpenSDKLandedPageEdit;
    req.publishStory = NO;
    
    req.title = [DouyinOpenSDKShareTitle new];
    req.title.text = title;
    req.title.shortTitle = shortTitle;
    
    [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
        if (status == PHAuthorizationStatusAuthorized) {
            [[PHPhotoLibrary sharedPhotoLibrary]performChanges:^{
                NSMutableArray *assetLocalIds = [NSMutableArray new];
                for (NSString* item in filePaths) {
                    NSURL *url = [NSURL URLWithString:filePaths.firstObject]; // file://
                    PHAssetChangeRequest *request = [PHAssetChangeRequest creationRequestForAssetFromVideoAtFileURL:url];
                    NSString *localId = request.placeholderForCreatedAsset.localIdentifier;
                    [assetLocalIds addObject:localId];
                }
                req.localIdentifiers = assetLocalIds;
            } completionHandler:^(BOOL success, NSError * _Nullable error) {
                if(success) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [req sendShareRequestWithCompleteBlock:^(DouyinOpenSDKShareResponse * _Nonnull Response) {
                            NSDictionary *dic = @{
                                @"code": [NSNumber numberWithInt:(int)Response.errCode],
                                @"msg": [NSString stringWithFormat:@"%@, subCode:%ld", Response.errString, (long)Response.subErrorCode],
                            };
                            resolve(dic);
                        }];
                    });
                } else {
                    NSDictionary *dic = @{
                        @"code": @-98,
                        @"msg": @"处理相册文件失败",
                    };
                    resolve(dic);
                }
                
            }];
        } else {
            NSDictionary *dic = @{
                @"code": @-99,
                @"msg": @"获取相册权限失败",
            };
            resolve(dic);
        }
    }];
    
    
}

#pragma mark - DouyinOpenSDKLogDelegate Delegate
- (void)onLog:(NSString *)logInfo
{
    NSLog(@"RCTDouYinLOG: %@", logInfo);
}


@end
