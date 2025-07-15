package com.moogsan.moongsan_backend.image.repository;

import com.moogsan.moongsan_backend.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
