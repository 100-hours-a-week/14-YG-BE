package com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ValidationMessage.MESSAGE_SIZE;

@Getter
public class ChatMessageRequest {

    @NotNull(message = MESSAGE_SIZE)
    @NotBlank(message = MESSAGE_SIZE)
    @Size(min = 1, max = 1000, message = MESSAGE_SIZE)
    private String message;

    private String sessionId;
}
