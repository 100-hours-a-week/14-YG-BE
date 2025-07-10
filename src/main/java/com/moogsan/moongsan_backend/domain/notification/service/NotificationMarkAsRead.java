package com.moogsan.moongsan_backend.domain.notification.service;

import com.moogsan.moongsan_backend.domain.notification.dto.NotificationReadStatus;
import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import com.moogsan.moongsan_backend.domain.notification.exception.specific.NotiNotFoundException;
import com.moogsan.moongsan_backend.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationMarkAsRead {

    private final NotificationRepository notificationRepository;

    public void execute(Long userId, Long notificationId, NotificationReadStatus request) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotiNotFoundException::new);

        notification.markAsRead(request.getRead());

        notificationRepository.save(notification);

    }
}
