package com.moogsan.moongsan_backend.domain.groupbuy.service.useCase;

import com.moogsan.moongsan_backend.domain.notification.template.NotificationTemplateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BroadcastGroupBuyUpdatedEventUseCase {

    private final NotificationTemplateRegistry templateRegistry;
    private final
}
