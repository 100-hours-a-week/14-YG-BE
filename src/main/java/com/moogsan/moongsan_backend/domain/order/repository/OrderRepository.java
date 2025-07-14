package com.moogsan.moongsan_backend.domain.order.repository;

import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 🔍 FIND
    List<Order> findAllByGroupBuyId(Long postId);
    Optional<Order> findByUserIdAndGroupBuyId(Long userId, Long groupBuyId);
    Optional<Order> findByUserIdAndGroupBuyIdAndStatusNot(Long userId, Long groupBuyId, String status);
    Optional<Order> findByUserIdAndGroupBuyIdAndStatusNotIn(Long userId, Long groupBuyId, List<String> statuses);
    List<Order> findByGroupBuyIdAndStatusNot(Long groupBuyId, String status);
    List<Order> findByGroupBuyIdAndStatusNotIn(Long groupBuyId, List<String> statuses);

    // 🔢 COUNT
    int countByGroupBuyIdAndStatusNot(Long postId, String status);
    int countByUserIdAndGroupBuyIdAndStatus(Long userId, Long groupBuyId, String status);
    int countByUserIdAndGroupBuyIdAndStatusIn(Long userId, Long groupBuyId, List<String> statuses);
    long countByGroupBuyIdAndStatusNotIn(Long groupBuyId, List<String> statuses);

    // ✅ EXISTS
    boolean existsByUserIdAndStatusNotIn(Long userId, List<String> statuses);
    boolean existsByUserIdAndGroupBuyIdAndStatusIn(Long userId, Long groupBuyId, List<String> statuses);
    boolean existsByUserIdAndGroupBuyIdAndStatusNotIn(Long userId, Long groupBuyId, List<String> statuses);

    // 🧾 CUSTOM QUERIES (@Query)
    @Query("""
      SELECT CASE WHEN COUNT(o) > 0 THEN TRUE ELSE FALSE END
        FROM Order o
       WHERE o.user.id      = :userId
         AND o.groupBuy.id  = :groupBuyId
         AND o.status      <> 'CANCELED'
    """)
    boolean existsParticipant(
            @Param("userId") Long userId,
            @Param("groupBuyId") Long groupBuyId,
            @Param("status") String status);

    @Query("""
        SELECT o
          FROM Order o
         WHERE o.user.id = :userId
           AND o.groupBuy.postStatus = :status
           AND o.status           <> 'CANCELED'
        ORDER BY o.createdAt DESC, o.id DESC
    """)
    List<Order> findByUserAndPostStatusAndNotCanceled(
            @Param("userId") Long userId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("""
        SELECT o
          FROM Order o
         WHERE o.user.id = :userId
           AND o.groupBuy.postStatus = :status
           AND o.status           <> 'CANCELED'
           AND (
                o.createdAt < :cursorCreatedAt
             OR (o.createdAt = :cursorCreatedAt AND o.id < :cursorOrderId)
           )
        ORDER BY o.createdAt DESC, o.id DESC
    """)
    List<Order> findByUserAndPostStatusAndNotCanceledBeforeCursor(
            @Param("userId")          Long userId,
            @Param("status")          String status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorOrderId")   Long cursorOrderId,
            Pageable pageable
    );

    @Query("""
        SELECT o FROM Order o
         WHERE o.groupBuy.id = :postId
         ORDER BY
             CASE o.status
                 WHEN 'CANCELED' THEN 1
                 WHEN 'PENDING' THEN 2
                 WHEN 'CONFIRMED' THEN 3
                 WHEN 'REFUNDED' THEN 4
                 ELSE 5
             END
    """)
    List<Order> findAllByGroupBuyIdOrderByStatusCustom(@Param("postId") Long postId);

    @Query("""
        SELECT o FROM Order o
         WHERE o.createdAt < :createdAt
           AND o.status <> :excludedStatus1
           AND o.status <> :excludedStatus2
    """)
    List<Order> findAllByCreatedAtBeforeAndStatusNotAndStatusNot(
            @Param("createdAt") LocalDateTime createdAt,
            @Param("excludedStatus1") String excludedStatus1,
            @Param("excludedStatus2") String excludedStatus2
    );
}