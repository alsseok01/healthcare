package org.hknu.healthcare.util;

public class HtmlUtil {

    /**
     * 유틸리티 클래스이므로 생성자를 private으로 선언합니다.
     */
    private HtmlUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 문자열에서 HTML 태그를 제거합니다.
     * @param html HTML 태그가 포함된 원본 문자열
     * @return HTML 태그가 제거된 순수 텍스트
     */
    public static String stripHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        // <p>, <b> 등 모든 HTML 태그를 정규식을 이용해 제거하고, 앞뒤 공백을 제거합니다.
        return html.replaceAll("<[^>]*>", "").trim();
    }
}