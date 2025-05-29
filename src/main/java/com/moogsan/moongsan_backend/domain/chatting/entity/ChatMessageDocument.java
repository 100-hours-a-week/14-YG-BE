package com.moogsan.moongsan_backend.domain.chatting.entity;

import com.moogsan.moongsan_backend.domain.chatting.BaseDocument;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatMessageDocument extends BaseDocument {
    @Id
    private String id;

    @Field("chat_room_id")
    private Long chatRoomId;
    private Long messageSeq;

    @Field("participant_id")
    private Long chatParticipantId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Builder.Default
    private int viewCount = 0;
}
