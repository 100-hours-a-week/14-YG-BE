package com.moogsan.moongsan_backend.domain.notification.repository;

import com.moogsan.moongsan_backend.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // cursorId 없을 때
    List<Notification> findByReceiverIdOrderByIdDesc(Long receiverId, Pageable pageable);

    // cursorId 있을 때
    List<Notification> findByReceiverIdAndIdLessThanOrderByIdDesc(
            Long receiverId, Long cursorId, Pageable pageable);
}
