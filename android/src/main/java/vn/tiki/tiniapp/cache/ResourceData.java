package vn.tiki.tiniapp.cache;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import java.util.Map;

public class ResourceData {
    private static final String TAG = "Tini.ResourceData";

    String resourceId;
    String resourceSha1;
    long resourceSize;
    long lastUpdateTime;
    public long expiredTime;

    public void reset() {
        resourceSha1 = "";
        resourceSize = 0;
        lastUpdateTime = 0;
        expiredTime = 0;
    }

    public boolean isExpired() {
        return expiredTime < System.currentTimeMillis();
    }

    public void delete(String url) {
        long startTime = System.currentTimeMillis();
        SQLiteResourceRepo.removeResourceData(resourceId);
        FileUtils.deleteResourceFiles(resourceId);
        Log.i(TAG, "remove resource data(" +  url + "), cost: " + +(System.currentTimeMillis() - startTime) + "ms.");
        this.reset();
    }

    public static ResourceData createResource(String resourceId, byte[] content, Map<String, List<String>> headers) {
        ResourceData resourceData = new ResourceData();
        resourceData.resourceId = resourceId;
        resourceData.resourceSha1 = FileUtils.getSHA1(content);
        resourceData.resourceSize = content.length;
        // TODO: handle expiredTime correctly
        resourceData.expiredTime = Long.MAX_VALUE;
        resourceData.lastUpdateTime = System.currentTimeMillis();

        return resourceData;
    }
}
