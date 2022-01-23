package vn.tiki.tiniapp;

import java.util.Arrays;
import java.util.List;

import vn.tiki.tiniapp.cache.SQLiteRepo;
import vn.tiki.tiniapp.download.DownloadEngine;
import vn.tiki.tiniapp.download.DownloadTask;

public class TNEngine {
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
     * Make sure {@link #createInstance(config)} has been called.
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
     * Create TNEngine instance. Meanwhile it will initialize engine and SonicRuntime.
     *
     * @return TNEngine object
     */
    public static synchronized TNEngine createInstance(TNConfig config) {
        if (null == instance) {
            instance = new TNEngine(config);
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

    public void postTask(Runnable r) {
        threadPool.postTask(r);
    }

    public String getAbsolutePath() {
        return config.getAbsoluteCache();
    }
}
