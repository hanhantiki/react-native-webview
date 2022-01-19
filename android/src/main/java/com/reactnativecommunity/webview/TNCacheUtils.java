package com.reactnativecommunity.webview;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TNCacheUtils {

  protected String getFolderMD5(URL url) {
    String urlPath = url.getPath();
    String[] components = urlPath.split("/");
    if (components.length == 2 && components[0].isEmpty()) {
      components[0] = "/";
    }
    String folder = TextUtils.join("/", Arrays.copyOfRange(components, 0, components.length - 1));
    return MD5Utils.getMD5(folder).toLowerCase();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  protected WebResourceResponse loadURL(URL url, File filePath, int expiredDay)  {
    HttpURLConnection connection = null;
    InputStream inputStream = null;
    try {
      Map<String, String> headers = new HashMap<>();

      if (filePath.exists() && !this.deleteFileIfExpired(filePath, expiredDay)) {
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "agent, user-data, Access-Control-Allow-Headers, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        headers.put("X-Powered-By", "Tiniapp");
      } else {
        // create folder if it does not exists
        filePath.getParentFile().mkdirs();
        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);

        for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            headers.put(entries.getKey(), String.join(", ", entries.getValue()));
          }
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
          inputStream = connection.getInputStream();
          // read from temp stream and write to file
          FileOutputStream fileOutputStream = new FileOutputStream(filePath, false);
          byte data[] = new byte[10 * 1024];
          long total = 0;
          int count;
          while ((count = inputStream.read(data)) != -1) {
            total += count;
            fileOutputStream.write(data, 0, count);
          }
          fileOutputStream.flush();
          fileOutputStream.close();
        }
      }

      if (filePath.exists()) {
        FileInputStream fileInputStream = new FileInputStream(filePath);
        String mimeType = this.getMimeType(filePath.getAbsolutePath());
        return new WebResourceResponse(mimeType, "UTF-8", 200, "OK", headers, fileInputStream);
      }
    } catch (Exception e) {
      Log.e("RNCWebViewManager", e.toString());
    } finally {
      try {
        if (connection != null) {
          connection.disconnect();
        }
        if (inputStream != null) {
          inputStream.close();
        }
      } catch (Exception e) {
        Log.e("RNCWebViewManager", e.toString());
      }
    }
    return null;
  }

  protected boolean deleteFileIfExpired(File file, int expiredDay) {
    if (file.exists()) {
      long lastModified = file.lastModified() / 1000;
      long now = System.currentTimeMillis() / 1000;
      if (now - lastModified > expiredDay * 864000) {
        file.delete();
        return true;
      }
    }
    return false;
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

  protected String getCachePathWithCacheFolderMapping(URL url, HashMap<String, String> mapping) {
    // we ignore query string
    String path = url.getHost() + url.getPath();
    for(Map.Entry<String, String> entry : mapping.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      if (!path.startsWith(key)) {
        continue;
      }

      return path.replaceFirst(key, entry.getValue());
    }
    return null;
  }
}
