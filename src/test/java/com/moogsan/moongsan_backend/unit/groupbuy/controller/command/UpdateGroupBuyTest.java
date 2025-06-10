package com.moogsan.moongsan_backend.unit.groupbuy.controller.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.controller.command.UpdateGroupBuyController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.UpdateGroupBuyRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.support.fake.InMemoryDuplicateRequestPreventer;
import com.moogsan.moongsan_backend.support.security.WithMockCustomUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.UPDATE_SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UpdateGroupBuyController.class)
@Import(InMemoryDuplicateRequestPreventer.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class UpdateGroupBuyTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyCommandFacade groupBuyCommandFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("공구 게시글 수정 성공 시 200 반환")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void updateGroupBuySuccess() throws Exception {
        // ====== 요청 바디 준비 ======
        UpdateGroupBuyRequest request = UpdateGroupBuyRequest.builder()
                .title("라면 공구")
                .name("진라면")
                .hostQuantity(2)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .pickupDate(LocalDateTime.now().plusDays(10))
                .dateModificationReason("배송이 늦네요...")
                .imageKeys(List.of("images/image1.jpg"))
                .build();

        // ====== Facade 스텁 ====== 생략

        // ====== 요청 & 검증 ======
        mockMvc.perform(patch("/api/group-buys/{postId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(UPDATE_SUCCESS));
    }

    @Test
    @DisplayName("공구 게시글 수정 실패 시 400 반환 - reason 명시 없이 pickupDate 변경")
    void updateGroupBuy_change_pickupDate_without_reason() throws Exception {
        // ====== 요청 바디 준비 ======
        UpdateGroupBuyRequest request = UpdateGroupBuyRequest.builder()
                .pickupDate(LocalDateTime.now().plusDays(4))
                .build();

        // ====== Facade 스텁 ====== 생략

        // ====== 요청 & 검증 ======
        mockMvc.perform(patch("/api/group-buys/{postId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.dateModificationReason").value("픽업 일자가 변경된 경우 사유를 작성해야 합니다."));
    }

    @Test
    @DisplayName("공구 게시글 수정 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getGroupBuyEditInfo_unauthorized() throws Exception {
        mockMvc.perform(patch("/api/group-buys/{postId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
