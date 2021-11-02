//
//  RNTCompositeComponent.m
//  TikiMiniApp
//
//  Created by Viet Nguyen on 30/09/2021.
//

#import "RNTCompositeComponent.h"
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import <React/UIView+React.h>
#import "RNCWebView.h"

@implementation RNTCompositeComponent {
  __weak RCTBridge *_bridge;
}

RCT_NOT_IMPLEMENTED(-(instancetype)initWithFrame : (CGRect)frame)
RCT_NOT_IMPLEMENTED(-(instancetype)initWithCoder : coder)

- (instancetype)initWithBridge:(RCTBridge *)bridge {
  if ((self = [super initWithFrame:CGRectZero])) {
     _bridge = bridge;
   }
   return self;
}

- (void)didUpdateReactSubviews {
  RCTUIManager* uiManager = [_bridge moduleForClass:[RCTUIManager class]];
  RNCWebView* webView = (RNCWebView*)[uiManager viewForReactTag:self.webviewTag];
  for (NSUInteger i = 0; i < self.reactSubviews.count; i++) {
    UIView *subview = [self.reactSubviews objectAtIndex:i];
    [webView addNativeComponent:subview];
  }
}

@end
