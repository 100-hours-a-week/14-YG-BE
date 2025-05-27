package com.moogsan.moongsan_backend.unit.groupbuy.controller.command;

import com.moogsan.moongsan_backend.domain.groupbuy.controller.GroupBuyCommandController;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.request.DescriptionGenerationRequest;
import com.moogsan.moongsan_backend.domain.groupbuy.dto.command.response.DescriptionDto;
import com.moogsan.moongsan_backend.domain.groupbuy.facade.command.GroupBuyCommandFacade;
import com.moogsan.moongsan_backend.support.fake.InMemoryDuplicateRequestPreventer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(GroupBuyCommandController.class)
@Import({InMemoryDuplicateRequestPreventer.class,
        GenerateDescriptionWebFluxTest.TestSecurityOff.class
})
@AutoConfigureWebTestClient
@ActiveProfiles("test")
// @AutoConfigureMockMvc(addFilters = false)
class GenerateDescriptionWebFluxTest {

    @Autowired
    WebTestClient webTestClient;

    @SuppressWarnings("removal")
    @MockBean
    GroupBuyCommandFacade facade;

    @TestConfiguration
    static class TestSecurityOff {
        @Bean @Order(-101)
        SecurityWebFilterChain noSecurity(ServerHttpSecurity http) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(ex -> ex.anyExchange().permitAll())
                    .build();
        }
    }
    /*

    @Test
    void generateDescriptionSuccess() {
        // given
        var req  = new DescriptionGenerationRequest("https://example.com");
        var fake = new DescriptionDto("제목", "상품", 1000, 1, "요약");
        Mockito.when(facade.generateDescription(any(), any()))
                .thenReturn(Mono.just(fake));

        // when - then
        webTestClient
                .mutateWith(mockUser("tester@example.com").roles("USER")) // 인증 주입
                .mutateWith(csrf())
                .post().uri("/api/group-buys/generation/description")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("상품 상세 설명이 성공적으로 생성되었습니다.")
                .jsonPath("$.data.title").isEqualTo("제목");
    }

     */
}
