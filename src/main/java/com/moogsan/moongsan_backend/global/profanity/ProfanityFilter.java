package com.moogsan.moongsan_backend.global.profanity;

import org.springframework.core.io.ClassPathResource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.AFTER_PROFANITY;
import static com.moogsan.moongsan_backend.global.message.ResponseMessage.LOAD_FILE_FAIL;

public class ProfanityFilter {

    // 1) 정적 키워드 리스트 (profanity.txt에서 로드)
    private static final Set<String> BANNED_WORDS = loadBannedWords();

    // 2) 정규식 패턴 리스트: 변형·은어 대응
    private static final List<Pattern> PATTERNS = Arrays.asList(
            // 한글 반복·변형 대응
            Pattern.compile("시+발+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ㅅ+[ㅣi1!]+[ㅂb]+[ㅏa4]+[ㄹl]+", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ㅈ+[ㅗo0]+[ㄷd]+?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ㄱ+[ㅐae]+[ㅅs]+[ㅋk]+", Pattern.CASE_INSENSITIVE),
            // 영어 욕설 대응
            Pattern.compile("\\b(f+u+c+k+|f+u+u+c+kk+)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(m+o+t+h+e+r+f+u+c+k+e+r+)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(s+h+i+t+)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(b+i+t+c+h+)\\b", Pattern.CASE_INSENSITIVE),
            // 자살·살해 조장 표현
            Pattern.compile("죽+어+(고싶|지싶|ㄹ래)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(self[-_ ]?harm|suicide|die(ing)?|killme?)\\b", Pattern.CASE_INSENSITIVE)
    );

    /**
     * 입력 텍스트에서 금지 단어 및 패턴을 제거합니다.
     * @param input 원본 텍스트
     * @return 필터링된 텍스트
     */
    public static String filter(String input) {
        if (input == null) {
            return null;
        }
        String result = input;

        // 1) 정적 키워드 필터링
        for (String word : BANNED_WORDS) {
            String regex = "(?i)" + Pattern.quote(word);
            result = result.replaceAll(regex, "");
        }

        // 2) 정규식 패턴 필터링
        for (Pattern pattern : PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }

        if (result.trim().isEmpty()) {
            return AFTER_PROFANITY;
        }

        return result;
    }

    private static Set<String> loadBannedWords() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("profanity.txt").getInputStream(),
                        StandardCharsets.UTF_8
                ))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException(LOAD_FILE_FAIL, e);
        }
    }
}
