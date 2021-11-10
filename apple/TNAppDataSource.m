//
//  TNAppDataSource.m
//  react-native-webview
//
//  Created by Viet Nguyen on 27/10/2021.
//

#import "TNAppDataSource.h"

@implementation TNAppDataSource {
  NSDictionary *_appMeta;
  NSString *_frameworkReplacementPath;
}

- (instancetype)initWithAppMeta:(NSDictionary *)appMeta
{
  self = [super init];
  if (self) {
    _appMeta = appMeta;
  }
  return self;
}

- (NSString *)workerFrameworkPath {
  if (_appMeta[@"workerFrameWorkPath"]) {
    return _appMeta[@"workerFrameWorkPath"];
  }
  return nil;
}

- (NSString *)renderFrameWorkPath {
  if (_appMeta[@"renderFrameWorkPath"]) {
    return _appMeta[@"renderFrameWorkPath"];
  }
  return nil;
}

- (NSString * _Nullable)indexHtmlSnapshotFile {
  if (_appMeta[@"indexHtmlSnapshotFile"]) {
    return _appMeta[@"indexHtmlSnapshotFile"];
  }
  return nil;
}

- (int)snapshotExpiredDay {
  if (_appMeta[@"snapshotExpiredDay"]) {
    return [_appMeta[@"snapshotExpiredDay"] intValue];
  }
  return 0;
}

- (int)cacheExpiredDay {
  if (_appMeta[@"cacheExpiredDay"]) {
    return [_appMeta[@"cacheExpiredDay"] intValue];
  }
  return 0;
}

@end