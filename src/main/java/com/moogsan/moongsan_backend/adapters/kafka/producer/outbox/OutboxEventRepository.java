package com.moogsan.moongsan_backend.global.outbox;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEventEntity e " +
            "WHERE e.status = 'PENDING' " +
            " AND e.nextRetryAt <= : now "+
            "ORDER BY e.createdAt ASC")
    List<OutboxEventEntity> findNextBatch(
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
