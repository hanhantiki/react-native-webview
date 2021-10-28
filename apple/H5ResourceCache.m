//
//  H5ResourceCache.m
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import "H5ResourceCache.h"
#import "MemoryCache.h"
#import "DiskFileCache.h"
#import "NSString+Util.h"

@interface H5ResourceCache () {
  // Memory cache size: 10M
  uint kMemoryCacheCostLimit;
  // Disk file cache size: 10M
  uint kDiskCacheCostLimit;
  // Disk file cache duration: 30 minutes
  NSTimeInterval kDiskCacheAgeLimit;
}

@property (nonatomic,strong) MemoryCache * memoryCache;
@property (nonatomic,strong) DiskFileCache * diskCache;

@end

@implementation H5ResourceCache

- (instancetype) init {
    self = [super init];
    if (self) {
        kMemoryCacheCostLimit = 10 * 1024 * 1024;
        kDiskCacheCostLimit = 10 * 1024 * 1024;
        kDiskCacheAgeLimit = 30 * 60;
        
        self.memoryCache = [MemoryCache shared];
        self.memoryCache.costLimit = kMemoryCacheCostLimit;
        
        self.diskCache = [[DiskFileCache alloc]initWithCacheDirectoryName:@"H5ResourceCache"];
        self.diskCache.costLimit = kDiskCacheCostLimit;
        self.diskCache.ageLimit = kDiskCacheAgeLimit;
    }
    return self;
}

- (BOOL)contain:(NSString *)key {
    return [self.memoryCache contain:key] || [self.diskCache contain:key];
}

- (void)setData:(NSData *)data forKey:(NSString *)key {
    NSString * dataString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    [self.memoryCache setObject:[dataString dataUsingEncoding:NSUTF8StringEncoding] forKey:key cost:(uint)[data length]];
    
    [self.diskCache setObject:[dataString dataUsingEncoding:NSUTF8StringEncoding] forKey:key cost:(uint)[data length]];
}

- (NSData *)dataForKey:(NSString *)key {
    NSData * data = [self.memoryCache object:key];
    if (data) {
        NSLog(@"From memory cache");
        return data;
    } else {
        NSData * data = [self.diskCache object:key];
        if (!data) {
            return nil;
        }
        [self.memoryCache setObject:data forKey:key cost:(uint)[data length]];
        NSLog(@"From disk cache");
        return data;
    }
}

- (void)removeDataForkey:(NSString *)key {
    [self.memoryCache removeObject:key];
    [self.diskCache removeObject:key];
}

- (void)removeAll {
    [self.memoryCache removeAllObject];
    [self.diskCache removeAllObject];
}

@end
