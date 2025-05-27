package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.GroupBuyQueryController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.WishList.WishListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.service.GroupBuyQueryService.GetGroupBuyWishList;
import com.moogsan.moongsan_backend.support.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GroupBuyQueryController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyWishListTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("관심 공구 리스트 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getWishList_authenticatedWithParams() throws Exception {
        // Given
        WishListResponse item = WishListResponse.builder()
                .postId(100L)
                .title("관심 공구")
                .postStatus("OPEN")
                .location("카카오테크 교육장")
                .imageKey("thumbnail.jpg")
                .unitPrice(5000)
                .soldAmount(50)
                .totalAmount(100)
                .participantCount(5)
                .dueSoon(true)
                .isWish(false)
                .build();

        PagedResponse<WishListResponse> page = PagedResponse.<WishListResponse>builder()
                .count(1L)
                .posts(Collections.singletonList(item))
                .nextCursor(101)
                .nextCursorPrice(null)
                .nextCreatedAt(LocalDateTime.of(2025,5,20,12,0))
                .hasMore(false)
                .build();

        Mockito.when(queryFacade.getGroupBuyWishList(
                eq(1L),
                eq("created"),
                eq(LocalDateTime.parse("2025-05-20T12:00:00")),
                eq(50L),
                eq(5)
        )).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/group-buys/users/me/wishes")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("관심 공구 리스트를 성공적으로 조회했습니다."));
        //.andExpect(jsonPath("$.data.content[0].postId").value(100));

        Mockito.verify(queryFacade).getGroupBuyWishList(
                eq(1L), eq("created"), eq(LocalDateTime.parse("2025-05-20T12:00:00")), eq(50L), eq(5)
        );
    }

    @Test
    @DisplayName("관심 공구 리스트 조회 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getWishList_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys/users/me/wishes")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isUnauthorized());
    }
}
