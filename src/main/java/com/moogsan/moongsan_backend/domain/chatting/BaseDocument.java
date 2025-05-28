package com.moogsan.moongsan_backend.domain.chatting;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Getter
public abstract class BaseDocument {
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("modified_at")
    private LocalDateTime modifiedAt;

    @Field("deleted_at")
    private LocalDateTime deletedAt;
}

