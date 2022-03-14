package vn.tiki.tiniapp.cache;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.tiki.tiniapp.TNEngine;

public class FileUtils {

    private static final String TAG = "Tini.FileRepo";
    private static final String HEADER_EXT = ".header";

    private static final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getMD5(String content) {
        if (TextUtils.isEmpty(content))
            return "";
        try {
            MessageDigest sha1 = MessageDigest.getInstance("MD5");
            sha1.update(content.getBytes(), 0, content.getBytes().length);
            return toHexString(sha1.digest());
        } catch (Exception e) {
            return "";
        }
    }

    private static String toHexString(byte b[]) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            sb.append(hexChar[(aB & 0xf0) >>> 4]);
            sb.append(hexChar[aB & 0xf]);
        }
        return sb.toString();
    }

    public static String getSHA1(byte[] contentBytes) {
        if (contentBytes == null || contentBytes.length <= 0) {
            return "";
        }
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            sha1.update(contentBytes, 0, contentBytes.length);
            return toHexString(sha1.digest());
        } catch (Exception e) {
            return "";
        }
    }

    static String getResourceCachePath() {
        String dirPath = TNEngine.getInstance().getConfig().getAbsoluteCache();
        if (!dirPath.endsWith(File.separator)) {
            dirPath += File.separator;
        }
        return dirPath;
    }

    static String getResourcePath(String resourceName) {
        return getResourceCachePath() + resourceName;
    }

    static String getResourceHeaderPath(String resourceName) {
        return getResourceCachePath() + resourceName + HEADER_EXT;
    }

    static boolean deleteResourceFiles(String resourceId) {
        boolean deleteSuccess = true;
        File resourceFile = new File(getResourcePath(resourceId));
        if (resourceFile.exists()) {
            deleteSuccess = resourceFile.delete();
        }

        File headerFile = new File(getResourceHeaderPath(resourceId));
        if (headerFile.exists()){
            deleteSuccess &= headerFile.delete();
        }

        return deleteSuccess;
    }


    public static Map<String, List<String>> getHeaderFromLocalCache(String headerPath) {
        Map<String, List<String>> headers = new HashMap<>();
        File headerFile = new File(headerPath);
        if (!headerFile.exists()) {
            return headers;
        }

        String headerString = readFile(headerFile);
        if (TextUtils.isEmpty(headerString)) {
            return headers;
        }


        String[] headerArray = headerString.split("\r\n");
        if (headerArray.length == 0) {
            return headers;
        }

        List<String> tmpHeaderList;
        for (String header : headerArray) {
            String[] keyValues = header.split(" : ");
            if (keyValues.length == 2) {
                String key = keyValues[0].trim();
                tmpHeaderList = headers.get(key.toLowerCase());
                if (null == tmpHeaderList) {
                    tmpHeaderList = new ArrayList<String>(1);
                    headers.put(key.toLowerCase(), tmpHeaderList);
                }
                tmpHeaderList.add(keyValues[1].trim());
            }
        }

        return headers;
    }

    private static String readFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        // read
        BufferedInputStream bis = null;
        InputStreamReader reader = null;
        char[] buffer;
        String rtn = null;
        int n;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            reader = new InputStreamReader(bis);
            int size = (int) file.length();
            if (size > 1024 * 12) {
                buffer = new char[1024 * 4];
                StringBuilder result = new StringBuilder(1024 * 12);
                while (-1 != (n = reader.read(buffer))) {
                    result.append(buffer, 0, n);
                }
                rtn = result.toString();
            } else {
                buffer = new char[size];
                n = reader.read(buffer);
                rtn = new String(buffer, 0, n);
            }
        } catch (Throwable e) {
            Log.e(TAG, "readFile error:(" + file.getName() + ") " + e.getMessage());
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    Log.e(TAG, "readFile close error:(" + file.getName() + ") " + e.getMessage());
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Log.e(TAG, "readFile close error:(" + file.getName() + ") " + e.getMessage());
                }
            }
        }
        return rtn;
    }

    public static boolean verifyData(byte[] content, String targetSha1) {
        return content != null && !TextUtils.isEmpty(targetSha1) &&
                targetSha1.equals(getSHA1(content));
    }

    public static byte[] readFileToBytes(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }

        // read
        BufferedInputStream bis = null;
        ByteArrayOutputStream out = null;
        byte[] rtn = null;
        int n;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int size = (int) file.length();
            if (size > 1024 * 12) {
                out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024 * 4];
                while ((n = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, n);
                }
                rtn = out.toByteArray();
            } else {
                rtn = new byte[size];
                n = bis.read(rtn);
            }
        } catch (Throwable e) {
            Log.e(TAG, "readFile error:(" + file.getName() + ") " + e.getMessage());
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    Log.e(TAG, "readFile close error:(" + file.getName() + ") " + e.getMessage());
                }
            }
        }
        return rtn;
    }

    public static boolean saveResourceFiles(String resourceName, byte[] resourceBytes, Map<String, List<String>> headers) {
        String resourcePath = getResourcePath(resourceName);
        if (resourceBytes != null && !writeFile(resourceBytes, resourcePath)) {
            Log.e(TAG, "saveResourceFiles error: write resource data fail.");
            return false;
        }
        Log.i(TAG, "save resource path " + resourcePath);

        String resourceHeaderPath = getResourceHeaderPath(resourceName);
        Log.i(TAG, "save resource path header " + resourceHeaderPath);
        if (headers != null && headers.size() > 0
                &&!writeFile(convertHeadersToString(headers), resourceHeaderPath)) {
            Log.e(TAG, "saveResourceFiles error: write header file fail.");
            return false;
        }
        return true;
    }

    private static String convertHeadersToString(Map<String, List<String>> headers) {
        if (headers != null && headers.size() > 0) {
            StringBuilder headerString = new StringBuilder();
            Set<Map.Entry<String, List<String>>> entries =  headers.entrySet();
            for (Map.Entry<String, List<String>> entry : entries) {
                String key = entry.getKey();
                if (!TextUtils.isEmpty(key)) {
                    List<String> values = entry.getValue();
                    for (String value : values) {
                        if (!TextUtils.isEmpty(value)) {
                            headerString.append(key).append(" : ");
                            headerString.append(value).append("\r\n");
                        }
                    }
                }
            }
            return headerString.toString();
        }

        return "";
    }

    private static boolean writeFile(String str, String filePath) {
        return writeFile(str.getBytes(), filePath);
    }

    private static boolean writeFile(byte[] content, String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        FileOutputStream fos = null;
        try {
            if (!file.exists() && !file.createNewFile()) {
                Log.e(TAG, "could not create file" + filePath);
                return false;
            }
            fos = new FileOutputStream(file);
            fos.write(content);
            fos.flush();
            return true;
        } catch (Throwable e) {
            Log.e(TAG, "writeFile error:(" + filePath + ") " + e.getMessage());
        } finally {
            if (null != fos) {
                try {
                    fos.close();
                } catch (Throwable e) {
                    Log.e(TAG, "writeFile close error:(" + filePath + ") " + e.getMessage());
                }
            }
        }
        return false;
    }
}
