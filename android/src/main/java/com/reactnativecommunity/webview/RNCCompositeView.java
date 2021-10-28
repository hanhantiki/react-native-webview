package com.reactnativecommunity.webview;
import android.view.View;
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

      this.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View view, View view1) {
          for (int i = 0; i < RNCCompositeView.this.getChildCount(); i++) {
            boolean isRemoved = RNCCompositeView.this.mWebView.addNativeComponent(RNCCompositeView.this.getChildAt(i));
            if (isRemoved) {
              i --;
            }
          }
        }

        @Override
        public void onChildViewRemoved(View view, View view1) {

        }
      });
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
  }
}
