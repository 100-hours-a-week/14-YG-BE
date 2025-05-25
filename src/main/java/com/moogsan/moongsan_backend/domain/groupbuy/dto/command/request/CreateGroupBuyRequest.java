package com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupBuyRequest {

    @NotNull(message = "제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.")
    @NotBlank(message = "제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.")
    @Size(min = 1, max = 100, message = "제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.")
    private String title;

    @NotNull(message = "상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.")
    @NotBlank(message = "상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.")
    @Size(min = 1, max = 100, message = "상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요.")
    private String name;

    @Size(min = 1, max = 2000, message = "URL은 1자 이상, 2000자 이하로 입력해주세요.")
    @URL(message = "URL 형식이 올바르지 않습니다.")
    private String url;

    @NotNull(message = "상품 가격은 필수 입력 항목입니다.")
    @Min(value = 1, message = "상품 가격은 1 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "상품 전체 수량은 필수 입력 항목입니다.")
    @Min(value = 1, message = "상품 전체 수량은 1 이상이어야 합니다.")
    private Integer totalAmount;

    @NotNull(message = "상품 주문 단위는 필수 입력 항목입니다.")
    @Min(value = 1, message = "상품 주문 단위는 1 이상이어야 합니다.")
    private Integer unitAmount;

    @NotNull(message = "주최자 주문 수량은 필수 입력 항목입니다.")
    @Min(value = 0, message = "주최자 주문 수량은 0 이상이어야 합니다.")  /// 이후 1로 수정 필요
    private Integer hostQuantity;

    @NotNull(message = "상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요.")
    @NotBlank(message = "상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요.")
    @Size(min = 2, max = 2000, message = "상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요.")
    private String description;

    @NotNull(message = "마감 일자는 필수 입력 항목입니다.")
    @Future(message = "마감일자는 현재 시간 이후여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dueDate;

    @NotNull(message = "거래 장소는 필수 입력 항목입니다.")
    private String location;

    @NotNull(message = "픽업 일자는 필수 입력 항목입니다.")
    @Future(message = "픽업 일자는 현재 시간 이후여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime pickupDate;

    @NotNull(message = "이미지 리스트는 필수입니다.")
    @Size(min=1, max = 5, message = "이미지는 1장 이상, 5장 이하까지 등록할 수 있습니다.")
    private List<
            @NotBlank(message = "이미지 파일은 공백이 될 수 없습니다."
            ) String> imageKeys;
}
