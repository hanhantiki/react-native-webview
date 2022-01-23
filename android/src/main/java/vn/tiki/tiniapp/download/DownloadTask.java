package vn.tiki.tiniapp.download;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DownloadTask record download information
 */
public class DownloadTask {
    public static final int STATE_INITIATE = 0;
    public static final int STATE_QUEUING = 1;
    public static final int STATE_DOWNLOADING = 2;
    public static final int STATE_DOWNLOADED = 3;
    public static final int STATE_LOAD_FROM_CACHE = 3;

    /**
     * url of resource to download
     */
    public String url;

    /**
     * download request 's response headers
     */
    public Map<String, List<String>> responseHeaders;

    /**
     * the network stream or memory stream
     */
    public InputStream inputStream;

    /**
     * the task's download state
     */
    public final AtomicInteger state = new AtomicInteger(STATE_INITIATE);

    /**
     * whether the task's responding resource was intercepted by WebView
     */
    public final AtomicBoolean wasWaitingForResult = new AtomicBoolean(false);

    /**
     * list of download callback
     */
    public List<DownloadCallback> callbacks = new ArrayList<>();
}
