package vn.tiki.tiniapp.download;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

import android.util.Log;

import vn.tiki.tiniapp.TNConstants;

/**
 * Handles a single HTTP resource download
 *
 */
public class DownloadClient {
    private static final String TAG = "Tini.DownloadClient";
    private static final int READ_BUFFER_SIZE = 2048;

    private final DownloadTask task;
    private final DownloadConnection conn;
    private final ByteArrayOutputStream outputStream;

    public DownloadClient(DownloadTask task) {
        this.task = task;
        conn = new DownloadConnection(task.url);
        outputStream = new ByteArrayOutputStream();
    }

    public int execute() {
        onStart();
        int resultCode = conn.connect();
        if (TNConstants.ERROR_CODE_SUCCESS != resultCode) {
            onError(resultCode);
            return resultCode;
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HTTP_OK) {
            onError(responseCode);
            return responseCode;
        }

        task.responseHeaders = conn.getResponseHeaders();
        if (!readServerResponse()) {
            return TNConstants.ERROR_CODE_UNKNOWN;
        }

        synchronized (task.wasWaitingForResult) {
            task.wasWaitingForResult.notify();
        }
        return TNConstants.ERROR_CODE_SUCCESS;
    }

    private boolean readServerResponse() {
        BufferedInputStream bufferedInputStream = conn.getResponseStream();
        if (null == bufferedInputStream) {
            Log.e(TAG, "readServerResponse error: bufferedInputStream is null!");
            return false;
        }

        try {
            byte[] buffer = new byte[READ_BUFFER_SIZE];
            int total = conn.connImpl.getContentLength();
            int n = 0, sum = 0;
            while (-1 != (n = bufferedInputStream.read(buffer))) {
                outputStream.write(buffer, 0, n);
                sum += n;
                if (total > 0) {
                    onProgress(sum, total);
                }
            }

            if (n == -1) {
                onSuccess(outputStream.toByteArray(), conn.getResponseHeaders());
            }
        } catch (Exception e) {
            Log.e(TAG, "readServerResponse error:" + e.getMessage() + ".");
            return false;
        }

        return true;
    }

    private void onStart() {
        for (DownloadCallback callback : task.callbacks) {
            if (callback != null) {
                callback.onStart();
            }
        }
    }

    private void onProgress(int pro, int total) {
        for (DownloadCallback callback : task.callbacks) {
            if (callback != null) {
                callback.onProgress(pro, total);
            }
        }
    }

    private void onSuccess(byte[] content, Map<String, List<String>> headers) {
        for (DownloadCallback callback : task.callbacks) {
            if (callback != null) {
                callback.onSuccess(content, headers);
            }
        }
        onFinish();
    }

    private void onError(int errCode) {
        for (DownloadCallback callback : task.callbacks) {
            if (callback != null) {
                callback.onError(errCode);
            }
        }
        onFinish();
    }

    private void onFinish() {
        for (DownloadCallback callback : task.callbacks) {
            if (callback != null) {
                callback.onFinish();
            }
        }
        conn.disconnect();
    }

}
