package com.moogsan.moongsan_backend.groupbuy.application.service.command;

import com.moogsan.moongsan_backend.groupbuy.domain.service.GroupBuyEventService;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.image.application.service.ImageService;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_HOST;
import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_OPEN;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateGroupBuy {

    private final GroupBuyEventService groupBuyEventService;
    private final ImageService imageService;
    private final GroupBuyRepository groupBuyRepository;
    private final Clock clock;

    /// 공구 게시글 수정
    public Long updateGroupBuy(User currentUser, UpdateGroupBuyRequest updateGroupBuyRequest, Long postId) {

        // 유효성 검사
        GroupBuy groupBuy = fetchAndValidate(currentUser.getId(), postId);

        // GroupBuy 기본 필드 매핑 (팩토리 메서드 사용)
        groupBuy = groupBuy.updateForm(updateGroupBuyRequest);

        // 기존 S3 파일 삭제 및 파일 이동
        imageService.syncUpdatedImages(updateGroupBuyRequest, groupBuy);

        // 픽업 일자 수정 이벤트 발행
        if (updateGroupBuyRequest.getDateModificationReason() != null) {
            groupBuyEventService.publishPickupUpdated(groupBuy);
        }

        return groupBuy.getId();
    }

    private GroupBuy fetchAndValidate(Long userId, Long postId) {
        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now(clock))) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(userId)) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }
        return groupBuy;
    }
}
