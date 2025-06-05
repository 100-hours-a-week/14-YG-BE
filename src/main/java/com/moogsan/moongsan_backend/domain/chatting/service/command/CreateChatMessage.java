package com.moogsan.moongsan_backend.domain.chatting.service.command;

import com.moogsan.moongsan_backend.domain.chatting.dto.command.request.CreateChatMessageRequest;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatMessageDocument;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatParticipant;
import com.moogsan.moongsan_backend.domain.chatting.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomInvalidStateException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.ChatRoomNotFoundException;
import com.moogsan.moongsan_backend.domain.chatting.exception.specific.NotParticipantException;
import com.moogsan.moongsan_backend.domain.chatting.mapper.ChatMessageCommandMapper;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatMessageRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatParticipantRepository;
import com.moogsan.moongsan_backend.domain.chatting.repository.ChatRoomRepository;
import com.moogsan.moongsan_backend.domain.chatting.service.query.GetLatestMessages;
import com.moogsan.moongsan_backend.domain.chatting.util.MessageSequenceGenerator;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateChatMessage {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageSequenceGenerator messageSequenceGenerator;
    private final ChatMessageCommandMapper chatMessageCommandMapper;
    private final GetLatestMessages getLatestMessages;

    public void createChatMessage(User currentUser, CreateChatMessageRequest request, Long chatRoomId) {

        // 채팅방 조회 -> 없으면 404
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        // 채팅방이 삭제되지 않았는지 조회
        if(chatRoom.getDeletedAt() != null) {
            throw new ChatRoomInvalidStateException("삭제된 채팅방에는 메세지를 보낼 수 없습니다.");
        }

        // 참여자인지 조회 -> 아니면 403
        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId());

        if(!isParticipant) {
            throw new NotParticipantException("참여자만 메세지를 작성할 수 있습니다.");
        }

        ChatParticipant participant = chatParticipantRepository
                .findByChatRoom_IdAndUser_IdAndLeftAtIsNull(chatRoomId, currentUser.getId())
                .orElseThrow(() -> new NotParticipantException("참여자만 메시지를 작성할 수 있습니다."));


        // 메세지 순번 생성 (커서 기반 페이징용)
        Long nextSeq = messageSequenceGenerator.getNextMessageSeq(chatRoomId);

        // 메세지 작성
        ChatMessageDocument document = chatMessageCommandMapper
                .toMessageDocument(chatRoom, participant.getId(), request, nextSeq);

        SecurityContext context = SecurityContextHolder.getContext();
        getLatestMessages.notifyNewMessage(document, currentUser.getNickname(), currentUser.getImageKey(), context);

        chatMessageRepository.save(document);
    }
}
