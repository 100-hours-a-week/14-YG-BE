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

    // 공구글 ID로 주문 참여자 전체 확인
    List<Order> findAllByGroupBuyId(Long postId);

    // 유저 ID + 공구글 ID로 주문 단건 조회
    Optional<Order> findByUserIdAndGroupBuyId(Long userId, Long groupBuyId);

    // 유저 ID + 공구글 ID + 상태가 아닌 주문 단건 조회
    Optional<Order> findByUserIdAndGroupBuyIdAndStatusNot(Long userId, Long groupBuyId, String status);
    Optional<Order> findByUserIdAndGroupBuyIdAndStatusNotIn(Long userId, Long groupBuyId, List<String> statuses);

    // 유저 ID + 상태가 아닌 주문 단건 조희
    boolean existsByUserIdAndStatusNotIn(Long userId, List<String> statuses);
    boolean existsByUserIdAndGroupBuyIdAndStatusNotIn(Long userId, Long groupBuyId, List<String> statuses);

    // 특정 공구의 참여 인원 수 확인
    int countByGroupBuyIdAndStatusNotIn(Long postId, List<String> statuses);

    // 주문 취소 횟수 카운트
    int countByUserIdAndGroupBuyIdAndStatus(Long userId, Long groupBuyId, String status);
    int countByUserIdAndGroupBuyIdAndStatusIn(Long userId, Long groupBuyId, List<String> statuses);

    // 특정 공구의 주문 목록 확인
    List<Order> findByGroupBuyIdAndStatusNot(Long groupBuyId, String status);

    // 모든 공구의 컨펌, 취소되지 않은 주문 검사
    List<Order> findAllByCreatedAtBeforeAndStatusNotAndStatusNot(
            LocalDateTime createdAt,
            String excludedStatus1,
            String excludedStatus2
    );


    // 특정 유저의 특정 공구 참여 여부 확인
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

    // 특정 유저의 공구 게시글 상태별 참여(주문) 리스트 첫 조회
    @Query("""
        SELECT o
          FROM Order o
         WHERE o.user.id = :userId
           AND o.groupBuy.postStatus = :status
           AND o.status           <> 'Refunded'
        ORDER BY o.createdAt DESC, o.id DESC
    """)
    List<Order> findByUserAndPostStatusAndNotRefunded(
            @Param("userId") Long userId,
            @Param("status") String status,
            Pageable pageable
    );

    // // 특정 유저의 공구 게시글 상태별 참여(주문) 리스트 이어서 조회
    @Query("""
        SELECT o
          FROM Order o
         WHERE o.user.id = :userId
           AND o.groupBuy.postStatus = :status
           AND o.status           <> 'Refunded'
           AND (
                o.createdAt < :cursorCreatedAt
             OR (o.createdAt = :cursorCreatedAt AND o.id < :cursorOrderId)
           )
        ORDER BY o.createdAt DESC, o.id DESC
    """)
    List<Order> findByUserAndPostStatusAndNotRefundedBeforeCursor(
            @Param("userId")          Long userId,
            @Param("status")          String status,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorOrderId")   Long cursorOrderId,
            Pageable pageable
    );

    // 주문 참여자를 상태에 따라 순서대로 조회
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
}
