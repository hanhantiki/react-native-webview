//
//  TNAppDataSource.h
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface TNAppDataSource : NSObject

- (instancetype)initWithAppMeta:(NSDictionary *)appMeta;

@property (readonly) NSString * _Nullable renderFrameWorkPath;
@property (readonly) NSString * _Nullable workerFrameWorkPath;
@property (readonly) NSString * _Nullable stylesFrameWorkPath;
@property (readonly) NSString * _Nullable indexHtmlSnapshotFile;
@property (readonly) int snapshotExpiredDay;
@property (readonly) int cacheExpiredDay;

@end

NS_ASSUME_NONNULL_END
