package com.reactnativecommunity.webview;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.reactnativecommunity.webview.MD5Utils;

public class MD5UtilsTest {
    @Test
    public void getMD5_ReturnsCorrectValue() {
        assertEquals(MD5Utils.getMD5("hello"), "5D41402ABC4B2A76B9719D911017C592");
    }
}
