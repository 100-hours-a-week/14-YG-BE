package com.moogsan.moongsan_backend.groupbuy.domain.repository;

import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBuyCategoryRepository extends JpaRepository<GroupBuyCategory, Long> {
    // 특정 공구의 카테고리 목록 조회
    List<GroupBuyCategory> findByGroupBuyId(Long groupBuyId);

    // 특정 카테고리에 속한 공구 목록 조회
    List<GroupBuyCategory> findByCategoryId(Long categoryId);
}
