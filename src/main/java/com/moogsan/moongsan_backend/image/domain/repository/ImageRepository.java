package com.moogsan.moongsan_backend.image.domain.repository;

import com.moogsan.moongsan_backend.image.domain.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
