package com.moogsan.moongsan_backend.unit.groupbuy.controller.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.controller.command.EndGroupBuyController;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.support.fake.InMemoryDuplicateRequestPreventer;
import com.moogsan.moongsan_backend.support.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.END_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EndGroupBuyController.class)
@Import(InMemoryDuplicateRequestPreventer.class)
@AutoConfigureMockMvc(addFilters = false)
public class EndGroupBuyTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyCommandFacade groupBuyCommandFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("공구 게시글 종료 성공 시 200 반환")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void endGroupBuy_Success() throws Exception {

        // 바디 없는 PATCH 요청이므로 생략

        // void 메서드이므로 별도 stubbing 없이 verify로 호출 여부만 검증

        // ====== 요청 & 검증 ======
        mockMvc.perform(patch("/api/group-buys/{postId}/end", 20L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(END_SUCCESS));

        Mockito.verify(groupBuyCommandFacade)
                .endGroupBuy(any(), eq(20L));

    }

    @Test
    @DisplayName("공구 게시글 종료 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void endGroupBuy_unauthorized() throws Exception {
        mockMvc.perform(patch("/api/group-buys/{postId}/end", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
