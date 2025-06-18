package com.moogsan.moongsan_backend.domain.groupbuy.mapper;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupBuyCommandMapper {

    private final DueSoonPolicy dueSoonPolicy;

    // 공구 게시글 생성 팩토리 메서드
    public GroupBuy create(CreateGroupBuyRequest req, User host) {
        int unitPrice = (int) Math.round((double) req.getPrice() / req.getTotalAmount());
        int leftAmount = req.getTotalAmount() - req.getHostQuantity();

        GroupBuy gb = GroupBuy.builder()
                .title(req.getTitle())
                .name(req.getName())
                .url(req.getUrl())
                .price(req.getPrice())
                .unitPrice(unitPrice)
                .totalAmount(req.getTotalAmount())
                .hostQuantity(req.getHostQuantity())
                .leftAmount(leftAmount)
                .unitAmount(req.getUnitAmount())
                .description(req.getDescription())
                .dueDate(req.getDueDate())
                .location(req.getLocation())
                .pickupDate(req.getPickupDate())
                .user(host)
                .build();
        gb.updateDueSoonStatus(dueSoonPolicy);
        return gb;
    }
}
