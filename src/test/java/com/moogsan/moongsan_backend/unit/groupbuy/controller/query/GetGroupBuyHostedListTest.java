package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyHostedListController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.HostedList.HostedListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.query.GroupBuyQueryFacade;
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

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_HOSTED_SUCCESS;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GroupBuyHostedListController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class GetGroupBuyHostedListTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("주최 공구 리스트 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getHostedList_authenticatedWithParams() throws Exception {
        // Given
        HostedListResponse item = HostedListResponse.builder()
                .postId(100L)
                .title("내가 주최한 공구")
                .postStatus("OPEN")
                .location("카카오테크 교육장")
                .imageKey("thumbnail.jpg")
                .unitPrice(5000)
                .hostQuantity(2)
                .soldAmount(50)
                .totalAmount(100)
                .participantCount(5)
                .dueSoon(true)
                .isWish(false)
                .build();

        PagedResponse<HostedListResponse> page = PagedResponse.<HostedListResponse>builder()
                .count(1L)
                .posts(Collections.singletonList(item))
                .nextCursor(101)
                .nextCursorPrice(null)
                .nextCreatedAt(LocalDateTime.of(2025,5,20,12,0))
                .hasMore(false)
                .build();

        Mockito.when(queryFacade.getGroupBuyHostedList(
                eq(1L),
                eq("created"),
                eq(50L),
                eq(5)
        )).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/group-buys/users/me/hosts")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(GET_HOSTED_SUCCESS));
                //.andExpect(jsonPath("$.data.content[0].postId").value(100));

        Mockito.verify(queryFacade).getGroupBuyHostedList(
                eq(1L), eq("created"), eq(50L), eq(5)
        );
    }

    @Test
    @DisplayName("주최 공구 리스트 조회 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getHostedList_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys/users/me/hosts")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isUnauthorized());
    }
}

