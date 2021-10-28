//
//  DiskFileCache.h
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import <Foundation/Foundation.h>
#import "CacheProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@interface DiskFileCache : NSObject<CacheProtocol>

- (instancetype)initWithCacheDirectoryName:(NSString *)directoryName;

@property (nonatomic,assign) uint costLimit;

@property (nonatomic,assign) uint ageLimit;

@end

NS_ASSUME_NONNULL_END
