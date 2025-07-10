package com.moogsan.moongsan_backend.domain.notification.service;

import com.moogsan.moongsan_backend.domain.notification.dto.NotificationResponse;
import com.moogsan.moongsan_backend.domain.notification.dto.PagedResponse;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import com.moogsan.moongsan_backend.domain.notification.mapper.NotificationMapper;
import com.moogsan.moongsan_backend.domain.notification.repository.NotificationRepository;
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

    private final NotificationRepository notificationRepository;

    public PagedResponse<NotificationResponse> getPastNotifications(Long userId, Long cursorId, int size) {

        Pageable page = PageRequest.of(0, size + 1);

        List<Notification> raw = (cursorId == null)
                ? notificationRepository.findByReceiverIdOrderByIdDesc(userId, page)
                : notificationRepository.findByReceiverIdAndIdLessThanOrderByIdDesc(userId, cursorId, page);

        boolean hasNext = raw.size() > size;

        if (hasNext) raw = raw.subList(0, size);

        List<NotificationResponse> items = raw.stream()
                .map(NotificationMapper::toNotificationResponse)
                .toList();

        Long nextCursor = hasNext ? raw.getLast().getId() : null;

        return PagedResponse.<NotificationResponse>builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
