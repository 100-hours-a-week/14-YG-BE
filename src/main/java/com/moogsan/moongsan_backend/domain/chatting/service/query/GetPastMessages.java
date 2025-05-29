package com.moogsan.moongsan_backend.domain.chatting.service.query;

import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessagePageResponse;
import com.moogsan.moongsan_backend.domain.chatting.dto.query.ChatMessageResponse;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GetPastMessages {

    private static final int PAGE_SIZE = 10;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MongoTemplate mongoTemplate;

    public ChatMessagePageResponse getPastMessages(
            User currentUser,
            Long chatRoomId,
            String cursorId
    ) {

        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 참여자인지 조회 -> 아니면 403
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_Id(chatRoomId, currentUser.getId());

        if(!isParticipant) {
            throw new NotParticipantException("참여자만 메세지를 조회할 수 있습니다.");
        }

        // 커서 기반 필터 생성
        Query q = new Query();
        if (cursorId == null || !ObjectId.isValid(cursorId)) {
            q.addCriteria(Criteria.where("chatRoomId").is(chatRoomId));
        } else {
            q.addCriteria(new Criteria().andOperator(
                    Criteria.where("chatRoomId").is(chatRoomId),
                    Criteria.where("_id").lt(new ObjectId(cursorId))
            ));
        }

        // 정렬 및 조회 (limit = PAGE_SIZE + 1)
        q.with(Sort.by(
                        Sort.Order.desc("_id")
                ))
                .limit(PAGE_SIZE + 1);

        List<ChatMessageDocument> cursorDocs =
                mongoTemplate.find(q, ChatMessageDocument.class);


        // hasNext 판단 & 실제 페이지 사이즈로 자르기
        boolean hasNext = cursorDocs.size() > PAGE_SIZE;
        List<ChatMessageDocument> page = cursorDocs.stream()
                .limit(PAGE_SIZE)
                .toList();

        // 빈 페이지인 경우 바로 리턴
        if (page.isEmpty()) {
            return ChatMessagePageResponse.builder()
                    .chatMessageResponses(Collections.emptyList())
                    .nextCursorId(null)         // 또는 cursorId 그대로
                    .hasNext(false)
                    .build();
        }

        log.info("📦 찾은 메시지 수: {}", page.size());
        page.forEach(m ->
                log.info("📨 messageSeq: {}, content: {}", m.getMessageSeq(), m.getContent())
        );

        // 참여자 정보 매핑
        Set<Long> participantIds = page.stream()
                .map(ChatMessageDocument::getChatParticipantId)
                .collect(Collectors.toSet());
        List<ChatParticipant> participants = chatParticipantRepository.findAllById(participantIds);
        Map<Long, User> participantIdToUser = participants.stream()
                .collect(Collectors.toMap(
                        ChatParticipant::getId,
                        ChatParticipant::getUser
                ));

        ChatMessageDocument last = page.getLast();
        String nextCursorId    = last.getId();
        LocalDateTime nextTime = last.getCreatedAt();

        // DTO 변환 후 반환
        List<ChatMessageResponse> responses =  page.stream()
                .map(doc -> {
                    User user = participantIdToUser.get(doc.getChatParticipantId());
                    String nickname = (user != null ? user.getNickname() : "알수없음");
                    String imageKey = (user != null ? user.getImageKey() : null);
                    return chatMessageMapper.toMessageResponse(doc, nickname, imageKey);
                })
                .collect(Collectors.toList());

        return ChatMessagePageResponse.builder()
                .chatMessageResponses(responses)
                .nextCursorId(nextCursorId)
                .nextCreatedAt(nextTime)
                .hasNext(hasNext)
                .build();
    }
}
