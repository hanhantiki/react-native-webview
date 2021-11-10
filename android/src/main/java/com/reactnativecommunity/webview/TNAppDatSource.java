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
    return this.appMeta.getString("renderFrameWorkPath");
  }

  public String getWorkerFrameworkPath() {
    return this.appMeta.getString("workerFrameWorkPath");
  }

  public int snapshotExpiredDay() {
    return this.appMeta.getInt("snapshotExpiredDay");
  }

  public int cacheExpiredDay() {
    return this.appMeta.getInt("cacheExpiredDay");
  }

  public String indexHtmlSnapshotFile() {
    return this.appMeta.getString("indexHtmlSnapshotFile");
  }
}