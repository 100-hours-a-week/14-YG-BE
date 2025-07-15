package com.moogsan.moongsan_backend.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Long receiverId;
    private String title;
    private String body;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> data;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    @Builder.Default
    @Column(columnDefinition = "bit(1) default 0", name = "`read`")
    private Boolean read = false;

    public void markAsRead(Boolean read) {
        if (!Boolean.TRUE.equals(this.read)) {
            this.read = true;
            this.readAt = LocalDateTime.now();
        }
    }
}
