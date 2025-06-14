package com.moogsan.moongsan_backend.global.util;

import org.bson.types.ObjectId;

public class ObjectIdScoreUtil {

    /**
     * ObjectId에서 Redis ZSet에 사용할 score(double)을 추출한다.
     * 정렬 기준: timestamp(millis) * 1000 + counter(하위 12비트)
     *
     * @param objectIdStr ObjectId 문자열 (24자리 hex)
     * @return Redis ZSet용 정렬 score (μs 단위 double)
     */
    public static double toScore(String objectIdStr) {
        if (objectIdStr == null || !ObjectId.isValid(objectIdStr)) {
            throw new IllegalArgumentException("❌ 유효하지 않은 ObjectId: " + objectIdStr);
        }

        ObjectId oid = new ObjectId(objectIdStr);
        long millis = oid.getTimestamp() * 1000L;
        int counter = extractCounter(oid) & 0xFFF;
        return millis * 1_000.0 + counter;
    }


    /**
     * ObjectId에서 counter 부분을 추출한다.
     * MongoDB ObjectId의 마지막 3바이트가 counter이다.
     */
    private static int extractCounter(ObjectId oid) {
        byte[] b = oid.toByteArray(); // 12 bytes
        return ((b[9]  & 0xFF) << 16) |
                ((b[10] & 0xFF) << 8)  |
                ( b[11] & 0xFF);
    }

    private ObjectIdScoreUtil() {
        // static-only class: 인스턴스화 방지
    }
}

