package com.moogsan.moongsan_backend.unit.groupbuy.controller.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moogsan.moongsan_backend.domain.groupbuy.controller.GroupBuyCommandController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.CreateGroupBuyRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GroupBuyCommandController.class)
@Import(InMemoryDuplicateRequestPreventer.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class CreateGroupBuyTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
    @MockBean
    private GroupBuyCommandFacade groupBuyCommandFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("공구 게시글 성공 시 201 반환")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuySuccess() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("라면 공구")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("공구 게시글이 성공적으로 업로드되었습니다."))
                .andExpect(jsonPath("$.data.postId").value(42L));
    }


    /// title

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - title 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_title() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.title").value("제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - title 공백")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_blank_title() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("   ")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.title").value("제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - title 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_title_too_short() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.title").value("제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - title 최대 값 초과")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_title_too_long() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("초특가 한정 수량 프리미엄 친환경 무농약 유기농 수제 천연 발효 장아찌 세트 " +
                        "지금 주문하면 무료 배송과 함께 사은품까지 증정되는 놀라운 기회를 놓치지 마세요 " +
                        "지금 바로 참여하여 건강한 한 끼의 행복을 경험하세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.title").value("제목은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }


    /// name

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - name 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_name() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.name").value("상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - name 공백")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_blank_name() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("   ")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.name").value("상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - name 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_name_too_short() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.name").value("상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - name 최대 값 초과")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_name_too_long() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면 얼큰하고 깊은 맛에 해물과 한우 사골을 더해 더욱 진하고 풍부해진 국물, " +
                        "정통 수타식 면발로 즐기는 프리미엄 대용량 가정용 패밀리팩 5+1 이벤트 한정판, " +
                        "캠핑·홈파티 필수 아이템")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.name").value("상품명은 공백을 제외한 1자 이상, 100자 이하로 입력해주세요."));
    }


    ///  url

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - url 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_url_too_short() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.url").value("URL은 1자 이상, 2000자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - url 최대 값 초과")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_url_too_long() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://www.example.com/product/jinramen?q=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\n")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.url").value("URL은 1자 이상, 2000자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - 잘못된 url 형식")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_invalid_url() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("hi")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.url").value("URL 형식이 올바르지 않습니다."));
    }


    ///  price

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - price 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_price() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.price").value("상품 가격은 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - price 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_price_too_small() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(0)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("image1.jpg"))
                .build();

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.price").value("상품 가격은 1 이상이어야 합니다."));
    }


    /// totalAmount
}