//
//  CacheProtocol.h
//  Pods
//
//  Created by Viet Nguyen on 27/10/2021.
//

@protocol CacheProtocol <NSObject>

// Total number of caches
- (uint)totalCount;

// Total cache size
- (uint)totalCost;

// Does the cache exist?
- (BOOL)contain:(NSString *)key;

// Return the cache of the specified key
- (id)object:(NSString *)key;

// Set cache k, v, c
- (void)setObject:(id)object forKey:(NSString *)key cost:(uint)g;

// Delete the cache of the specified key
- (void)removeObject:(NSString *)key;

// delete all caches
- (void)removeAllObject;

// Clean up according to cache size
- (void)trimWithCost:(uint)cost;

// Clean up according to the number of caches
- (void)trimWithCount:(uint)count;

// Clean up according to the cache duration
- (void)trimWithAge:(uint)age;

@end
