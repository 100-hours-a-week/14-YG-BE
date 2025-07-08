package com.moogsan.moongsan_backend.unit.groupbuy.controller.query;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.query.GroupBuyParticipantsController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantListResponse;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.query.response.groupBuyList.ParticipantList.ParticipantResponse;
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

import java.util.List;

import static com.moogsan.moongsan_backend.domain.groupbuy.message.ResponseMessage.GET_PARTICIPANTS_SUCCESS;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupBuyParticipantsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetGroupBuyParticipantsInfoTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyQueryFacade queryFacade;

    @Test
    @DisplayName("참여자 리스트 조회 성공 시 200 반환 - 인증 사용자, 파라미터 포함")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void getGroupBuyParticipantsInfo_authenticatedWithParams() throws Exception {
        // Given
        ParticipantResponse participant = ParticipantResponse.builder()
                .participantId(1L)
                .nickname("뭉산")
                .orderName("박지은")
                .phoneNumber("01094598198")
                .imageKey("images/profile")
                .orderQuantity(1)
                .orderStatus("CONFIRMED")
                .build();

        ParticipantListResponse item = ParticipantListResponse.builder()
                .participants(List.of(participant))
                .build();

        Mockito.when(queryFacade.getGroupBuyParticipantsInfo(
                eq(1L),
                eq(20L)
        )).thenReturn(item);

        // When & Then
        mockMvc.perform(get("/api/group-buys/{postId}/participants", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(GET_PARTICIPANTS_SUCCESS));

        Mockito.verify(queryFacade).getGroupBuyParticipantsInfo(
                eq(1L), eq(20L)
        );
    }

    @Test
    @DisplayName("공구 참여자 리스트 조회 실패 시 401 반환 - 비인증 사용자 접근 불가")
    void getGroupBuyParticipantsInfo_unauthorized() throws Exception {
        mockMvc.perform(get("/api/group-buys/{postId}/participants", 20L))
                .andExpect(status().isUnauthorized());
    }

}
