package com.reactnativecommunity.webview;

import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TNAppDataSource {

  private ReadableMap appMeta;

  TNAppDataSource() {}
  TNAppDataSource(ReadableMap appMeta) {
    this.appMeta = appMeta;
  }

  public ReadableMap getAppMeta() {
    return appMeta;
  }

  public void setAppMeta(ReadableMap appMeta) {
    this.appMeta = appMeta;
  }

  public String getRenderFrameWorkPath() {
    if (this.appMeta != null && this.appMeta.hasKey("renderFrameWorkPath")) {
      return this.appMeta.getString("renderFrameWorkPath");
    }
    return null;
  }

  public String getWorkerFrameworkPath() {
    if (this.appMeta != null && this.appMeta.hasKey("workerFrameWorkPath")) {
      return this.appMeta.getString("workerFrameWorkPath");
    }
    return null;
  }

  public String getStylesFrameworkPath() {
    if (this.appMeta != null && this.appMeta.hasKey("stylesFrameWorkPath")) {
      return this.appMeta.getString("stylesFrameWorkPath");
    }
    return null;
  }

  public int snapshotExpiredDay() {
    if (this.appMeta != null && this.appMeta.hasKey("snapshotExpiredDay")) {
      return this.appMeta.getInt("snapshotExpiredDay");
    }
    return 0;
  }

  public int cacheExpiredDay() {
    if (this.appMeta != null && this.appMeta.hasKey("cacheExpiredDay")) {
      return this.appMeta.getInt("cacheExpiredDay");
    }
    return 0;
  }

  public ReadableMap getLaunchParams() {
    if (this.appMeta != null && this.appMeta.hasKey("launchParams")) {
      return this.appMeta.getMap("launchParams");
    }
    return null;
  }

  public String indexHtmlSnapshotFile() {
    if (this.appMeta != null && this.appMeta.hasKey("indexHtmlSnapshotFile")) {
      return this.appMeta.getString("indexHtmlSnapshotFile");
    }
    return null;
  }

  public HashMap<String, String> cacheFolderMapping() {
    if (this.appMeta == null || !this.appMeta.hasKey("cacheFolderMapping")) {
      return null;
    }

    HashMap<String, String> result = new HashMap<>();
    ReadableMap value = this.appMeta.getMap("cacheFolderMapping");
    Iterator<Map.Entry<String, Object>> iterator = value.getEntryIterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      result.put(entry.getKey(), entry.getValue().toString());
    }
    return result;
  }
}
