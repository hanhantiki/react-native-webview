//
//  RNCompositeComponentManager.m
//  TikiMiniApp
//
//  Created by Viet Nguyen on 30/09/2021.
//

#import "RNTCompositeComponentManager.h"
#import "RNTCompositeComponent.h"


@implementation RNTCompositeComponentManager

RCT_EXPORT_VIEW_PROPERTY(webviewTag, NSNumber)

RCT_EXPORT_MODULE(RNTCompositeComponent)
- (UIView *)view {
  return [[RNTCompositeComponent alloc] initWithBridge:self.bridge];
}

@end
