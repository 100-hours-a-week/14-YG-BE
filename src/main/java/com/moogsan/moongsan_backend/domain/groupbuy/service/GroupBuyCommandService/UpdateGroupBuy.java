package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateGroupBuy {
    private final GroupBuyRepository groupBuyRepository;
    private final ImageMapper imageMapper;

    /// 공구 게시글 수정
    // TODO V2
    public Long updateGroupBuy(User currentUser, UpdateGroupBuyRequest updateGroupBuyRequest, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        // GroupBuy 기본 필드 매핑 (팩토리 메서드 사용)
        GroupBuy gb = groupBuy.updateForm(updateGroupBuyRequest);

        ///  TODO: 기존 이미지 처리 로직 필요!
        imageMapper.mapImagesToGroupBuy(updateGroupBuyRequest.getImageKeys(), gb);

        groupBuyRepository.save(gb);

        return gb.getId();
    }
}
