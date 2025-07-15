package com.moogsan.moongsan_backend.groupbuy.policy;

import com.moogsan.moongsan_backend.groupbuy.entity.GroupBuy;
import org.springframework.stereotype.Component;

@Component
public class DueSoonPolicy {

    private static final double THRESHOLD = 0.8;

    public boolean isDueSoon(GroupBuy gb) {
        int sold = gb.getTotalAmount() - gb.getLeftAmount();
        return sold >= Math.floor(gb.getTotalAmount() * THRESHOLD);
    }
}
