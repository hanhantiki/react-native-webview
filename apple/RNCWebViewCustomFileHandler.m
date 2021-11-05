/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */


#import "RNCWebViewCustomFileHandler.h"
#import <React/RCTMultipartDataTask.h>
#import <React/RCTJavaScriptLoader.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import "TNAppDataSource.h"
#import "NSString+MD5.h"
#import <objc/message.h>

@interface RNCWebViewCustomFileHandler ()

@property (nonatomic, strong) NSMutableDictionary *holdUrlSchemeTasks;
@property (nonatomic, strong) TNAppDataSource *appDataSource;

@end

@implementation RNCWebViewCustomFileHandler

- (instancetype)initWithDataSource:(TNAppDataSource *)dataSource
{
  self = [super init];
  if (self) {
    self.holdUrlSchemeTasks = [[NSMutableDictionary alloc] init];
    _appDataSource = dataSource;
  }
  return self;
}

- (void)webView:(WKWebView *)webView startURLSchemeTask:(id <WKURLSchemeTask>)urlSchemeTask  API_AVAILABLE(ios(11.0)){
  [self.holdUrlSchemeTasks setObject:@(YES) forKey:urlSchemeTask.description];
  NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
  NSString *documentPath = [paths firstObject];
  NSURL * url = urlSchemeTask.request.URL;
  NSString * stringToLoad = url.path;
  NSString * scheme = url.scheme;
  if ([scheme isEqualToString:@"miniapp-resource"]) {
    NSString * host = url.host;
    NSString *path = url.path;
    
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
      NSDate * start = [[NSDate alloc] init];
      typedef void (^EvaluteJavascriptSyncBlock)(void);
      __block __weak EvaluteJavascriptSyncBlock weakEvaluateJavascriptSync = nil;
      EvaluteJavascriptSyncBlock evaluateJavascriptSync = ^ void () {
        [webView evaluateJavaScript:script completionHandler:^(id result, NSError * _Nullable error) {
          if (error == nil) {
            if (result != nil) {
              resultString = [NSString stringWithFormat:@"%@", result];
              finished = YES;
            } else if (weakEvaluateJavascriptSync) {
                NSDate * now = [[NSDate alloc] init];
                if ([now timeIntervalSinceDate:start] > 5) {
                    //timeout
                    finished = YES;
                } else {
                    weakEvaluateJavascriptSync();
                }
            }
          } else {
            finished = YES;
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
    } else if (path) {
      NSString *documentDir = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject];
      NSArray *components = [path pathComponents];
      NSString *folder = [NSString pathWithComponents:[components subarrayWithRange:(NSRange){ 0, components.count - 1}]];
      NSString *folderHash = [folder MD5Hash];
      NSString *fileName = [components lastObject];
      NSString *fragment = url.fragment;
      bool disableCache = [fragment containsString:@"NOCACHE"];
      
      if (([host hasSuffix:@".tikicdn.com"] || [host hasSuffix:@".tiki.vn"] || [host hasSuffix:@".tala.xyz"])) {
        NSString *cacheFilePath = [NSString stringWithFormat:@"%@/tiki-miniapp/apps/%@/%@", documentDir, folderHash, fileName];
        [self loadURL:url localFile:cacheFilePath urlSchemeTask:urlSchemeTask disableCache:disableCache];
        return;
      } else if ([host isEqualToString:@"framework"]) {
        // final url is remote url of framework files. URL can be:
        // http://localhost:8080/tf-tiniapp.render.js
        // https://tiniapp-dev.tikicdn.com/tiniapps/framework_files/1.81.18/worker_files/tf-tiniapp.render.js
        // https://tiniapp-dev.tikicdn.com/tiniapps/framework_files/1.81.18/worker_files/tf-tiniapp.render.js#NOCACHE
        NSURL *frameworkUrl;
        if ([fileName hasPrefix:@"tf-tiniapp.render.js"] || [fileName hasPrefix:@"tf-miniapp.render.js"]) {
          frameworkUrl = [[NSURL alloc] initWithString:_appDataSource.renderFrameWorkPath];
        } else if ([fileName hasPrefix:@"tf-tiniapp.worker.js"] || [fileName hasPrefix:@"tf-miniapp.worker.js"]) {
          frameworkUrl = [[NSURL alloc] initWithString:_appDataSource.workerFrameworkPath];
        }
        
        if (frameworkUrl) {
          NSString *cacheFilePath = [NSString stringWithFormat:@"%@/tiki-miniapp/frameworks/%@/%@", documentDir, folderHash, fileName];
          [self loadURL:frameworkUrl localFile:cacheFilePath urlSchemeTask: urlSchemeTask disableCache:disableCache];
        }
        return;
      }
    } else if ([stringToLoad hasPrefix:@"/resource"]) {
      documentPath = [stringToLoad stringByReplacingOccurrencesOfString:@"/resource" withString:@""];
    } else {
      documentPath = stringToLoad;
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
  [self.holdUrlSchemeTasks setObject:@(NO) forKey:urlSchemeTask.description];
}

- (void)loadURL:(NSURL *)url localFile:(NSString *)filePath urlSchemeTask:(id <WKURLSchemeTask>)urlSchemeTask disableCache:(bool)disableCache API_AVAILABLE(ios(11.0)){
    if (!urlSchemeTask) {
      return;
    }
  
    if (disableCache || ![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
      [self requestRemoteURL:url urlSchemeTask:urlSchemeTask filePath:filePath];
    } else if (filePath) {
      NSData *data = [NSData dataWithContentsOfFile:filePath options:NSDataReadingMappedIfSafe error:nil];
      if (!data) {
        return;
      }
      [self resendRequestWithUrlSchemeTask:urlSchemeTask mimeType:[self getMimeTypeWithFilePath:filePath] requestData:data];
    }
}

- (void)requestRemoteURL:(NSURL *)url urlSchemeTask:(id <WKURLSchemeTask>)urlSchemeTask filePath:(NSString *)filePath API_AVAILABLE(ios(11.0)) {
    if (![self.holdUrlSchemeTasks objectForKey:urlSchemeTask.description]) {
      return;
    }
    NSString *requestUrl = url.absoluteString;
    NSString *replacedStr = [requestUrl stringByReplacingOccurrencesOfString:@"miniapp-resource" withString:@"https"];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:replacedStr]];
  
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.timeoutIntervalForRequest = 15;
    configuration.allowsCellularAccess = YES;
    configuration.HTTPAdditionalHeaders = @{@"Accept": @"text/html,application/json,text/json,text/javascript,text/plain,application/javascript,text/css,image/svg+xml,application/font-woff2,font/woff2,application/octet-stream",
                                            @"Accept-Language": @"en"};
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        // response the request
        [urlSchemeTask didReceiveResponse:response];
        [urlSchemeTask didReceiveData:data];
        // save content to disk
        if (error) {
            [urlSchemeTask didFailWithError:error];
        } else if (filePath != nil) {
            NSArray *components = [filePath pathComponents];
            NSString *folder = [NSString pathWithComponents:[components subarrayWithRange:(NSRange){ 0, components.count - 1}]];
            // if the directory does not exist, create it...
            if ([[NSFileManager defaultManager] createDirectoryAtPath:folder withIntermediateDirectories:YES attributes:nil error:&error] == NO) {
                NSLog(@"createDirectoryAtPath failed %@", error);
            }
            [data writeToFile:filePath atomically:YES];
            [urlSchemeTask didFinish];
        } else {
            [urlSchemeTask didFinish];
        }
    }];

    [dataTask resume];
    [session finishTasksAndInvalidate];
}

- (void)resendRequestWithUrlSchemeTask:(id <WKURLSchemeTask>)urlSchemeTask
                              mimeType:(NSString *)mimeType
                           requestData:(NSData *)requestData  API_AVAILABLE(ios(11.0)) {
    if (!urlSchemeTask || !urlSchemeTask.request || !urlSchemeTask.request.URL) {
        return;
    }
    if (!self.holdUrlSchemeTasks[urlSchemeTask.description]) {
      return;
    }

    NSString *mimeType_local = mimeType ? mimeType : @"text/html";
    NSData *data = requestData ? requestData : [NSData data];
    NSDictionary *headers = @{
      @"Access-Control-Allow-Origin": @"*",
      @"Access-Control-Allow-Methods": @"GET, POST, DELETE, PUT, OPTIONS",
      @"Access-Control-Allow-Headers": @"agent, user-data, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers",
      @"Content-Type": [mimeType_local stringByAppendingString:@"; charset=UTF-8"],
      @"X-Powered-By": @"Tiniapp"
    };
    NSHTTPURLResponse *response = [[NSHTTPURLResponse alloc]
                                   initWithURL:urlSchemeTask.request.URL
                                   statusCode:200
                                   HTTPVersion:@"HTTP/1.1"
                                   headerFields:headers];
    [urlSchemeTask didReceiveResponse:response];
    [urlSchemeTask didReceiveData:data];
    [urlSchemeTask didFinish];
}

#pragma mark - private

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

- (NSString *)getMimeTypeWithFilePath:(NSString *)filePath
{
    CFStringRef pathExtension = (__bridge_retained CFStringRef)[filePath pathExtension];
    CFStringRef type = UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, pathExtension, NULL);
    CFRelease(pathExtension);

    //The UTI can be converted to a mime type:
    NSString *mimeType = (__bridge_transfer NSString *)UTTypeCopyPreferredTagWithClass(type, kUTTagClassMIMEType);
    if (type != NULL) {
        CFRelease(type);
    }
    return mimeType;
}

@end
