package vn.tiki.tiniapp.cache;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Map;

// TODO: implement this class
public class DownloadCache {
    private static final String TAG = "Tini.DownloadCache";

    public byte[] getResourceCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        String resourceId = FileUtils.getMD5(url);
        ResourceData resourceData = SQLiteResourceRepo.getResourceData(resourceId);

        if (resourceData.isExpired()) {
            Log.i(TAG, "resource data " + url + " expired.");
            resourceData.delete(url);
            return null;
        }

        boolean verifyError;
        byte[] resourceBytes = null;
        // verify local data
        if (TextUtils.isEmpty(resourceData.resourceSha1)) {
            verifyError = true;
            Log.i(TAG, "get resource data(" + url + "): resource data is empty.");
        } else {
            String resourcePath = FileUtils.getResourcePath(resourceId);
            File resourceFile = new File(resourcePath);
            resourceBytes = FileUtils.readFileToBytes(resourceFile);
            verifyError = resourceBytes == null || resourceBytes.length <= 0;
            if (verifyError) {
                Log.e(TAG, "get resource data(" + url + ") error:cache data is null.");
            } else {
                if (!FileUtils.verifyData(resourceBytes, resourceData.resourceSha1)) {
                    verifyError = true;
                    resourceBytes = null;
                    Log.e(TAG, "get resource data(" + url + ") error:verify html cache with sha1 fail.");
                } else {
                    Log.e(TAG, "get resource data(" + url + ") verify html cache with sha1 success.");
                }
            }
        }
        // if the local data is faulty, delete it
        if (verifyError) {
            resourceData.delete(url);
        }
        return resourceBytes;

    }

    public Map<String, List<String>> getResourceCacheHeader(String url) {
        String resourceName = FileUtils.getMD5(url);
        String headerPath = FileUtils.getResourceHeaderPath(resourceName);
        return FileUtils.getHeaderFromLocalCache(headerPath);
    }

    public void saveResourceCache(String url, byte[] content, Map<String, List<String>> headers) {
        String resourceId = FileUtils.getMD5(url);
        FileUtils.saveResourceFiles(resourceId, content, headers);
        // save resource data to db
        ResourceData resourceData = ResourceData.createResource(resourceId, content, headers);
        SQLiteResourceRepo.saveResourceData(resourceId, resourceData);
    }
}
