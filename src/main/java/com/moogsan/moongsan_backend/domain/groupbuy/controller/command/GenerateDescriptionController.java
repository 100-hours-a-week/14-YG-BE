package com.moogsan.moongsan_backend.domain.groupbuy.controller.command;

import com.moogsan.moongsan_backend.domain.WrapperResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.DescriptionGenerationRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.DescriptionDto;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.domain.user.entity.CustomUserDetails;
import com.moogsan.moongsan_backend.global.exception.specific.UnauthenticatedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GENERATE_SUCCESS;
import static com.moogsan.moongsan_backend.global.util.CookieUtils.extractCookie;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-buys/generation")
public class GenerateDescriptionController {

    private final GroupBuyCommandFacade groupBuyFacade;

    @PostMapping("/description")
    public Mono<ResponseEntity<WrapperResponse<DescriptionDto>>> generateDescription(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid DescriptionGenerationRequest req,
            HttpServletRequest servletRequest) {

        if (userDetails == null) throw new UnauthenticatedAccessException("로그인이 필요합니다.");

        String sessionId = extractCookie(servletRequest, "AccessToken");

        return groupBuyFacade.generateDescription(req.getUrl(), sessionId)
                .map(data -> ResponseEntity.ok(
                        new WrapperResponse<>(GENERATE_SUCCESS, data)))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(new WrapperResponse<>(e.getMessage(), null))))
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new WrapperResponse<>("서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", null))));
    }
}
