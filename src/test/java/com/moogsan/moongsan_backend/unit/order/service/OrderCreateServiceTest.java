package com.moogsan.moongsan_backend.unit.order.service;

import com.moogsan.moongsan_backend.domain.chatting.Facade.command.ChattingCommandFacade;
import com.moogsan.moongsan_backend.domain.groupbuy.entity.GroupBuy;
import com.moogsan.moongsan_backend.domain.groupbuy.policy.DueSoonPolicy;
import com.moogsan.moongsan_backend.domain.groupbuy.repository.GroupBuyRepository;
import com.moogsan.moongsan_backend.domain.order.dto.request.OrderCreateRequest;
import com.moogsan.moongsan_backend.domain.order.dto.response.OrderCreateResponse;
import com.moogsan.moongsan_backend.domain.order.entity.Order;
import com.moogsan.moongsan_backend.domain.order.repository.OrderRepository;
import com.moogsan.moongsan_backend.domain.order.service.OrderCreateService;
import com.moogsan.moongsan_backend.domain.user.entity.User;
import com.moogsan.moongsan_backend.domain.user.repository.UserRepository;
import com.moogsan.moongsan_backend.global.exception.base.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled
class OrderCreateServiceTest {

    @InjectMocks
    private OrderCreateService orderCreateService;

    @Mock private UserRepository userRepository;
    @Mock private GroupBuyRepository groupBuyRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private DueSoonPolicy dueSoonPolicy;
    @Mock private ChattingCommandFacade chattingCommandFacade;

    private final Long userId = 1L;
    private final Long postId = 10L;
    private User user;
    private GroupBuy groupBuy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(userId)
                .name("홍길동")
                .accountBank("카카오뱅크")
                .accountNumber("12345678901234")
                .build();

        groupBuy = GroupBuy.builder()
                .id(postId)
                .name("고추장")
                .unitAmount(5)
                .leftAmount(20)
                .postStatus("OPEN")
                .user(user)
                .build();
    }

    @Test
    @DisplayName("정상 주문 생성")
    void createOrder_success() {
        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 10, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, postId, "CANCELED"))
                .thenReturn(Optional.empty());

        OrderCreateResponse response = orderCreateService.createOrder(request, userId);

        assertThat(response.getProductName()).isEqualTo("고추장");
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getPrice()).isEqualTo(1000);
        assertThat(response.getHostName()).isEqualTo("홍길동");
        assertThat(response.getHostAccountBank()).isEqualTo("카카오뱅크");
    }

    @Test
    @DisplayName("공구 상태가 OPEN이 아니면 예외")
    void createOrder_closedGroupBuy() {
        groupBuy.changePostStatus("CLOSED");

        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 10, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.of(groupBuy));

        assertThatThrownBy(() -> orderCreateService.createOrder(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("현재 주문이 불가능한 상태입니다.");
    }

    @Test
    @DisplayName("이미 참여한 주문이 존재할 경우 예외")
    void createOrder_duplicate() {
        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 10, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, postId, "CANCELED"))
                .thenReturn(Optional.of(mock(Order.class)));

        assertThatThrownBy(() -> orderCreateService.createOrder(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 공동구매에 참여하였습니다.");
    }

    @Test
    @DisplayName("수량이 단위의 배수가 아닐 경우 예외")
    void createOrder_invalidUnitAmount() {
        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 7, null); // 7은 5의 배수가 아님

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, postId, "CANCELED"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCreateService.createOrder(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("수량은 주문 단위의 배수여야 합니다.");
    }

    @Test
    @DisplayName("남은 수량 초과 시 예외")
    void createOrder_exceedLeftAmount() {
        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 25, null); // 남은 수량은 20

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.of(groupBuy));
        when(orderRepository.findByUserIdAndGroupBuyIdAndStatusNot(userId, postId, "CANCELED"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCreateService.createOrder(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("남은 수량을 초과하여 주문할 수 없습니다.");
    }

    @Test
    @DisplayName("유저가 없을 경우 예외")
    void createOrder_userNotFound() {
        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 10, null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCreateService.createOrder(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("유저 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공구가 없을 경우 예외")
    void createOrder_groupBuyNotFound() {
        OrderCreateRequest request = new OrderCreateRequest(postId, 1000, 10, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(groupBuyRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCreateService.createOrder(request, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("공동구매 정보를 찾을 수 없습니다.");
    }
}