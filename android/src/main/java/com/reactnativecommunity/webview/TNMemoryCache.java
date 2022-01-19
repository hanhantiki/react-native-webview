package com.reactnativecommunity.webview;

import android.util.Log;
import android.util.LruCache;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class TNMemoryCache {
  final static int CACHE_SIZE = 10 * 1024 * 1024; // 5MB
  final static int BUFFER_SIZE = 10 * 1024; // 10KB
  final LruCache<String, String> cache;

  public TNMemoryCache() {
    cache = new LruCache<String, String>(CACHE_SIZE) {
      protected int sizeOf(String key, String value) {
        return value.length();
      }
    };
  }

  protected boolean isExpired(File file, int expiredDay) {
    long lastModified = file.lastModified() / 1000;
    long now = System.currentTimeMillis() / 1000;
    return now - lastModified > expiredDay * 864000;
  }


  private String readFileToString(File file, int expiredDay) {
    // in this function, we already make sure filePath is exist
    if (isExpired(file, expiredDay)) {
      file.delete();
      return null;
    }

    try {
      FileInputStream inputStream = new FileInputStream(file.getPath());
      StringBuilder builder = new StringBuilder();
      byte[] data = new byte[BUFFER_SIZE];
      while (inputStream.read(data) != -1) {
        builder.append(Arrays.toString(data));
      }
      return builder.toString();
    } catch(Exception e) {
      return null;
    }
  }

  private String downloadAndReturnContentFromUrl(URL url, String filePath) {
    HttpURLConnection connection = null;
    InputStream inputStream = null;
    FileOutputStream outputStream = null;

    try {
      File file = new File(filePath);
      // create folder if it does not exists
      file.getParentFile().mkdirs();
      connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);

      int responseCode = connection.getResponseCode();
      // skip if we could not download file
      if (responseCode != HttpURLConnection.HTTP_OK) {
        return null;
      }

      inputStream = connection.getInputStream();
      StringBuilder builder = new StringBuilder();

      // read from temp stream and write to file
      outputStream = new FileOutputStream(filePath, false);
      byte data[] = new byte[BUFFER_SIZE];
      int count;
      while ((count = inputStream.read(data)) != -1) {
        outputStream.write(data, 0, count);
        builder.append(Arrays.toString(data));
      }
      outputStream.flush();
      return builder.toString();
    } catch (Exception e) {
      return null;
    } finally {
      try {
        if (connection != null) {
          connection.disconnect();
        }
        if (inputStream != null) {
          inputStream.close();
        }
        if (outputStream != null) {
          outputStream.close();
        }
      } catch(Exception e) {
        Log.e("RNCWebViewManager", e.toString());
      }
    }
  }

  // TODO: how to handle race condition when read from cache parallel
  public InputStream getInputStream(URL url, String cacheFilePath, int expiredDay) {
    if (cache.get(cacheFilePath) != null) {
      return new ByteArrayInputStream(cache.get(cacheFilePath).getBytes());
    }

    String content = null;
    File file = new File(cacheFilePath);
    if (file.exists()) {
      content = readFileToString(file, expiredDay);
    } else {
      content = downloadAndReturnContentFromUrl(url, cacheFilePath);
    }

    // skip if we could not get content because of failed download
    if (content == null) {
      return null;
    }

    synchronized (cache) {
      cache.put(cacheFilePath, content);
    }

    return new ByteArrayInputStream(content.getBytes());
  }

  public WebResourceResponse getWebResourceResponse(URL url, String cacheFilePath, int expiredDay) {
    InputStream inputStream = getInputStream(url, cacheFilePath, expiredDay);
    if (inputStream == null) {
      return null;
    }
    return new WebResourceResponse(url.toString(), "utf8", inputStream);
  }
}

