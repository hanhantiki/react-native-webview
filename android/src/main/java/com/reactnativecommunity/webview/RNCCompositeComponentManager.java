package com.reactnativecommunity.webview;

import androidx.annotation.NonNull;

import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.jetbrains.annotations.NotNull;

@ReactModule(name = RNCCompositeComponentManager.REACT_CLASS)
public class RNCCompositeComponentManager extends ViewGroupManager<RNCCompositeView> {
  private static final String TAG = "RNTCompositeComponentManager";

  protected static final String REACT_CLASS = "RNTCompositeComponent";
  protected static final String webviewTag = "";

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @NonNull
  @NotNull
  @Override
  protected RNCCompositeView createViewInstance(@NonNull @NotNull ThemedReactContext reactContext) {
    return new RNCCompositeView(reactContext);
  }

  @ReactProp(name = "webviewTag")
  public void setWebViewTag(RNCCompositeView view, int tag) {
    view.setWebViewTag(tag);
  }
}

