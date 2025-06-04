package com.moogsan.moongsan_backend.domain.groupbuy.mapper;

import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.ImageResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GroupBuyQueryMapper {

    // 수정 정보 조회용 DTO 변환
    public GroupBuyForUpdateResponse toUpdateResponse(GroupBuy gb){
        List<ImageResponse> imageUrls = gb.getImages().stream()
                .map(image -> ImageResponse.builder()
                        .imageKey(image.getImageKey())
                        .imageSeqNo(image.getImageSeqNo())
                        .thumbnail(image.isThumbnail())
                        .build())
                .toList();

        return GroupBuyForUpdateResponse.builder()
                .title(gb.getTitle())
                .name(gb.getName())
                .description(gb.getDescription())
                .url(gb.getUrl())
                .imageKeys(imageUrls)
                .dueDate(gb.getDueDate())
                .location(gb.getLocation())
                .pickupDate(gb.getPickupDate())
                .price(gb.getPrice())
                .unitAmount(gb.getUnitAmount())
                .totalAmount(gb.getTotalAmount())
                .build();
    }

    // 공구 리스트 관심 조회
    public List<BasicListResponse> toBasicListWishResponses(
            List<GroupBuy> groupBuys,
            Map<Long, Boolean> wishMap
    ) {
        return groupBuys.stream()
                .map(gb -> {
                    boolean isWished = wishMap.getOrDefault(gb.getId(), false);
                    return toBasicListResponse(gb, isWished);
                })
                .collect(Collectors.toList());
    }

    // 공구 리스트 조회용 DTO 변환
    public BasicListResponse toBasicListResponse(GroupBuy g, Boolean isWish) {
        List<ImageResponse> imageKeys = g.getImages().stream()
                .map(img -> ImageResponse.builder()
                        .imageKey(img.getImageKey())
                        .imageSeqNo(img.getImageSeqNo())
                        .thumbnail(img.isThumbnail())
                        .build())
                .collect(Collectors.toList());

        return BasicListResponse.builder()
                .postId(g.getId())
                .title(g.getTitle())
                .name(g.getName())
                .postStatus(g.getPostStatus())
                .imageKeys(imageKeys)
                .unitPrice(g.getUnitPrice())
                .unitAmount(g.getUnitAmount())
                .soldAmount(g.getTotalAmount() - g.getLeftAmount())
                .totalAmount(g.getTotalAmount())
                .participantCount(g.getParticipantCount())
                .dueSoon(g.isAlmostSoldOut())
                .isWish(isWish)
                .createdAt(g.getCreatedAt())
                .build();
    }

    // 상세 페이지 조회용 DTO
    public DetailResponse toDetailResponse(GroupBuy gb, Boolean isParticipant, Boolean isWish) {
        List<ImageResponse> imageUrls = gb.getImages().stream()
                .map(img -> ImageResponse.builder()
                        .imageKey(img.getImageKey())
                        .imageSeqNo(img.getImageSeqNo())
                        .thumbnail(img.isThumbnail())
                        .build())
                .toList();

        return DetailResponse.builder()
                .postId(gb.getId())
                .title(gb.getTitle())
                .name(gb.getName())
                .postStatus(gb.getPostStatus())
                .description(gb.getDescription())
                .url(gb.getUrl())
                .imageKeys(imageUrls)
                .unitPrice(gb.getUnitPrice())
                .unitAmount(gb.getUnitAmount())
                .soldAmount(gb.getTotalAmount() - gb.getLeftAmount())
                .totalAmount(gb.getTotalAmount())
                .leftAmount(gb.getLeftAmount())
                .participantCount(gb.getParticipantCount())
                .dueDate(gb.getDueDate())
                .dueSoon(gb.isAlmostSoldOut())
                .pickupDate(gb.getPickupDate())
                .location(gb.getLocation())
                .isParticipant(isParticipant)
                .isWish(isWish)
                .createdAt(gb.getCreatedAt())
                .userProfileResponse(toUserProfile(gb.getUser()))
                .build();
    }

    // 공동구매 유저 프로필 조회용 DTO
    private UserProfileResponse toUserProfile(User u) {
        return UserProfileResponse.builder()
                .authorId(u.getId())
                .nickname(u.getNickname())
                .profileImageUrl(u.getImageKey())
                .build();
    }

    // 공동구매 유저 계좌 정보 조회용 DTO
    public UserAccountResponse toHostAccount(GroupBuy gb) {
        return UserAccountResponse.builder()
                .name(gb.getUser().getName())
                .accountBank(gb.getUser().getAccountBank())
                .accountNumber(gb.getUser().getAccountNumber())
                .build();
    }

    // 관심 공구 리스트 조회
    public WishListResponse toWishListResponse(GroupBuy gb) {
        String img = gb.getImages().stream()
                .findFirst()
                .map(Image::getImageKey)
                .orElse(null);

        boolean dueSoon = "OPEN".equals(gb.getPostStatus())
                && gb.getDueDate().isAfter(LocalDateTime.now())
                && gb.getDueDate().isBefore(LocalDateTime.now().plusDays(3));

        return WishListResponse.builder()
                .postId(gb.getId())
                .title(gb.getTitle())
                .postStatus(gb.getPostStatus())
                .location(gb.getLocation())
                .imageKey(img)
                .unitPrice(gb.getUnitPrice())
                .soldAmount(gb.getTotalAmount() - gb.getLeftAmount())
                .totalAmount(gb.getTotalAmount())
                .participantCount(gb.getParticipantCount())
                .isWish(true)
                .dueSoon(dueSoon)
                .build();
    }

    // 주최 공구 관심 조회
    public List<HostedListResponse> toHostedListWishResponses(
            List<GroupBuy> groupBuys,
            Map<Long, Boolean> wishMap
    ) {
        return groupBuys.stream()
                .map(gb -> {
                    boolean isWished = wishMap.getOrDefault(gb.getId(), false);
                    return toHostedListResponse(gb, isWished);
                })
                .collect(Collectors.toList());
    }

    // 주최 공구 리스트 조회
    public HostedListResponse toHostedListResponse(GroupBuy gb, Boolean isWish) {
        String img = gb.getImages().stream()
                .findFirst()
                .map(Image::getImageKey)
                .orElse(null);

        boolean dueSoon = "OPEN".equals(gb.getPostStatus())
                && gb.getDueDate().isAfter(LocalDateTime.now())
                && gb.getDueDate().isBefore(LocalDateTime.now().plusDays(3));

        return HostedListResponse.builder()
                .postId(gb.getId())
                .title(gb.getTitle())
                .postStatus(gb.getPostStatus())
                .location(gb.getLocation())
                .imageKey(img)
                .unitPrice(gb.getUnitPrice())
                .hostQuantity(gb.getHostQuantity())
                .soldAmount(gb.getTotalAmount() - gb.getLeftAmount())
                .totalAmount(gb.getTotalAmount())
                .participantCount(gb.getParticipantCount())
                .isWish(isWish)
                .dueSoon(dueSoon)
                .build();
    }

    // 참여 공구 관심 조회
    public List<ParticipatedListResponse> toParticipatedListWishResponse(List<Order> orders, Map<Long, Boolean> wishMap) {
        return orders.stream()
                .map(order -> {
                    boolean isWish = wishMap.getOrDefault(order.getGroupBuy().getId(), false);
                    return toParticipatedListResponse(order, isWish);
                })
                .toList();
    }

    // 참여 공구 리스트 조회
    public ParticipatedListResponse toParticipatedListResponse(Order o, boolean isWish) {
        GroupBuy post = o.getGroupBuy();

        String img = post.getImages().stream()
                .findFirst()
                .map(Image::getImageKey)
                .orElse(null);

        boolean dueSoon = "OPEN".equals(post.getPostStatus())
                && post.getDueDate().isAfter(LocalDateTime.now())
                && post.getDueDate().isBefore(LocalDateTime.now().plusDays(3));

        return ParticipatedListResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .postStatus(post.getPostStatus())
                .location(post.getLocation())
                .imageKey(img)
                .orderPrice(post.getUnitPrice()*o.getQuantity())
                .orderQuantity(o.getQuantity())
                .orderStatus(o.getStatus())
                .soldAmount(post.getTotalAmount() - post.getLeftAmount())
                .totalAmount(post.getTotalAmount())
                .participantCount(post.getParticipantCount())
                .isWish(isWish)
                .dueSoon(dueSoon)
                .build();
    }

    public ParticipantResponse toParticipantResponse(Order order) {
        User user = order.getUser();

        return ParticipantResponse.builder()
                .participantId(user.getId())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .imageKey(user.getImageKey())
                .orderName(order.getName())
                .orderQuantity(order.getQuantity())
                .orderStatus(order.getStatus())
                .build();
    }

}
