package com.moogsan.moongsan_backend.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleMessage {
    private String sender;
    private String content;
}
