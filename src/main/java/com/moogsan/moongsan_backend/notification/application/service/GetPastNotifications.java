package com.moogsan.moongsan_backend.notification.application.service;

import com.moogsan.moongsan_backend.notification.presentation.dto.NotificationResponse;
import com.moogsan.moongsan_backend.notification.presentation.dto.PagedResponse;
import com.moogsan.moongsan_backend.notification.domain.entity.Notification;
import com.moogsan.moongsan_backend.notification.domain.mapper.NotificationMapper;
import com.moogsan.moongsan_backend.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GetPastNotifications {

    private static final long RANGE      = 1_000_000_000_000L;

    private final NotificationRepository notificationRepository;

    public PagedResponse<NotificationResponse> getPastNotifications(Long userId, Long cursorId, int size) {

        int  level  = 3;       // default: 최상위부터 시작
        Long lastId = null;    // 첫 호출

        if (cursorId != null && cursorId > 0) {
            level  = (int)(cursorId / RANGE);   // priority
            lastId = cursorId %  RANGE;         // id
        }

        Pageable page = PageRequest.of(0, size + 1);

        List<Notification> raw = notificationRepository
                .fetchPage(userId, level, lastId, page);
        boolean hasNext = raw.size() == size + 1;
        if (hasNext) raw = raw.subList(0, size);

        Long nextCursor = hasNext
                ? raw.getLast().getPriorityLevel() * RANGE   // priority 보존
                + raw.getLast().getId()                    // id
                : null;

        List<NotificationResponse> items = raw.stream()
                .map(NotificationMapper::toNotificationResponse)
                .toList();

        return PagedResponse.<NotificationResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
