//
//  NSString+Util.m
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import "NSString+Util.h"
#import <CommonCrypto/CommonCrypto.h>
#import <CoreServices/CoreServices.h>

@implementation NSString (Util)

- (NSString *)stringToMD5
{
    const char *fooData = [self UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];

    CC_MD5(fooData, (CC_LONG)strlen(fooData), result);
    NSMutableString *saveResult = [NSMutableString string];
    for (int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [saveResult appendFormat:@"%02x", result[i]];
    }
    return saveResult;
}

- (BOOL)isJSOrCSSFile {
    if (self.length == 0) {
        return NO;
    }
    NSString * pattern = @"\\.(js|css)";
    
    NSRegularExpression * result = [[NSRegularExpression alloc] initWithPattern:pattern options:NSRegularExpressionCaseInsensitive error:nil];
    
    NSArray * array = [result matchesInString:self options:0 range:NSMakeRange(0, self.length)];
    
    if (array.count > 0) {
        return YES;
    } else {
        return NO;
    }
}

+ (NSString *) mimeType:(NSString *)pathExtension {
    if (!pathExtension) {
        return @"application/octet-stream";
    }
    NSString * uti = (__bridge NSString *)(UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef _Nonnull)(pathExtension), nil));
    if (uti) {
        NSString * mimetype = (__bridge NSString *)(UTTypeCopyPreferredTagWithClass((__bridge CFStringRef _Nonnull)(uti), kUTTagClassMIMEType));
        return mimetype;
    } else {
        return @"application/octet-stream";
    }
}

@end
