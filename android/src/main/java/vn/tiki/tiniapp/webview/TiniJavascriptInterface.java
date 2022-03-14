package vn.tiki.tiniapp.webview;

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.facebook.react.bridge.ReadableMap;

import org.json.JSONObject;

public class TiniJavascriptInterface {
    private static final String TAG = "TiniJavascriptInterface";

    ReadableMap appMeta;

    public TiniJavascriptInterface(ReadableMap appMeta) {
        this.appMeta = appMeta;
    }

    @JavascriptInterface
    public String runtimeVariables() {
        try {
            JSONObject obj = JSONUtils.convertMapToJson(appMeta.getMap("runtimeVariables"));
            return obj.toString();
        } catch(Exception e) {
            Log.e(TAG, "convert appMeta to JSON failed, error: " + e.toString());
            return "{}";
        }
    }

}
