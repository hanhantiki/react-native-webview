package com.reactnativecommunity.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.views.view.ReactViewGroup;

class RNCCompositeView extends ReactViewGroup {
  private static final String TAG = "RNCCompositeView";

  private ReactContext mContext = null;

  private RNCWebViewManager.RNCWebView mWebView = null;

  public RNCCompositeView(ReactContext context) {
    super(context);
    this.mContext = context;
  }

  public void setWebViewTag(int tag) {
    UIManagerModule uiManager = mContext.getNativeModule(UIManagerModule.class);
    RNCWebViewManager.RNCWebView view = (RNCWebViewManager.RNCWebView) uiManager.resolveView(tag);
    if (view != null && view instanceof RNCWebViewManager.RNCWebView) {
      this.mWebView = view;
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    if (changed) {
      if (this.mWebView != null) {
        for (int i = 0; i < this.getChildCount(); i++) {
          boolean isRemoved = this.mWebView.addNativeComponent(this.getChildAt(i));
          if (isRemoved) {
            i --;
          }
        }
      }
    }
  }
}
