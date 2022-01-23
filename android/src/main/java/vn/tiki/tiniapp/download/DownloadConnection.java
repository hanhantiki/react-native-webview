package vn.tiki.tiniapp.download;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import vn.tiki.tiniapp.TNConstants;

public class DownloadConnection {
    private final String TAG = "Tini.DownloadConnection";

    // TODO: how to reuse connection
    final URLConnection connImpl;
    private final String url;
    private BufferedInputStream responseStream;

    public DownloadConnection(String url) {
        this.url = url;
        connImpl = createConnection();
    }

    private URLConnection createConnection() {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        URLConnection newConn = null;
        try {
            URL currentUrl = new URL(url);
            newConn = currentUrl.openConnection();
            connImpl.setConnectTimeout(5000);
            connImpl.setReadTimeout(15000);
        } catch(Throwable e) {
            if (newConn != null) {
                newConn = null;
            }
            Log.e(TAG, "create connection fail, error: " + e.getMessage() + ", url: " + this.url);
        }

        return newConn;
    }

    synchronized int connect() {
        if (connImpl instanceof HttpURLConnection) {
            HttpURLConnection httpURLConnection = (HttpURLConnection) connImpl;
            try {
                httpURLConnection.connect();
                return TNConstants.ERROR_CODE_SUCCESS;
            } catch (IOException e) {
                return TNConstants.ERROR_CODE_CONNECT_IOE;
            }
        }
        return TNConstants.ERROR_CODE_UNKNOWN;
    }

    public void disconnect() {
        if (connImpl instanceof HttpURLConnection) {
            final HttpURLConnection httpURLConnection = (HttpURLConnection) connImpl;
            try {
                httpURLConnection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "disconnect error:" + e.getMessage());
            }
        }
    }

    public BufferedInputStream getResponseStream() {
        if (null == responseStream && null != connImpl) {
            try {
                InputStream inputStream = connImpl.getInputStream();
                responseStream = new BufferedInputStream(inputStream);
            } catch (Throwable e) {
                Log.e(TAG, "getResponseStream error:" + e.getMessage() + ".");
            }
        }
        return responseStream;
    }

    public int getResponseCode() {
        if (connImpl instanceof HttpURLConnection) {
            try {
                return ((HttpURLConnection) connImpl).getResponseCode();
            } catch (IOException e) {
                String errMsg = e.getMessage();
                Log.e(TAG, "getResponseCode error:" + errMsg);
                return TNConstants.ERROR_CODE_CONNECT_IOE;
            }
        }
        return TNConstants.ERROR_CODE_UNKNOWN;
    }

    public Map<String, List<String>> getResponseHeaders() {
        if (null == connImpl) {
            return null;
        }
        return connImpl.getHeaderFields();
    }

}
