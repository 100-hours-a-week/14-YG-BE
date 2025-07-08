package com.moogsan.moongsan_backend.global.xss;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.vdurmont.emoji.EmojiParser;

public class XssSanitizer {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "em", "strong", "a", "p", "ul", "ol", "li", "br")
            .allowElements("code", "pre", "div", "span") // GPT용 태그 추가
            .allowUrlProtocols("http", "https")
            .allowAttributes("href").onElements("a")
            .toFactory();

    /**
     * 이모티콘을 유니코드로 변환하고, 오직 필요한 HTML 태그만 허용하며
     * 나머지 잠재적 XSS 요소를 제거합니다.
     */
    public static String sanitize(String input) {
        if (input == null) return null;

        // 1) ASCII 이모티콘 → 유니코드 이모지로 변환 (EmojiParser가 <3도 처리)
        String withEmoji = EmojiParser.parseToUnicode(input);

        // 2) XSS 정책에 따라 허용된 태그만 남기고 모두 제거
        return POLICY.sanitize(withEmoji);
    }
}
