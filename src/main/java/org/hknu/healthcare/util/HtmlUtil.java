package org.hknu.healthcare.util;

public class HtmlUtil {
    public static String strip(String s) {
        return s == null ? null : s.replaceAll("<[^>]+>", "").trim();
    }
}