package com.moogsan.moongsan_backend.domain.user.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moogsan.moongsan_backend.domain.user.exception.base.UserException;
import com.moogsan.moongsan_backend.domain.user.exception.code.UserErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Slf4j
@Service
public class CheckAccountService {

    public void checkBankAccountHolder(String bankCode, String bankNum, String name) {
        // Iamport API에서 발급받은 REST API Key와 Secret
        String impKey = "8765210302616126";
        String impSecret = "BtJeILNsVdZjAUlht0MtIQw1hpvf6qucJsOTbh4bYrUZb1EeI24ywBHVOkCTzPJ7niOtsxNNopDzKMIO";

        // 액세스 토큰을 발급받기 위한 URL
        String tokenUrl = "https://api.iamport.kr/users/getToken";

        try {
            // 1단계: 액세스 토큰 요청
            URL url = new URL(tokenUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); // POST 요청 시 body 데이터를 포함시킴
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // JSON 요청 바디 생성 (imp_key, imp_secret 포함)
            JsonObject tokenRequest = new JsonObject();
            tokenRequest.addProperty("imp_key", impKey);
            tokenRequest.addProperty("imp_secret", impSecret);

            // 요청 바디 전송
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()))) {
                writer.write(tokenRequest.toString());
                writer.flush();
            }

            // 응답 코드가 200이면 성공
            if (conn.getResponseCode() == 200) {
                // 응답 본문 읽기
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                // 응답 JSON 파싱하여 액세스 토큰 추출
                String tokenJson = sb.toString();
                JsonElement tokenElement = JsonParser.parseString(tokenJson);
                String accessToken = tokenElement.getAsJsonObject().getAsJsonObject("response").get("access_token").getAsString();

                // 2단계: 예금주 정보 요청
                String holderUrl = "https://api.iamport.kr/vbanks/holder";
                String query = String.format("?bank_code=%s&bank_num=%s",
                        URLEncoder.encode(bankCode, "UTF-8"),
                        URLEncoder.encode(bankNum, "UTF-8"));

                // 예금주 조회 URL 생성
                URL bankUrl = new URL(holderUrl + query);
                HttpURLConnection getConn = (HttpURLConnection) bankUrl.openConnection();
                getConn.setRequestMethod("GET");
                getConn.setRequestProperty("Content-Type", "application/json");
                getConn.setRequestProperty("Authorization", "Bearer " + accessToken);

                // 예금주 조회 응답 코드가 200이면 성공
                if (getConn.getResponseCode() == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                    StringBuilder getResponseSb = new StringBuilder();
                    String getLine;
                    while ((getLine = br.readLine()) != null) {
                        getResponseSb.append(getLine);
                    }

                    // 응답 JSON 파싱하여 예금주 이름 추출
                    JsonObject holderJson = JsonParser.parseString(getResponseSb.toString()).getAsJsonObject();
                    String bankHolder = holderJson.getAsJsonObject("response").get("bank_holder").getAsString();

                    if (!bankHolder.equals(name)) {
                        throw new UserException(UserErrorCode.INVALID_INPUT, "계좌주명이 실명과 일치하지 않습니다.");
                    }
                } else {
                    throw new UserException(UserErrorCode.NOT_FOUND, "계좌번호가 존재하지 않습니다.");
                }
            } else {
                throw new UserException(UserErrorCode.INTERNAL_SERVER_ERROR, "내부 서버 오류 발생");
            }
        } catch (IOException e) {
            log.error("Error while checking bank holder", e);
            throw new UserException(UserErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
