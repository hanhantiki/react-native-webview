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
  return @"";
}

- (NSString *)renderFrameWorkPath {
  if (_appMeta[@"renderFrameWorkPath"]) {
    return _appMeta[@"renderFrameWorkPath"];
  }
  return @"";
}

@end
