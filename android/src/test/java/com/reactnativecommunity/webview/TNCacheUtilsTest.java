package com.reactnativecommunity.webview;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.net.URL;
import java.util.HashMap;

public class TNCacheUtilsTest {
  @Test
  public void getFolderMD5_DoesNotCareAboutQueryParams() throws Exception {
    TNCacheUtils cacheUtils = new TNCacheUtils();
    assertEquals(cacheUtils.getFolderMD5(new URL("http://localhost:8082/index.worker.js?__appStartTime=10000")), "6666cd76f96956469e7be39d750cc7d9");
    assertEquals(cacheUtils.getFolderMD5(new URL("http://localhost:8082/index.worker.js?__appStartTime=10001")), "6666cd76f96956469e7be39d750cc7d9");
    assertEquals(cacheUtils.getFolderMD5(new URL("http://localhost:8082/index.worker.js?__a=10&b=20")), "6666cd76f96956469e7be39d750cc7d9");
  }

  @Test
  public void getCachePathWithCacheFolderMapping_ReturnCorrectValue() throws Exception {
    TNCacheUtils cacheUtils = new TNCacheUtils();
    HashMap<String, String> mapping = new HashMap<>();
    mapping.put("framework", "/path/to/framework/cache");
    mapping.put("tikicdn.com/app", "/path/to/app/build/cache");

    assertEquals(cacheUtils.getCachePathWithCacheFolderMapping(new URL("http://framework/tf-tiniapp.render.js"), mapping), "/path/to/framework/cache/tf-tiniapp.render.js");
    assertEquals(cacheUtils.getCachePathWithCacheFolderMapping(new URL("http://framework/tf-tiniapp.render.js?a=1&b=2"), mapping), "/path/to/framework/cache/tf-tiniapp.render.js");
    assertEquals(cacheUtils.getCachePathWithCacheFolderMapping(new URL("http://tikicdn.com/app/index.js"), mapping), "/path/to/app/build/cache/index.js");
  }
}
