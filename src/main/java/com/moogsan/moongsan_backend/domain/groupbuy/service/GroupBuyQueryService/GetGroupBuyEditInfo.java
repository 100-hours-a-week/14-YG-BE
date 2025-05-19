package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyQueryMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class GetGroupBuyEditInfo {

    private final GroupBuyRepository groupBuyRepository;
    private final GroupBuyQueryMapper groupBuyQueryMapper;

    /// 공구 게시글 수정 전 정보 조회
    /// TODO V2
    public GroupBuyForUpdateResponse getGroupBuyEditInfo(Long postId) {
        GroupBuy groupBuy = groupBuyRepository.findWithImagesById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        return groupBuyQueryMapper.toUpdateResponse(groupBuy);
    }
}
