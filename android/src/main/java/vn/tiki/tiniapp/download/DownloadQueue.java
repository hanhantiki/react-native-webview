package vn.tiki.tiniapp.download;

import android.text.TextUtils;

import java.util.LinkedHashMap;

public class DownloadQueue extends LinkedHashMap<String, DownloadTask> {

    synchronized DownloadTask dequeue() {
        if (values().iterator().hasNext()) {
            DownloadTask task = values().iterator().next();
            return remove(task.url);
        }
        return null;
    }

    synchronized void enqueue(DownloadTask task) {
        if (task != null && !TextUtils.isEmpty(task.url)) {
            put(task.url, task);
        }
    }
}
