package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyDetailController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.ImageResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.DetailResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserProfileResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.support.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupBuyDetailController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyDetailInfoTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @DisplayName("공구 게시글 상세 조회 성공 시 200 반환 - 인증 사용자")
    @Test
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getGroupBuyDetail_authenticated() throws Exception {
        // Given
        DetailResponse detail = DetailResponse.builder()
                .postId(100L)
                .title("진라면 공구")
                .name("진라면")
                .postStatus("OPEN")
                .description("맵고 맛있는 진라면입니다.")
                .url("https://example.com/jinramen")
                .imageKeys(List.of(
                        ImageResponse.builder().imageKey("images/thumbnail.jpg").build()
                ))
                .location("카카오테크 교육장")
                .unitPrice(5000)
                .unitAmount(1)
                .soldAmount(10)
                .totalAmount(100)
                .leftAmount(90)
                .participantCount(5)
                .dueSoon(false)
                .isWish(true)
                .isParticipant(true)
                .createdAt(LocalDateTime.of(2025, 5, 20, 12, 0))
                .dueDate(LocalDateTime.of(2025, 5, 27, 12, 0))
                .pickupDate(LocalDateTime.of(2025, 5, 28, 18, 0))
                .userProfileResponse(UserProfileResponse.builder()
                        .userId(1L)
                        .nickname("주최자")
                        .profileImageUrl("https://example.com/profile.jpg")
                        .build())
                .build();


        Mockito.when(queryFacade.getGroupBuyDetailInfo(eq(1L), eq(100L)))
                .thenReturn(detail);

        // When & Then
        mockMvc.perform(get("/api/group-buys/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공구 게시글 상세 정보를 성공적으로 조회했습니다."));

        Mockito.verify(queryFacade).getGroupBuyDetailInfo(eq(1L), eq(100L));
    }

    @DisplayName("공구 게시글 상세 조회 성공 시 200 반환 - 비인증 사용자")
    @Test
    void getGroupBuyDetail_unauthenticated() throws Exception {
        DetailResponse detail = DetailResponse.builder()
                .postId(100L)
                .title("진라면 공구")
                .name("진라면")
                .postStatus("OPEN")
                .description("맵고 맛있는 진라면입니다.")
                .url("https://example.com/jinramen")
                .imageKeys(List.of(
                        ImageResponse.builder().imageKey("images/thumbnail.jpg").build()
                ))
                .location("카카오테크 교육장")
                .unitPrice(5000)
                .unitAmount(1)
                .soldAmount(10)
                .totalAmount(100)
                .leftAmount(90)
                .participantCount(5)
                .dueSoon(false)
                .isWish(true)
                .isParticipant(true)
                .createdAt(LocalDateTime.of(2025, 5, 20, 12, 0))
                .dueDate(LocalDateTime.of(2025, 5, 27, 12, 0))
                .pickupDate(LocalDateTime.of(2025, 5, 28, 18, 0))
                .userProfileResponse(UserProfileResponse.builder()
                        .userId(1L)
                        .nickname("주최자")
                        .profileImageUrl("https://example.com/profile.jpg")
                        .build())
                .build();


        Mockito.when(queryFacade.getGroupBuyDetailInfo(eq(null), eq(100L)))
                .thenReturn(detail);

        mockMvc.perform(get("/api/group-buys/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공구 게시글 상세 정보를 성공적으로 조회했습니다."));

        Mockito.verify(queryFacade).getGroupBuyDetailInfo(eq(null), eq(100L));
    }
}
