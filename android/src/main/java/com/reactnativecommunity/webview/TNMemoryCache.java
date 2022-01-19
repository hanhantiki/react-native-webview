package com.reactnativecommunity.webview;

import android.os.Build;
import android.util.Log;
import android.util.LruCache;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TNMemoryCache {
  // TODO: how to determine better cache size
  final static int CACHE_SIZE = 10 * 1024 * 1024; // 5MB
  final static int BUFFER_SIZE = 10 * 1024; // 10KB
  static TNMemoryCache instance = null;

  static TNMemoryCache getInstance() {
    if (instance == null) {
      instance = new TNMemoryCache();
    }
    return instance;
  }

  final LruCache<String, String> cache;

  private TNMemoryCache() {
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
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      StringBuilder builder = new StringBuilder();
      byte[] buffer = new byte[BUFFER_SIZE];
      int length = 0;
      while ((length = inputStream.read(buffer)) != -1) {
        byteStream.write(buffer, 0, length);
      }
      return byteStream.toString();
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
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

      // read from temp stream and write to file
      outputStream = new FileOutputStream(filePath, false);
      byte buffer [] = new byte[BUFFER_SIZE];
      int count;
      while ((count = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, count);
        byteStream.write(buffer, 0, count);
      }
      outputStream.flush();
      return byteStream.toString();
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
    boolean isJSFile = cacheFilePath.endsWith(".js");

    if (isJSFile && cache.get(cacheFilePath) != null) {
      return new ByteArrayInputStream(cache.get(cacheFilePath).getBytes(Charset.forName("UTF-8")));
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

    if (isJSFile) {
      synchronized (cache) {
        cache.put(cacheFilePath, content);
      }
    }

    return new ByteArrayInputStream(content.getBytes());
  }

  protected String getMimeType(String url) {
    String extension = MimeTypeMap.getFileExtensionFromUrl(url);
    if (extension != null) {
      String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
      if (type != null) {
        return type;
      }
    }
    return "text/html";
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public WebResourceResponse getWebResourceResponse(URL url, String cacheFilePath, int expiredDay) {
    InputStream inputStream = getInputStream(url, cacheFilePath, expiredDay);
    if (inputStream == null) {
      return null;
    }

    Map<String, String> headers = new HashMap<>();
    headers.put("Access-Control-Allow-Origin", "*");
    headers.put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
    headers.put("Access-Control-Allow-Headers", "agent, user-data, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
    headers.put("X-Powered-By", "Tiniapp");
    return new WebResourceResponse(getMimeType(url.toString()), "UTF-8", 200, "OK", headers, inputStream);
  }
}

