package com.moogsan.moongsan_backend.domain.groupbuy.repository;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
