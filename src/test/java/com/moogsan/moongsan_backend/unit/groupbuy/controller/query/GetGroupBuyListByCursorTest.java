package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyListController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.BasicList.BasicListResponse;
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
import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_LIST_SUCCESS;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GroupBuyListController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyListByCursorTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("전체 공구 리스트 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getGroupBuyListByCursor_authenticatedWithParams() throws Exception {
        // Given
        BasicListResponse item = BasicListResponse.builder()
                .postId(100L)
                .title("내가 주최한 공구")
                .name("진라면")
                .postStatus("OPEN")
                .imageKeys(List.of())
                .unitPrice(5000)
                .unitAmount(1)
                .soldAmount(2)
                .totalAmount(100)
                .participantCount(5)
                .dueSoon(true)
                .isWish(true)
                .createdAt(LocalDateTime.of(2025,5,20,12,0))
                .build();

        PagedResponse<BasicListResponse> page = PagedResponse.<BasicListResponse>builder()
                .count(1L)
                .posts(Collections.singletonList(item))
                .nextCursor(101)
                .nextCursorPrice(null)
                .nextCreatedAt(LocalDateTime.of(2025,5,20,12,0))
                .hasMore(false)
                .build();

        Mockito.when(queryFacade.getGroupBuyListByCursor(
                eq(1L),                              // userId
                eq(123L),                            // categoryId (ex: 123L로 임의 지정)
                eq("created"),                       // orderBy
                eq(50L),                             // cursorId
                eq(LocalDateTime.parse("2025-05-26T12:00:00")), // cursorCreatedAt
                eq(1000),                            // cursorPrice
                eq(5),                               // limit
                eq(false),                           // openOnly
                eq("참치")                           // keyword
        )).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/group-buys")
                        .param("category", "123") // categoryId
                        .param("sort", "created")
                        .param("cursorId", "50")
                        .param("cursorCreatedAt", "2025-05-26T12:00:00") // 날짜 일치시켜야 함
                        .param("cursorPrice", "1000")
                        .param("limit", "5")
                        .param("openOnly", "false")
                        .param("keyword", "참치"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(GET_LIST_SUCCESS));

        Mockito.verify(queryFacade).getGroupBuyListByCursor(
                eq(1L),                                         // userId
                eq(123L),                                       // categoryId
                eq("created"),                                  // orderBy
                eq(50L),                                        // cursorId
                eq(LocalDateTime.parse("2025-05-26T12:00:00")), // cursorCreatedAt
                eq(1000),                                       // cursorPrice
                eq(5),                                          // limit
                eq(false),                                      // openOnly
                eq("참치")                                      // keyword
        );

    }

    @Test
    @DisplayName("전체 공구 리스트 조회 성공 시 200 반환 - 비인증 사용자 접근 가능")
    void getGroupBuyListByCursor_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys")
                        .param("sort", "created")
                        .param("cursorCreatedAt", "2025-05-20T12:00:00")
                        .param("cursorId", "50")
                        .param("limit", "5")
                )
                .andExpect(status().isOk());
    }

}
