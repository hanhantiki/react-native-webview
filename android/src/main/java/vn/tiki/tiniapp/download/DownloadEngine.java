package vn.tiki.tiniapp.download;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import vn.tiki.tiniapp.TNEngine;
import vn.tiki.tiniapp.cache.DownloadCache;

public class DownloadEngine implements Handler.Callback {
    static final String TAG = "Tini.DownloadEngine";

    /**
     * Maximum number of concurrent download tasks
     */
    static final int MAX_NUM_OF_DOWNLOADING_TASKS = 3;

    /**
     * message code enqueue:
     * when downloading tasks more than config number, the task should enqueue for waiting.
     */
    static final int MSG_ENQUEUE = 0;

    /**
     * message code: one download task is complete and the download queue is free.
     */
    static final int MSG_DEQUEUE = 1;

    /**
     * the download task queue.
     */
    private final DownloadQueue queue;

    /**
     * download thread handler.
     */
    private final Handler handler;

    /**
     * number of downloading tasks.
     */
    private final AtomicInteger numOfDownloadingTask;

    /**
     * A download cache
     */
    private final DownloadCache cache;

    private final ConcurrentMap<String, DownloadTask> resourceTasks = new ConcurrentHashMap<>();

    public DownloadEngine() {
        cache = new DownloadCache();
        queue = new DownloadQueue();
        HandlerThread queueThread = new HandlerThread("Tini.Download-Thread");
        queueThread.start();
        handler = new Handler(queueThread.getLooper(), this);
        numOfDownloadingTask = new AtomicInteger(0);
    }

    /**
     * Override message for Handler.Callback
     *
     * @param msg - input message
     * @return boolean value
     */
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case MSG_ENQUEUE:
                DownloadTask task = (DownloadTask) msg.obj;
                queue.enqueue(task);
                task.state.set(DownloadTask.STATE_QUEUING);
                Log.i(TAG, "enqueue download task url: " + task.url);
                break;
            case MSG_DEQUEUE:
                if (!queue.isEmpty()) {
                    task = queue.dequeue();
                    startDownload(task);
                    Log.i(TAG, "dequeue download task url: " + task.url);
                }
                break;
        }
        return false;
    }

    private DownloadTask createDownloadTask(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        // get task in queue
        synchronized (queue) {
            if (queue.containsKey(url)) {
                Log.i(TAG, "download task has been in queue, url: " + url);
                return queue.get(url);
            }
        }

        // create a new task
        DownloadTask task = new DownloadTask();
        task.url = url;

        // get task from cache
        byte[] resourceBytes = cache.getResourceCache(url);
        if (resourceBytes != null) {
            task.inputStream = new ByteArrayInputStream(resourceBytes);
            task.responseHeaders = cache.getResourceCacheHeader(url);
            task.state.set(DownloadTask.STATE_LOAD_FROM_CACHE);
            return task;
        }

        if (numOfDownloadingTask.get() < MAX_NUM_OF_DOWNLOADING_TASKS) {
            startDownload(task);
        } else {
            Message msg = handler.obtainMessage(MSG_ENQUEUE, task);
            handler.sendMessage(msg);
        }
        return task;
    }

    private void startDownload(DownloadTask task) {
        // set callback for task
        task.callbacks.add(new DownloadCallback.SimpleDownloadCallback() {
            @Override
            public void onStart() {
                Log.i(TAG, "start download resource, url=" + task.url);
            }

            @Override
            public void onSuccess(byte[] content, Map<String, List<String>> rspHeaders) {
                cache.saveResourceCache(task.url, content, rspHeaders);
            }

            @Override
            public void onError(int errorCode) {
               Log.i(TAG, "download resource error: code = " + errorCode + ", url=" + task.url);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "download resource finished, url = " + task.url);
                task.state.set(DownloadTask.STATE_DOWNLOADED);
                handler.sendEmptyMessage(MSG_DEQUEUE);
            }
        });

        TNEngine.getInstance().postTask(() -> {
            numOfDownloadingTask.incrementAndGet();
            task.state.set(DownloadTask.STATE_DOWNLOADING);
            DownloadClient client = new DownloadClient(task);
            client.execute();
        });
    }

    public void preloadResources(List<String> urls) {
        for (final String url : urls) {
            // we do not preload duplicated url
            if (resourceTasks.containsKey(url)) {
                continue;
            }

            resourceTasks.put(url, createDownloadTask(url));
        }
    }

    public DownloadTask onResourceDownloaded(String url) {
        if (!resourceTasks.containsKey(url)) {
            return null;
        }

        DownloadTask task = resourceTasks.get(url);
        task.wasWaitingForResult.set(true);
        if (task.state.get() == DownloadTask.STATE_INITIATE || task.state.get() == DownloadTask.STATE_QUEUING) {
            return null;
        }

        if (task.inputStream == null) {
            synchronized (task.wasWaitingForResult) {
                try {
                    task.wasWaitingForResult.wait(3000);
                } catch(InterruptedException e) {
                    Log.e(TAG, "onResourceDownloaded error: " + e.getMessage());
                }
            }
        }

        if (task.inputStream == null) {
            return null;
        }

        return task;
    }

}
