package com.moogsan.moongsan_backend.domain.chatting.dto.command.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChatMessageRequest {
    // 본문
    @Field("messageContent")
    @NotBlank(message = "채팅 메세지는 1자 이상 1000자 이하로 입력해야 합니다.")
    @Size(min = 1, max = 1000, message = "채팅 메세지는 1자 이상 1000자 이하로 입력해야 합니다.")
    private String messageContent;     // 채팅 메세지 내용
}
