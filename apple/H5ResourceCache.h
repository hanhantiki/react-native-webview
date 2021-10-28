//
//  H5ResourceCache.h
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface H5ResourceCache : NSObject

- (BOOL)contain:(NSString *)key;
- (void)setData:(NSData *)data forKey:(NSString *)key;
- (NSData *)dataForKey:(NSString *)key;

@end

NS_ASSUME_NONNULL_END
