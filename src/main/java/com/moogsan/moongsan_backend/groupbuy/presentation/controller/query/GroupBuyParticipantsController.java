package com.moogsan.moongsan_backend.groupbuy.presentation.controller.query;

import com.moogsan.moongsan_backend.global.dto.WrapperResponse;
import com.moogsan.moongsan_backend.global.security.annotation.RequireLogin;
import com.moogsan.moongsan_backend.groupbuy.presentation.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.groupbuy.application.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.moogsan.moongsan_backend.groupbuy.domain.message.ResponseMessage.GET_PARTICIPANTS_SUCCESS;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/{postId}/participants")
@RequireLogin
public class GroupBuyParticipantsController {

    private final GroupBuyQueryFacade queryFacade;

    @GetMapping
    public ResponseEntity<WrapperResponse<ParticipantListResponse>> getGroupBuyParticipantsInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId) {

        ParticipantListResponse participantList = queryFacade.getGroupBuyParticipantsInfo(
                userDetails.getUser().getId(), postId);
        return ResponseEntity.ok(
                WrapperResponse.<ParticipantListResponse>builder()
                        .message(GET_PARTICIPANTS_SUCCESS)
                        .data(participantList)
                        .build()
        );
    }
}

