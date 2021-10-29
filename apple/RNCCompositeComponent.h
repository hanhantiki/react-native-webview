//
//  RNTCompositeComponent.h
//  TikiMiniApp
//
//  Created by Viet Nguyen on 30/09/2021.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@class RCTBridge;

NS_ASSUME_NONNULL_BEGIN

@interface RNTCompositeComponent : UIView

- (instancetype)initWithBridge:(RCTBridge *)bridge NS_DESIGNATED_INITIALIZER;

@property (nonatomic, copy) NSNumber* webviewTag;

@end

NS_ASSUME_NONNULL_END
