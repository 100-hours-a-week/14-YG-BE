package com.moogsan.moongsan_backend.groupbuy.application.service.command;

import com.moogsan.moongsan_backend.participantchat.application.facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.groupbuy.domain.entity.GroupBuy;
import com.moogsan.moongsan_backend.groupbuy.domain.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.groupbuy.application.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.image.application.mapper.ImageMapper;
import com.moogsan.moongsan_backend.groupbuy.domain.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.image.application.service.S3Service;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.exception.specific.DuplicateRequestException;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.order.constant.OrderConstants.GROUPBUY_STOCK_PREFIX;
import static com.moogsan.moongsan_backend.groupbuy.domain.constant.GroupBuyConstants.DUPLICATE_LOCK_PREFIX;
import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.NOT_DIVISOR;
import static com.moogsan.moongsan_backend.image.domain.constant.ImageConstants.GROUP_BUYS_PREFIX;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateGroupBuy {
    private final GroupBuyRepository groupBuyRepository;
    private final ImageMapper imageMapper;
    private final GroupBuyCommandMapper groupBuyCommandMapper;
    private final ChattingCommandFacade chattingCommandFacade;
    private final DuplicateRequestPreventer duplicateRequestPreventer;
    private final S3Service s3Service;
    private final Clock clock;
    private final RedisTemplate<String, String> redisTemplate;

    /// 공구 게시글 작성
    public Long createGroupBuy(User currentUser, CreateGroupBuyRequest request) {

        // 중복 게시글 작성 방지
        preventDuplicate(currentUser);

        // 약수 보장
        validateDivisor(request);

        // 게시글 생성
        GroupBuy groupBuy = groupBuyCommandMapper.create(request, currentUser);

        // 이미지 S3 파일 이동
        moveAndMapImages(request, groupBuy);

        // 주문 수량 및 공동구매 게시글 상태 전환
        adjustStockAndStatus(request, groupBuy);

        // 주문 관리용 Redis 재고 초기화
        redisTemplate.opsForValue().set(GROUPBUY_STOCK_PREFIX + groupBuy.getId(), String.valueOf(groupBuy.getLeftAmount()));

        chattingCommandFacade.joinChatRoom(currentUser, groupBuy.getId());

        return groupBuy.getId();
    }

    private void preventDuplicate(User user) {
        String key = DUPLICATE_LOCK_PREFIX + user.getId();
        if (!duplicateRequestPreventer.tryAcquireLock(key, 3)) {
            throw new DuplicateRequestException();
        }
    }

    private void validateDivisor(CreateGroupBuyRequest request) {
        int total = request.getTotalAmount();
        int unit  = request.getUnitAmount();

        if (unit == 0 || total % unit != 0) {
            throw new GroupBuyInvalidStateException(NOT_DIVISOR);
        }
    }

    private void moveAndMapImages(CreateGroupBuyRequest request, GroupBuy groupBuy) {
        List<String> destKeys = request.getImageKeys().stream()
                .map(srcKey -> {
                    String fileName = srcKey.substring(srcKey.lastIndexOf('/') + 1);
                    String destKey  = GROUP_BUYS_PREFIX + "/" + fileName;
                    s3Service.moveImage(srcKey, destKey);
                    return destKey;
                }).toList();

        imageMapper.mapImagesToGroupBuy(destKeys, groupBuy);
    }

    private void adjustStockAndStatus(CreateGroupBuyRequest request, GroupBuy groupBuy) {
        groupBuy.decreaseLeftAmount(request.getHostQuantity());
        groupBuy.increaseParticipantCount();

        if (groupBuy.getLeftAmount() == 0) {
            groupBuy.changePostStatus("CLOSED");
        }
        groupBuyRepository.save(groupBuy);
    }
}
