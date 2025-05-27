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

import static org.hamcrest.Matchers.hasItem;
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
    @DisplayName("공구 게시글 생성 성공 시 201 반환")
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
                .imageKeys(List.of("images/image1.jpg"))
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
    @DisplayName("공구 게시글 생성 실패 시 400 반환 - title 없음")
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .url("https://www.example.com/product/jinramen?q=" +
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
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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
                .imageKeys(List.of("images/image1.jpg"))
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

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - totalAmount 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_totalAmount() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.totalAmount").value("상품 전체 수량은 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - totalAmount 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_totalAmount_too_small() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(0)
                .unitAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.totalAmount").value("상품 전체 수량은 1 이상이어야 합니다."));
    }


    /// unitAmount

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - unitAmount 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_unitAmount() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(10)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.unitAmount").value("상품 주문 단위는 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - unitAmount 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_unitAmount_too_small() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(10000)
                .unitAmount(0)
                .hostQuantity(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.unitAmount").value("상품 주문 단위는 1 이상이어야 합니다."));
    }


    /// hostQuantity

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - hostQuantity 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_hostQuantity() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(10)
                .unitAmount(1)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.hostQuantity").value("주최자 주문 수량은 필수 입력 항목입니다."));
    }

    /*
    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - hostQuantity 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_hostQuantity_too_small() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(10000)
                .unitAmount(10)
                .hostQuantity(0)
                .description("라면 맛있어요")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.hostQuantity").value("주최자 주문 수량은 0 이상이어야 합니다."));
    }
     */


    /// description

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - description 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_description() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.description").value("상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - description 공백")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_blank_description() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("   ")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.description").value("상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - description 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_description_too_short() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.description").value("상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - description 최대 값 초과")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_description_too_long() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 맛있어요라는 단순한 문장 속에는 깊고 진한 국물 맛과 탱글탱글한 면발의 조화, " +
                        "그리고 누구나 부담 없이 즐길 수 있는 매력적인 얼큰함이 모두 담겨 있습니다. " +
                        "봉지를 뜯는 순간 퍼지는 고추와 마늘, 파향을 머금은 스프의 향긋한 아로마는 바쁜 일상 속에서 잠시나마 따뜻한 위안을 전해 주고, " +
                        "120g의 면과 40g의 스프가 어우러져 완성되는 550ml의 국물 한 그릇은 그 자체로 훌륭한 한 끼 식사가 됩니다. " +
                        "면은 반죽과 숙성 과정을 최적화해 쫀득함이 살아있고, " +
                        "국물은 6가지 향신료와 자연 조미료의 균형 잡힌 배합으로 느껴지는 감칠맛이 진하면서도 깔끔해 마지막 한 방울까지 놓치고 싶지 않게 만듭니다. " +
                        "간편 조리법 또한 진라면의 큰 장점으로, 끓는 물에 면과 스프, 건더기를 넣고 4분만 기다리면 완성되어 시간 대비 최고의 만족을 선사하고, " +
                        "취향에 따라 계란, 대파, 김치, 치즈 등 각종 토핑을 추가해 나만의 레시피로 재해석할 수 있습니다. " +
                        "특히 소비자 설문 조사에서 매년 “가장 맛있는 라면”으로 선정되며 스테디셀러로 자리매김한 브랜드 히스토리는, " +
                        "엄선된 원재료와 철저한 품질 관리, 그리고 오랜 전통과 노하우가 결합되어 매 제품마다 일관된 맛을 유지해 온 결과입니다. " +
                        "포장 단위는 4개입, 10개입, 20개입으로 선택할 수 있어 가정용으로는 물론, 사무실 비치용, 캠핑 등의 야외 활동에도 부담 없이 가져갈 수 있으며, " +
                        "유통기한은 제조일로부터 9개월로 넉넉해 여유 있게 보관 가능합니다. " +
                        "진라면 한 봉지는 칼로리 460kcal로 적당한 에너지를 공급해 주며, 보관은 직사광선을 피해 상온에 두는 것만으로도 충분합니다. " +
                        "온 가족이 둘러앉아 뜨끈한 한 끼를 나눌 때나, 혼자만의 시간에 잔잔한 위로가 필요할 때, 급히 끓여야 하는 야식 타임에도, " +
                        "진라면은 한 치의 고민 없이 꺼내어 “진라면 맛있어요”라고 외치게 만드는 마법 같은 경험을 선사합니다. " +
                        "끝없이 쏟아지는 수많은 라면 신제품 속에서도 꾸준히 사랑받아 온 이유는 명확합니다. 한결같은 맛과 품질, " +
                        "그리고 언제나 든든한 한 그릇으로 우리 곁을 지켜 온 신뢰가 있기 때문입니다. 진라면 맛있어요—이 한마디면 충분합니다." +
                        "진라면 맛있어요라는 단순한 문장 속에는 깊고 진한 국물 맛과 탱글탱글한 면발의 조화, " +
                        "그리고 누구나 부담 없이 즐길 수 있는 매력적인 얼큰함이 모두 담겨 있습니다. " +
                        "봉지를 뜯는 순간 퍼지는 고추와 마늘, 파향을 머금은 스프의 향긋한 아로마는 바쁜 일상 속에서 잠시나마 따뜻한 위안을 전해 주고, " +
                        "120g의 면과 40g의 스프가 어우러져 완성되는 550ml의 국물 한 그릇은 그 자체로 훌륭한 한 끼 식사가 됩니다. " +
                        "면은 반죽과 숙성 과정을 최적화해 쫀득함이 살아있고, " +
                        "국물은 6가지 향신료와 자연 조미료의 균형 잡힌 배합으로 느껴지는 감칠맛이 진하면서도 깔끔해 마지막 한 방울까지 놓치고 싶지 않게 만듭니다. " +
                        "간편 조리법 또한 진라면의 큰 장점으로, 끓는 물에 면과 스프, 건더기를 넣고 4분만 기다리면 완성되어 시간 대비 최고의 만족을 선사하고, " +
                        "취향에 따라 계란, 대파, 김치, 치즈 등 각종 토핑을 추가해 나만의 레시피로 재해석할 수 있습니다. " +
                        "특히 소비자 설문 조사에서 매년 “가장 맛있는 라면”으로 선정되며 스테디셀러로 자리매김한 브랜드 히스토리는, " +
                        "엄선된 원재료와 철저한 품질 관리, 그리고 오랜 전통과 노하우가 결합되어 매 제품마다 일관된 맛을 유지해 온 결과입니다. " +
                        "포장 단위는 4개입, 10개입, 20개입으로 선택할 수 있어 가정용으로는 물론, 사무실 비치용, 캠핑 등의 야외 활동에도 부담 없이 가져갈 수 있으며, " +
                        "유통기한은 제조일로부터 9개월로 넉넉해 여유 있게 보관 가능합니다. " +
                        "진라면 한 봉지는 칼로리 460kcal로 적당한 에너지를 공급해 주며, 보관은 직사광선을 피해 상온에 두는 것만으로도 충분합니다. " +
                        "온 가족이 둘러앉아 뜨끈한 한 끼를 나눌 때나, 혼자만의 시간에 잔잔한 위로가 필요할 때, 급히 끓여야 하는 야식 타임에도, " +
                        "진라면은 한 치의 고민 없이 꺼내어 “진라면 맛있어요”라고 외치게 만드는 마법 같은 경험을 선사합니다. " +
                        "끝없이 쏟아지는 수많은 라면 신제품 속에서도 꾸준히 사랑받아 온 이유는 명확합니다. 한결같은 맛과 품질, " +
                        "그리고 언제나 든든한 한 그릇으로 우리 곁을 지켜 온 신뢰가 있기 때문입니다. 진라면 맛있어요—이 한마디면 충분합니다.")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.description").value("상품 설명은 공백을 제외한 2자 이상, 2000자 이하로 입력해주세요."));
    }


    /// dueDate

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - dueDate 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_dueDate() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.dueDate").value("마감 일자는 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - dueDate 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_dueDate_too_early() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().minusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.dueDate").value("마감 일자는 현재 시간 이후여야 합니다."));
    }

    /*
    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - dueDate 형식 위반")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_invalid_dueDate() throws Exception {

        String dueDate = "2025-05-26 11:03:33";

        String json = "{"
                + "\"title\":\"진라면 공구\","
                + "\"name\":\"진라면\","
                + "\"url\":\"https://example.com\","
                + "\"price\":1000,"
                + "\"totalAmount\":10,"
                + "\"unitAmount\":1,"
                + "\"hostQuantity\":1,"
                + "\"description\":\"유효한 설명입니다.\","
                + "\"dueDate\":\"2025-05-26T11:03:33\","
                + "\"location\":\"테스트 장소\","
                + "\"pickupDate\":\"2025-05-28T10:00\","
                + "\"imageKeys\":[\"images/image1.jpg\"]"
                + "}";

        // ====== Facade 스텁 ======
        Mockito.when(groupBuyCommandFacade.createGroupBuy(any(), any()))
                .thenReturn(42L);

        // ====== 요청 & 검증 ======
        mockMvc.perform(post("/api/group-buys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.data.dueDate").value("마감 일자는 yyyy-MM-dd'T'HH:mm 형식으로 입력해주세요."));
    }

     */


    ///  location

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - location 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_location() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .dueDate(LocalDateTime.now().plusDays(3))
                .description("진라면 사실 분")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.location").value("거래 장소는 공백을 제외한 2자 이상, 85자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - location 공백")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_blank_location() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("   ")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.location").value("거래 장소는 공백을 제외한 2자 이상, 85자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - location 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_location_too_short() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.location").value("거래 장소는 공백을 제외한 2자 이상, 85자 이하로 입력해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - location 최대 값 초과")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_location_too_long() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("어디서 뵐까요? 카카오테크 교육장은 판교 테크노밸리 인근에 위치해 있어 접근성이 뛰어납니다. " +
                        "넓고 쾌적한 강의실은 최신형 컴퓨터와 대형 스크린을 갖추고 있으며, 자유로운 토론을 위한 휴게 라운지와 그룹 프로젝트룸이 마련되어 있습니다. " +
                        "현업 전문가가 실습 중심으로 진행하는 커리큘럼과 소규모 멘토링 세션을 통해 개발 역량을 더욱 높일 수 있는 최적의 학습 공간입니다.")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.location").value("거래 장소는 공백을 제외한 2자 이상, 85자 이하로 입력해주세요."));
    }


    /// pickupDate

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - pickupDate 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_pickupDate() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(4))
                .location("카카오테크 교육장")
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.pickupDate").value("픽업 일자는 필수 입력 항목입니다."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - pickupDate 최소값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_pickupDate_too_early() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().minusDays(4))
                .imageKeys(List.of("images/image1.jpg"))
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
                .andExpect(jsonPath("$.data.pickupDate").value("픽업 일자는 현재 시간 이후여야 합니다."));
    }


    ///  imageKeys

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - imageKeys 없음")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_without_imageKeys() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
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
                .andExpect(jsonPath("$.data.imageKeys").value("이미지는 1장 이상, 5장 이하로 등록해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - imageKeys 최소 값 미만")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_imageKeys_too_small() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of())
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
                .andExpect(jsonPath("$.data.imageKeys").value("이미지는 1장 이상, 5장 이하로 등록해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - imageKeys 최대 값 초과")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_imageKeys_too_large() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of(
                        "images/image1.jpg", "images/image2.jpg",
                        "images/image3.jpg", "images/image4.jpg",
                        "images/image5.jpg", "images/image6.jpg"))
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
                .andExpect(jsonPath("$.data.imageKeys").value("이미지는 1장 이상, 5장 이하로 등록해주세요."));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - imageKeys 공백")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_blank_imageKeys() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
                .dueDate(LocalDateTime.now().plusDays(3))
                .location("카카오테크 교육장")
                .pickupDate(LocalDateTime.now().plusDays(4))
                .imageKeys(List.of("   ", "images/image2.jpg"))
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
                .andExpect(jsonPath("$.data.*", hasItem("이미지는 반드시 images/로 시작해야 합니다")));
    }

    @Test
    @DisplayName("공구 게시글 실패 시 400 반환 - imageKeys 형식 오류")
    @WithMockCustomUser(id = 1L, username = "tester@example.com")
    void createGroupBuyFail_invalid_imageKeys() throws Exception {
        // ====== 요청 바디 준비 ======
        CreateGroupBuyRequest request = CreateGroupBuyRequest.builder()
                .title("진라면 싸게 데려가세요!")
                .name("진라면")
                .url("https://example.com")
                .price(10000)
                .totalAmount(100)
                .unitAmount(10)
                .hostQuantity(1)
                .description("진라면 사실 분")
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
                .andExpect(jsonPath("$.data.*", hasItem("이미지는 반드시 images/로 시작해야 합니다")));
    }
}