package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyHostAccountController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyDetail.UserAccountResponse;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupBuyHostAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyHostAccountInfoTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("주최자 계좌 정보 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getGroupBuyHostAccountInfo_authenticatedWithParams() throws Exception {
        // Given
        UserAccountResponse item = UserAccountResponse.builder()
                .name("박지은")
                .accountBank("신한은행")
                .accountNumber("110500165112")
                .build();

        Mockito.when(queryFacade.getGroupBuyHostAccountInfo(
                eq(1L),
                eq(20L)
        )).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/api/group-buys/{postId}/host/account", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("공구 게시글 주최자 계좌 정보를 성공적으로 조회했습니다."));

        //.andExpect(jsonPath("$.data.content[0].postId").value(100));

        Mockito.verify(queryFacade).getGroupBuyHostAccountInfo(eq(1L), eq(20L));
    }

    @Test
    @DisplayName("주최자 계좌 정보 조회 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getGroupBuyHostAccountInfo_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys/{postId}/host/account", 20L))
                .andExpect(status().isUnauthorized());
    }
}
