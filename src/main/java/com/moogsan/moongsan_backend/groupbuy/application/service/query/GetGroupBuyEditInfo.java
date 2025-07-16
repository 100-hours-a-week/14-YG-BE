package com.moogsan.moongsan_backend.groupbuy.application.service.query;

import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
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
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyEditInfo {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;
    private final Clock clock;

    /// 공구 게시글 수정 전 정보 조회
    public GroupBuyForUpdateResponse getGroupBuyEditInfo(Long userId, Long postId) {

        GroupBuy groupBuy = fetchAndValidate(userId, postId);
        return groupBuyQueryMapper.toUpdateResponse(groupBuy);
    }

    private GroupBuy fetchAndValidate(Long userId, Long postId) {

        GroupBuy groupBuy = groupBuyRepository.findWithImagesById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        if (!groupBuy.getUser().getId().equals(userId)) {
            throw new GroupBuyNotHostException(NOT_HOST);
        }

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now(clock))) {
            throw new GroupBuyInvalidStateException(NOT_OPEN);
        }

        return groupBuy;
    }
}
