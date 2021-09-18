/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */


#import "RNCWebViewCustomFileHandler.h"
#import <MobileCoreServices/MobileCoreServices.h>

#import <objc/message.h>

@implementation RNCWebViewCustomFileHandler

- (void)webView:(WKWebView *)webView startURLSchemeTask:(id <WKURLSchemeTask>)urlSchemeTask  API_AVAILABLE(ios(11.0)){
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentPath = [paths firstObject];
  NSURL * url = urlSchemeTask.request.URL;
  NSString * stringToLoad = url.path;
  NSString * scheme = url.scheme;
  if ([scheme isEqualToString:@"miniapp-resource"]) {
    NSString * host = url.host;
    
    // handle bridge request
    if ([host isEqualToString:@"tinibridge"]) {
      NSString *method = [url.path substringFromIndex:1]; // remove /
      // convert query string to dictionary
      NSString *query = url.query;
      NSMutableDictionary *queryStringDictionary = [[NSMutableDictionary alloc] init];
      NSArray *urlComponents = [query componentsSeparatedByString:@"&"];
      for (NSString *keyValuePair in urlComponents) {
        NSArray *pairComponents = [keyValuePair componentsSeparatedByString:@"="];
        NSString *key = [[pairComponents firstObject] stringByRemovingPercentEncoding];
        NSString *value = [[pairComponents lastObject] stringByRemovingPercentEncoding];
        [queryStringDictionary setObject:value forKey:key];
      }
      // build bridge script
      NSString *args = [queryStringDictionary valueForKey:@"args"];
      NSString *requestId = [queryStringDictionary valueForKey:@"requestId"];
      NSString *script = [NSString stringWithFormat:@"window.JSBridge['%@'].apply(null, %@.concat([%@]))", method, args, requestId];
      
      __block NSString *resultString = nil;
      __block BOOL finished = NO;
      
      // recursive execute script in render until get result
      typedef void (^EvaluteJavascriptSyncBlock)(void);
      __block __weak EvaluteJavascriptSyncBlock weakEvaluateJavascriptSync = nil;
      EvaluteJavascriptSyncBlock evaluateJavascriptSync = ^ void () {
        [webView evaluateJavaScript:script completionHandler:^(id result, NSError * _Nullable error) {
          if (error == nil) {
            if (result != nil) {
              resultString = [NSString stringWithFormat:@"%@", result];
              finished = YES;
            } else if (weakEvaluateJavascriptSync) {
              weakEvaluateJavascriptSync();
            }
          }
        }];
      };
      weakEvaluateJavascriptSync = evaluateJavascriptSync;
      evaluateJavascriptSync();
      
      while (!finished) {
        [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate distantFuture]];
      }
      weakEvaluateJavascriptSync = nil;
      
      // build response on API result
      NSDictionary *headers = @{
        @"Access-Control-Allow-Origin" : @"*",
        @"Content-Type" : @"application/json"
      };
      NSURLResponse *response = [[NSHTTPURLResponse alloc] initWithURL:url statusCode:200 HTTPVersion:nil headerFields:headers];
      NSData *data = [resultString dataUsingEncoding:NSUTF8StringEncoding];
      [urlSchemeTask didReceiveResponse:response];
      [urlSchemeTask didReceiveData:data];
      [urlSchemeTask didFinish];
      return;
    } else {
      if ([stringToLoad hasPrefix:@"/resource"]) {
        documentPath = [stringToLoad stringByReplacingOccurrencesOfString:@"/resource" withString:@""];
      } else {
        documentPath = stringToLoad;
      }
    }
  }
  
  NSError * fileError = nil;
  NSData * data = nil;
  if ([self isMediaExtension:url.pathExtension]) {
    data = [NSData dataWithContentsOfFile:documentPath options:NSDataReadingMappedIfSafe error:&fileError];
  }
  if (!data || fileError) {
    data =  [[NSData alloc] initWithContentsOfFile:documentPath];
  }
  NSInteger statusCode = 200;
  if (!data) {
    statusCode = 404;
  }
  NSURL * localUrl = [NSURL URLWithString:url.absoluteString];
  NSString * mimeType = [self getMimeType:url.pathExtension];
  id response = nil;
  if (data && [self isMediaExtension:url.pathExtension]) {
    response = [[NSURLResponse alloc] initWithURL:localUrl MIMEType:mimeType expectedContentLength:data.length textEncodingName:nil];
  } else {
    NSDictionary * headers = @{ @"Content-Type" : mimeType, @"Cache-Control": @"no-cache"};
    response = [[NSHTTPURLResponse alloc] initWithURL:localUrl statusCode:statusCode HTTPVersion:nil headerFields:headers];
  }
  
  [urlSchemeTask didReceiveResponse:response];
  if (data) {
    [urlSchemeTask didReceiveData:data];
  }
  [urlSchemeTask didFinish];
}

- (void)webView:(nonnull WKWebView *)webView stopURLSchemeTask:(nonnull id<WKURLSchemeTask>)urlSchemeTask  API_AVAILABLE(ios(11.0)){
}

-(NSString *) getMimeType:(NSString *)fileExtension {
  if (fileExtension && ![fileExtension isEqualToString:@""]) {
    NSString *UTI = (__bridge_transfer NSString *)UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef)fileExtension, NULL);
    NSString *contentType = (__bridge_transfer NSString *)UTTypeCopyPreferredTagWithClass((__bridge CFStringRef)UTI, kUTTagClassMIMEType);
    return contentType ? contentType : @"application/octet-stream";
  } else {
    return @"text/html";
  }
}

-(BOOL) isMediaExtension:(NSString *) pathExtension {
  NSArray * mediaExtensions = @[@"m4v", @"mov", @"mp4",
                                @"aac", @"ac3", @"aiff", @"au", @"flac", @"m4a", @"mp3", @"wav"];
  if ([mediaExtensions containsObject:pathExtension.lowercaseString]) {
    return YES;
  }
  return NO;
}


@end
