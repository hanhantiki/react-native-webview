package com.reactnativecommunity.webview;

import com.facebook.react.bridge.ReadableMap;

public class TNAppDatSource {

  private ReadableMap appMeta;

  TNAppDatSource(ReadableMap appMeta) {
    this.appMeta = appMeta;
  }

  public ReadableMap getAppMeta() {
    return appMeta;
  }

  public String getRenderFrameWorkPath() {
    if (this.appMeta.hasKey("renderFrameWorkPath")) {
      return this.appMeta.getString("renderFrameWorkPath");
    }
    return null;
  }

  public String getWorkerFrameworkPath() {
    if (this.appMeta.hasKey("workerFrameWorkPath")) {
      return this.appMeta.getString("workerFrameWorkPath");
    }
    return null;
  }

  public String getStylesFrameworkPath() {
    if (this.appMeta.hasKey("stylesFrameWorkPath")) {
      return this.appMeta.getString("stylesFrameWorkPath");
    }
    return null;
  }

  public int snapshotExpiredDay() {
    if (this.appMeta.hasKey("snapshotExpiredDay")) {
      return this.appMeta.getInt("snapshotExpiredDay");
    }
    return 0;
  }

  public int cacheExpiredDay() {
    if (this.appMeta.hasKey("cacheExpiredDay")) {
      return this.appMeta.getInt("cacheExpiredDay");
    }
    return 0;
  }

  public ReadableMap getLaunchParams() {
    if (this.appMeta.hasKey("launchParams")) {
      return this.appMeta.getMap("launchParams");
    }
    return null;
  }

  public String indexHtmlSnapshotFile() {
    if (this.appMeta.hasKey("indexHtmlSnapshotFile")) {
      return this.appMeta.getString("indexHtmlSnapshotFile");
    }
    return null;
  }
}
