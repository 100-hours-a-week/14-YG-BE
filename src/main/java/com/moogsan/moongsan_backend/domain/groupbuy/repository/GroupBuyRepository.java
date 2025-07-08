package com.moogsan.moongsan_backend.domain.groupbuy.repository;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long>, JpaSpecificationExecutor<GroupBuy> {

    // 공구 마감 조건 기반 조회 (백그라운드 API용)
    List<GroupBuy> findByPostStatusAndDueDateBefore(String postStatus, LocalDateTime now);

    // 공구 종료 조건 기반 조회 (백그라운드 API용)
    List<GroupBuy> findByPostStatusAndPickupDateLessThanEqual(String postStatus, LocalDateTime now);

    // 공구 주최 리스트 첫 조회
    List<GroupBuy> findByUser_IdAndPostStatus(Long userId, String postStatus, Pageable pageable);

    // 공구 주최 리스트 이어서 조회
    List<GroupBuy> findByUser_IdAndPostStatusAndIdLessThan(
            Long userId,
            String postStatus,
            Long cursorId,
            Pageable pageable);

    boolean existsGroupBuyByUserIdAndPostStatusNot(Long userId, String postStatus);

    // 게시글 조회 - 공구 게시글 수정 전 정보, 공구 게시글 상세
    @EntityGraph(attributePaths = "images")
    Optional<GroupBuy> findWithImagesById(Long id);

}
