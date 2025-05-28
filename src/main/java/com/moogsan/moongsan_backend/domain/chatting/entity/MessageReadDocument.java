package com.moogsan.moongsan_backend.domain.chatting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "message_read")
@CompoundIndex(name = "idx_part_msg", def = "{'participant_id':1,'chat_message_id':1}", unique = true)
public class MessageRead {
    @Id
    private Long id;

    @Field("chat_message_id")
    private String chatMessageId;

    @Field("participant_id")
    private Long chatParticipantId;

    @Field("read_at")
    private LocalDateTime readAt;
}
