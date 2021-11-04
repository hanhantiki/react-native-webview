//
//  NSString+Util.h
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSString (Util)

- (NSString *)stringToMD5;
- (BOOL)isJSOrCSSFile;
+ (NSString *) mimeType:(NSString *)pathExtension;

@end

NS_ASSUME_NONNULL_END
