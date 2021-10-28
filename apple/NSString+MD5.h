//
//  NSString+MD5.h
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonDigest.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSString (MD5)

// MD5 hash of the file on the filesystem specified by path
+ (NSString *) stringWithMD5OfFile: (NSString *) path;
// The string's MD5 hash
- (NSString *) MD5Hash;

@end

NS_ASSUME_NONNULL_END
