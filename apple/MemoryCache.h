//
//  MemoryCache.h
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import <Foundation/Foundation.h>
#import "CacheProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@interface LinkedNode : NSObject

@property (nonatomic, strong) LinkedNode * prev;

@property (nonatomic, strong) LinkedNode * next;

@property (nonatomic, strong) NSString * key;

@property (nonatomic, strong) id value;

@property (nonatomic, assign) uint cost;

@property (nonatomic, assign) NSTimeInterval time;

@end

@interface LinkedNodeMap : NSObject

@property (nonatomic, strong) NSMutableDictionary * dict;

@property (nonatomic, assign) uint totalCost;

@property (nonatomic, assign) uint totalCount;

@property (nonatomic, strong) LinkedNode * head;

@property (nonatomic, strong) LinkedNode * tail;

@end

@interface MemoryCache : NSObject <CacheProtocol>

+(instancetype)shared;

@property (nonatomic,assign) uint costLimit;

@end

NS_ASSUME_NONNULL_END
