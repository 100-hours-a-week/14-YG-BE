package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyParticipatedListController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.PagedResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipatedList.ParticipatedListResponse;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GroupBuyParticipatedListController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyParticipatedListTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("참여 공구 리스트 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getParticipatedList_authenticatedWithParams() throws Exception {
        // Given
        ParticipatedListResponse item = ParticipatedListResponse.builder()
                .postId(100L)
                .title("내가 참여한 공구")
                .postStatus("OPEN")
                .location("카카오테크 교육장")
                .imageKey("thumbnail.jpg")
                .orderPrice(5000)
                .orderQuantity(3)
                .soldAmount(50)
                .totalAmount(100)
                .participantCount(5)
                .orderStatus("CONFIRMED")
                .dueSoon(true)
                .isWish(false)
                .build();

        PagedResponse<ParticipatedListResponse> page = PagedResponse.<ParticipatedListResponse>builder()
                .count(1L)
                .posts(Collections.singletonList(item))
                .nextCursor(101)
                .nextCursorPrice(null)
                .nextCreatedAt(LocalDateTime.of(2025,5,20,12,0))
                .hasMore(false)
                .build();

        Mockito.when(queryFacade.getGroupBuyParticipatedList(
                eq(1L),
                eq("created"),
                eq(LocalDateTime.parse("2025-05-20T12:00:00")),
                eq(50L),
                eq(5)
        )).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/group-buys/users/me/participants")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("참여 공구 리스트를 성공적으로 조회했습니다."));
        //.andExpect(jsonPath("$.data.content[0].postId").value(100));

        Mockito.verify(queryFacade).getGroupBuyParticipatedList(
                eq(1L), eq("created"), eq(LocalDateTime.parse("2025-05-20T12:00:00")), eq(50L), eq(5)
        );
    }

    @Test
    @DisplayName("참여 공구 리스트 조회 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getParticipatedList_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys/users/me/participants")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isUnauthorized());
    }
}