package vn.tiki.tiniapp;

import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.facebook.react.bridge.ReadableMap;

import java.util.Arrays;
import java.util.List;

import vn.tiki.tiniapp.cache.SQLiteRepo;
import vn.tiki.tiniapp.download.DownloadEngine;
import vn.tiki.tiniapp.download.DownloadTask;
import vn.tiki.tiniapp.webview.TiniJavascriptInterface;

public class TNEngine {
    private static final String TAG = "Tini.TNEngine";

    /**
     * Single instance
     */
    private static TNEngine instance;

    /**
     * The real download engine
     */
    private DownloadEngine downloader;

    /**
     * Thread Pool to execute
     */
    private TNThreadPool threadPool;

    private TNConfig config;

    private TNEngine(TNConfig config) {
        downloader = new DownloadEngine();
        threadPool = new TNThreadPool();
        this.config = config;
        SQLiteRepo.createInstance(config.getContext());
    }

    /**
     * Returns a TNEngine instance.
     * Make sure {@link #createInstance} has been called.
     *
     * @return TNEngine instance
     */
    public static synchronized TNEngine getInstance() {
        if (null == instance) {
            throw new IllegalStateException("TNEngine::createInstance() needs to be called before TNEngine::getInstance()");
        }
        return instance;
    }

    /**
     * Create TNEngine instance. Meanwhile it will initialize engine
     *
     * @return TNEngine object
     */
    public static synchronized TNEngine createInstance(TNConfig config) {
        if (null == instance) {
            instance = new TNEngine(config);
            Log.i(TAG, "create TNEngine");
        }

        return instance;
    }

    /**
     * Ask engine to preload a list of urls, and do not care about the result.
     * All urls will be downloaded in background, and cached
     *
     * @param urls - list of resource to preload
     */
    public void preloadResources(List<String> urls) {
        downloader.preloadResources(urls);
    }

    /**
     * Ask engine to download a resource.
     * This API will wait for download task is finish, and return its result
     *
     * @param url - resource url
     * @return a DownloadTask with result
     */
    public DownloadTask downloadResource(String url) {
        downloader.preloadResources(Arrays.asList(url));
        return downloader.onResourceDownloaded(url);
    }

    /**
     * Send task to thread pool
     * @param r - runnable
     */
    public void postTask(Runnable r) {
        threadPool.postTask(r);
    }

    /**
     * Setup WebView to bind with this engine
     * @param webview
     */
    public void setUpWebView(WebView webview, ReadableMap appMeta) {
        Log.i(TAG, "set up JavascriptInterface for object with name Tini");
        webview.addJavascriptInterface(new TiniJavascriptInterface(appMeta), "Tini");
    }

    public TNConfig getConfig() {
        return config;
    }

    public WebResourceResponse shouldInterceptRequest(final WebView view, String url) {
        Uri originUrl = Uri.parse(url);
        String host = originUrl.getHost();
        if (host == null) {
            return null;
        }

        String path = originUrl.getPath();
        if (path == null) {
            return null;
        }

        boolean isValidHost = host.endsWith(".tikicdn.com") || host.endsWith(".tiki.vn") || host.endsWith(".tala.xyz") || host.startsWith("localhost");
        boolean isBundle = (
            path.endsWith("/tf-tiniapp.render.js") ||
            path.endsWith("/tf-tiniapp.worker.js") ||
            path.endsWith("/tf-tiniapp.css") ||
            path.endsWith("/index.js") ||
            path.endsWith("/index.worker.js"));
        if (!isValidHost || !isBundle) {
            return null;
        }

        DownloadTask task = downloadResource(url);
        return new WebResourceResponse("", "UTF-8", task.inputStream);
    }
}
