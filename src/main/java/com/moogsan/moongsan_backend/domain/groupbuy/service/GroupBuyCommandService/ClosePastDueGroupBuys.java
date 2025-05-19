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
public class ClosePastDueGroupBuys {

    private final GroupBuyRepository groupBuyRepository;

    ///  공구 모집 마감(백그라운드 API)
    public void closePastDueGroupBuys(LocalDateTime now) {
        List<GroupBuy> expired = groupBuyRepository
                .findByPostStatusAndDueDateBefore("OPEN", now);
        for (GroupBuy gb : expired) {
            gb.changePostStatus("CLOSED");
        }
        groupBuyRepository.saveAll(expired);
    }
}
