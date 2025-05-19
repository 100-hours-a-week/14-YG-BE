package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.client.AiClient;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateGroupBuy {
    private final GroupBuyRepository groupBuyRepository;
    private final ImageMapper imageMapper;
    private final GroupBuyCommandMapper groupBuyCommandMapper;

    /// 공구 게시글 작성
    public Long createGroupBuy(User currentUser, CreateGroupBuyRequest createGroupBuyRequest) {

        int total = createGroupBuyRequest.getTotalAmount();
        int unit  = createGroupBuyRequest.getUnitAmount();

        if (unit == 0 || total % unit != 0) {
            throw new GroupBuyInvalidStateException("상품 주문 단위는 상품 전체 수량의 약수여야 합니다.");
        }

        // GroupBuy 기본 필드 매핑 (팩토리 메서드 사용)
        GroupBuy gb = groupBuyCommandMapper.create(createGroupBuyRequest, currentUser);

        imageMapper.mapImagesToGroupBuy(createGroupBuyRequest.getImageKeys(), gb);

        gb.increaseParticipantCount();
        groupBuyRepository.save(gb);

        return gb.getId();
    }
}
