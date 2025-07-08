package com.moogsan.moongsan_backend.domain.groupbuy.entity;

import com.moogsan.moongsan_backend.domain.BaseEntity;
import com.moogsan.moongsan_backend.domain.chatting.participant.entity.ChatRoom;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.exception.specific.GroupBuyInvalidStateException;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.image.entity.Image;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="group_buy")
public class GroupBuy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(nullable = false, length = 40)
    private String name;

    private String url;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int unitPrice;

    @Column(nullable = false)
    private int totalAmount;

    @Column(nullable = false)
    private int leftAmount;

    @Column(nullable = false)
    private int unitAmount;

    @Column(nullable = false)
    private int hostQuantity;

    @Column(nullable = false, length = 500)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean dueSoon = false;

    @org.hibernate.annotations.Formula("(total_amount -  left_amount) * 100 / total_amount ")
    private int soldRatio;

    @Column(length = 20)
    private String badge;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime pickupDate;

    @Builder.Default
    @Column(nullable = false)
    private int wishCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int participantCount = 0;

    @Builder.Default
    @Column(nullable = false, length = 10)
    private String postStatus = "OPEN";

    private String dateModificationReason;

    @Builder.Default
    private boolean isFinalized = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupBuyCategory> groupBuyCategories = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "groupBuy",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    @OrderBy("imageSeqNo ASC")
    private List<Image> images = new ArrayList<>();

    @OneToOne(mappedBy = "groupBuy")
    private ChatRoom participantChatRoom;

    public void setParticipantChatRoom(ChatRoom room) {
        if (room == null) return;
        this.participantChatRoom = room;
    }

    @Transient
    public boolean isAlmostSoldOut() {
        return soldRatio >= 80;
    }

    public void increaseLeftAmount(int quantity) {
        this.leftAmount += quantity;
    }

    public void decreaseLeftAmount(int quantity) {
        this.leftAmount = Math.max(0, this.leftAmount - quantity);
        if (this.leftAmount == 0) {
            this.postStatus = "CLOSED";
        }
    }

    public void increaseParticipantCount() {
        this.participantCount++;
    }

    public void decreaseParticipantCount() {
        this.participantCount = Math.max(0, this.participantCount - 1);
    }

    public void changePostStatus(String status) {
        String normalized = status.trim().toUpperCase();
        switch (normalized) {
            case "CLOSED":
                this.postStatus = "CLOSED";
                break;
            case "ENDED":
                this.postStatus = "ENDED";
                break;
            case "DELETED":
                this.postStatus = "DELETED";
                break;
            default:
                throw new GroupBuyInvalidStateException("공구 진행 상태는 CLOSED, ENDED, DELETED로만 전환할 수 있습니다.");
        }
    }

    public void updateDueSoonStatus(DueSoonPolicy policy) {
        this.dueSoon = policy.isDueSoon(this);
    }

    // ===========================================
    // 업데이트용 도메인 메서드
    // ===========================================
    public GroupBuy updateForm(UpdateGroupBuyRequest req) {
        if (req.getTitle() != null) {
            this.title = req.getTitle();
        }
        if (req.getName() != null) {
            this.name = req.getName();
        }
        if (req.getHostQuantity() != null) {
            int originalHostQuantity = this.hostQuantity;
            int newHostQuantity = req.getHostQuantity();

            if (originalHostQuantity < newHostQuantity) {
                decreaseLeftAmount(newHostQuantity - originalHostQuantity);
            } else if (originalHostQuantity > newHostQuantity){
                increaseLeftAmount(originalHostQuantity - newHostQuantity);
            }
            this.hostQuantity = newHostQuantity;
        }
        if (req.getDescription() != null) {
            this.description = req.getDescription();
        }
        if (req.getDueDate() != null) {
            this.dueDate = req.getDueDate();
        }
        if (req.getPickupDate() != null) {
            this.pickupDate = req.getPickupDate();
            this.dateModificationReason = req.getDateModificationReason();
        }

        return this;
    }

    public boolean isFixed() {
        return isFinalized;
    }

    public void setFixed(boolean fixed) {
        this.isFinalized = fixed;
    }

}
