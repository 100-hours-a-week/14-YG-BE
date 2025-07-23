package com.moogsan.moongsan_backend.notification.domain.repository;

import com.moogsan.moongsan_backend.notification.domain.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * priority_level(2:미읽음·ORDER_CANCELED, 1 = 미읽음·기타, 0 = 읽음)
 * 인덱스:  (receiver_id, priority_level, id DESC)
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT DISTINCT n
        FROM Notification n
        WHERE n.receiverId = :uid
          AND (
               n.priorityLevel < :lvl
            OR (n.priorityLevel = :lvl AND (:id IS NULL OR n.id < :id))
          )
        ORDER BY n.priorityLevel DESC, n.id DESC
    """)
    List<Notification> fetchPage(Long uid, int lvl, Long id, Pageable pageable);
}
