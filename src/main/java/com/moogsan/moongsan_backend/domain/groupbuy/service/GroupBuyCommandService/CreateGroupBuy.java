package com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyCommandService;

import com.moogsan.moongsan_backend.domain.chatting.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.domain.image.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.image.service.S3Service;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.global.exception.specific.DuplicateRequestException;
import com.moogsan.moongsan_backend.global.lock.DuplicateRequestPreventer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.NOT_DIVISOR;

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

    /// 공구 게시글 작성
    public Long createGroupBuy(User currentUser, CreateGroupBuyRequest createGroupBuyRequest) {

        int total = createGroupBuyRequest.getTotalAmount();
        int unit  = createGroupBuyRequest.getUnitAmount();

        Long userId = currentUser.getId();
        String key = "group-buy:creating:" + userId;

        if (!duplicateRequestPreventer.tryAcquireLock(key, 3)) {
            throw new DuplicateRequestException();
        }

        if (unit == 0 || total % unit != 0) {
            throw new GroupBuyInvalidStateException(NOT_DIVISOR);
        }

        GroupBuy gb = groupBuyCommandMapper.create(createGroupBuyRequest, currentUser);

        // S3 파일 이동
        String destPrefix = "group-buys";
        List<String> destKeys = createGroupBuyRequest.getImageKeys().stream()
            .map(srcKey -> {
                String fileName = srcKey.substring(srcKey.lastIndexOf('/') + 1);
                String destKey  = destPrefix + "/" + fileName;
                s3Service.moveImage(srcKey, destKey);
                return destKey;
            }).toList();

        imageMapper.mapImagesToGroupBuy(destKeys, gb);
        gb.increaseParticipantCount();
        groupBuyRepository.save(gb);

        Long chatRoomId = chattingCommandFacade.joinChatRoom(currentUser, gb.getId());

        return gb.getId();
    }
}
