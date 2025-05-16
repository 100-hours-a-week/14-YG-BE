package com.moogsan.moongsan_backend.domain.groupbuy.service;

import com.moogsan.moongsan_backend.domain.groupbuy.client.AiClient;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.DescriptionDto;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotFoundException;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyNotHostException;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.GroupBuyCommandMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.mapper.ImageMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderInvalidStateException;
import com.moogsan.moongsan_backend.domain.order.exception.specific.OrderNotFoundException;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GroupBuyCommandService {

    private final GroupBuyRepository groupBuyRepository;
    private final ImageMapper imageMapper;
    private final GroupBuyCommandMapper groupBuyCommandMapper;
    private final OrderRepository orderRepository;
    private final AiClient aiClient;
    private final DueSoonPolicy dueSoonPolicy;

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


    /// 공구 게시글 상세 설명 생성
    public Mono<DescriptionDto> generate(String url, String sessionId) {
        return aiClient.generateDescription(url, sessionId);
    }


    /// 공구 게시글 수정
    // TODO V2
    public Long updateGroupBuy(User currentUser, UpdateGroupBuyRequest updateGroupBuyRequest, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("공구 수정은 공구가 열려있는 상태에서만 가능합니다.");
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException("공구 수정은 공구의 주최자만 요청 가능합니다.");
        }

        // GroupBuy 기본 필드 매핑 (팩토리 메서드 사용)
        GroupBuy gb = groupBuy.updateForm(updateGroupBuyRequest);

        ///  TODO: 기존 이미지 처리 로직 필요!
        imageMapper.mapImagesToGroupBuy(updateGroupBuyRequest.getImageUrls(), gb);

        groupBuyRepository.save(gb);

        return gb.getId();
    }

    /// 공구 게시글 삭제: 참여자가 아무도 없는, 주문 레코드가 없는 경우이므로 하드 삭제
    // TODO V2
    public void deleteGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 아니면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구의 status가 open인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
                || groupBuy.getDueDate().isBefore(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("공구 삭제는 공구가 열려있는 상태에서만 가능합니다.");
        }

        // 해당 공구의 참여자가 0명인지 조회 -> 아니면 409
        int participantCount = orderRepository.countByGroupBuyId(postId);
        if(participantCount != 0) {
            throw new GroupBuyInvalidStateException("참여자가 1명 이상일 경우 공구를 삭제할 수 없습니다.");
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException("공구 삭제는 공구의 주최자만 요청 가능합니다.");
        }

        groupBuyRepository.delete(groupBuy);

    }

    /// 공구 참여 취소
    public void leaveGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 OPEN인지 조회, dueDate가 현재 이후인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("OPEN")
            || groupBuy.getDueDate().isBefore(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("공구 참여 취소는 공구가 열려있는 상태에서만 가능합니다.");
        }

        // 해당 공구의 주문 테이블에 해당 유저의 주문이 존재하는지 조회 -> 아니면 404
        Order order = orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(currentUser.getId(), groupBuy.getId(), "CANCELED")
                .orElseThrow(OrderNotFoundException::new);

        // 해당 주문의 상태가 canceled가 아닌지 조회 -> 아니면 409
        if (order.getStatus().equals("CANCELED")) {
            throw new OrderInvalidStateException("이미 취소된 주문입니다.");
        }

        // 해당 주문의 상태가 paid인지 조회
        if (order.getStatus().equals("PAID")) {
            // 별도의 환불 로직 처리 필요
        }

        // 남은 수량, 참여 인원 수 업데이트
        int returnQuantity = order.getQuantity();
        groupBuy.increaseLeftAmount(returnQuantity);
        groupBuy.decreaseParticipantCount();

        // 해당 유저의 주문을 취소
        order.setStatus("CANCELED");

        groupBuy.updateDueSoonStatus(dueSoonPolicy);

        orderRepository.save(order);

    }

    ///  공구 모집 마감(백그라운드 API)
    public void closePastDueGroupBuys(LocalDateTime now) {
        List<GroupBuy> expired = groupBuyRepository
                .findByPostStatusAndDueDateBefore("OPEN", now);
        for (GroupBuy gb : expired) {
            gb.changePostStatus("CLOSED");
        }
        groupBuyRepository.saveAll(expired);
    }

    /// 공구 종료 (백그라운드 API)
    public void endPastPickupGroupBuys(LocalDateTime now) {
        List<GroupBuy> toEnd = groupBuyRepository
                .findByPostStatusAndPickupDateBefore("CLOSED", now);

        for (GroupBuy gb : toEnd) {
            gb.changePostStatus("ENDED");
        }
        groupBuyRepository.saveAll(toEnd);
    }


    /// 공구 게시글 공구 종료
    public void endGroupBuy(User currentUser, Long postId) {

        // 해당 공구가 존재하는지 조회 -> 없으면 404
        GroupBuy groupBuy = groupBuyRepository.findById(postId)
                .orElseThrow(GroupBuyNotFoundException::new);

        // 해당 공구가 CLOSED인지 조회 -> 아니면 409
        if (!groupBuy.getPostStatus().equals("CLOSED")) {
            throw new GroupBuyInvalidStateException("공구 종료는 모집 마감 이후에만 가능합니다.");
        }

        // 해당 공구가 ENDED인지 조회 -> 맞으면 409
        if (groupBuy.getPostStatus().equals("ENDED")) {
            throw new GroupBuyInvalidStateException("이미 종료된 공구입니다.");
        }

        // dueDate 이후인지 조회 -> 아니면 409
        if (groupBuy.getDueDate().isAfter(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("공구 종료는 공구 마감 일자 이후에만 가능합니다.");
        }

        // pickupDate 이후인지 조회 -> 아니면 409
        if (groupBuy.getPickupDate().isAfter(LocalDateTime.now())) {
            throw new GroupBuyInvalidStateException("공구 종료는 공구 픽업 일자 이후에만 가능합니다.");
        }

        // 해당 공구의 주최자가 해당 유저인지 조회 -> 아니면 403
        if(!groupBuy.getUser().getId().equals(currentUser.getId())) {
            throw new GroupBuyNotHostException("공구 종료는 공구의 주최자만 요청 가능합니다.");
        }

        //공구 게시글 status ENDED로 변경
        groupBuy.changePostStatus("ENDED");

        // TODO V2, V3에서는 참여자 채팅방 해제 카운트 시작(2주- CS 고려), 익명 채팅방 즉시 해제

    }

}