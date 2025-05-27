package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyEditController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyUpdate.GroupBuyForUpdateResponse;
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
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupBuyEditController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyEditInfoTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("공구 게시글 수정 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getGroupBuyEditInfo_authenticatedWithParams() throws Exception {
        // Given
        GroupBuyForUpdateResponse item = GroupBuyForUpdateResponse.builder()
                .title("진라면 공구해요")
                .name("진라면")
                .description("얼큰한 진라면 20개 묶음! 편의점보다 저렴해요.")
                .url("https://example.com/jinramen")
                .imageKeys(List.of())
                .dueDate(LocalDateTime.of(2025, 6, 10, 23, 59))
                .location("서울 성동구 성수이로 113")
                .pickupDate(LocalDateTime.of(2025, 6, 13, 14, 0))
                .price(8500)
                .unitAmount(5)
                .totalAmount(100)
                .build();

        Mockito.when(queryFacade.getGroupBuyEditInfo(
                eq(20L)
        )).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/api/group-buys/{postId}/edit", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공구 게시글 수정용 정보를 성공적으로 조회했습니다."));

        Mockito.verify(queryFacade).getGroupBuyEditInfo(eq(20L));
    }

    @Test
    @DisplayName("공구 게시글 수정 조회 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getGroupBuyEditInfo_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys/{postId}/edit", 20L))
                .andExpect(status().isUnauthorized());
    }
}
