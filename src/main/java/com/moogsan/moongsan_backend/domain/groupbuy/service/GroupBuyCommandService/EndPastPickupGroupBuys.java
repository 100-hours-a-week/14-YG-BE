package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EndPastPickupGroupBuys {

    private final GroupBuyRepository groupBuyRepository;

    /// 공구 종료 (백그라운드 API)
    public void endPastPickupGroupBuys(LocalDateTime now) {
        List<GroupBuy> toEnd = groupBuyRepository
                .findByPostStatusAndPickupDateBefore("CLOSED", now);

        for (GroupBuy gb : toEnd) {
            gb.changePostStatus("ENDED");
        }
        groupBuyRepository.saveAll(toEnd);
    }
}
