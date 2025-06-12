package com.moogsan.moongsan_backend.global.profanity;

import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.moogsan.moongsan_backend.global.message.ResponseMessage.LOAD_FILE_FAIL;

public class ProfanityFilter {

    private static final Set<String> BANNED_WORDS = loadBannedWords();

    public static String filter(String input) {
        if (input == null) return null;

        for (String word : BANNED_WORDS) {
            String regex = "(?i)" + Pattern.quote(word);
            input = input.replaceAll(regex, "*".repeat(word.length()));
        }
        return input;
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
