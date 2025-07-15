package com.moogsan.moongsan_backend.groupbuy.domain.repository;

import com.moogsan.moongsan_backend.groupbuy.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
