package com.tastyhouse.domain.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 주문 상태전이 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트를 손으로 만든 스텁으로 대체해 검증한다.
 * 특히 <b>상태 전이 후 명시적 save 호출</b>(순수 POJO 도메인 모델은 더티 체킹이 없다)을 확인한다 —
 * 이 저장이 빠지면 결제 승인·취소가 주문에 반영되지 않고 조용히 유실된다.
 */
class OrderTransitionServiceTest {

    private static final Long ORDER_ID = 42L;
    private static final Long MEMBER_ID = 7L;
    private static final Long OTHER_MEMBER_ID = 8L;

    @Test
    @DisplayName("주문을 PK로 로드한다")
    void load_returnsOrder() {
        Fixture fixture = new Fixture();

        Order loaded = fixture.service.load(OrderId.of(ORDER_ID));

        assertThat(loaded.getOrderId()).isEqualTo(OrderId.of(ORDER_ID));
    }

    @Test
    @DisplayName("없는 주문을 로드하면 ORDER_NOT_FOUND")
    void load_notFound() {
        Fixture fixture = new Fixture();
        fixture.orderRepository.stored = null;

        assertThatThrownBy(() -> fixture.service.load(OrderId.of(ORDER_ID)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("소유자가 아니면 ORDER_ACCESS_DENIED")
    void loadOwnedBy_deniesOtherMember() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> fixture.service.loadOwnedBy(OrderId.of(ORDER_ID), MemberId.of(OTHER_MEMBER_ID)))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("소유자면 주문을 돌려준다")
    void loadOwnedBy_allowsOwner() {
        Fixture fixture = new Fixture();

        Order loaded = fixture.service.loadOwnedBy(OrderId.of(ORDER_ID), MemberId.of(MEMBER_ID));

        assertThat(loaded.getOrderId()).isEqualTo(OrderId.of(ORDER_ID));
    }

    @Test
    @DisplayName("상태를 전이하고 명시적으로 저장한다")
    void changeStatus_transitionsAndSaves() {
        Fixture fixture = new Fixture();

        fixture.service.changeStatus(OrderId.of(ORDER_ID), OrderStatus.CONFIRMED);

        assertThat(fixture.orderRepository.saved).hasSize(1);
        assertThat(fixture.orderRepository.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("이미 로드된 주문의 상태 전이도 저장한다(결제 경로용 오버로드)")
    void changeStatus_withLoadedOrder_saves() {
        Fixture fixture = new Fixture();
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        fixture.service.changeStatus(order, OrderStatus.CANCELLED);

        assertThat(fixture.orderRepository.saved).hasSize(1);
        assertThat(fixture.orderRepository.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("전이 테이블이 막는 상태 변경은 저장 없이 BusinessException으로 실패한다")
    void changeStatus_invalidTransition_throwsAndDoesNotSave() {
        Fixture fixture = new Fixture();

        // PENDING -> COMPLETED 는 단계 건너뛰기라 허용하지 않는다
        assertThatThrownBy(() -> fixture.service.changeStatus(OrderId.of(ORDER_ID), OrderStatus.COMPLETED))
            .isInstanceOf(BusinessException.class);

        assertThat(fixture.orderRepository.saved).isEmpty();
    }

    @Test
    @DisplayName("이미 취소된 주문의 결제 확정은 저장 없이 ORDER_ALREADY_CANCELLED로 실패한다")
    void confirm_onCancelledOrder_throwsAndDoesNotSave() {
        Fixture fixture = new Fixture();
        fixture.orderRepository.stored = Fixture.orderWithStatus(OrderStatus.CANCELLED);
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        assertThatThrownBy(() -> fixture.service.confirm(order))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_CANCELLED);

        assertThat(fixture.orderRepository.saved).isEmpty();
    }

    @Test
    @DisplayName("조리 시작된 주문의 결제 취소는 저장 없이 ORDER_ALREADY_PREPARING으로 실패한다")
    void cancel_onPreparingOrder_throwsAndDoesNotSave() {
        Fixture fixture = new Fixture();
        fixture.orderRepository.stored = Fixture.orderWithStatus(OrderStatus.PREPARING);
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        assertThatThrownBy(() -> fixture.service.cancel(order))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_PREPARING);

        assertThat(fixture.orderRepository.saved).isEmpty();
    }

    @Test
    @DisplayName("결제 승인 확정은 CONFIRMED로 전이하고 저장한다")
    void confirm_transitionsAndSaves() {
        Fixture fixture = new Fixture();
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        fixture.service.confirm(order);

        assertThat(fixture.orderRepository.saved).hasSize(1);
        assertThat(fixture.orderRepository.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("결제 취소는 CANCELLED로 전이하고 저장한다")
    void cancel_transitionsAndSaves() {
        Fixture fixture = new Fixture();
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        fixture.service.cancel(order);

        assertThat(fixture.orderRepository.saved).hasSize(1);
        assertThat(fixture.orderRepository.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("삭제는 soft delete 후 저장한다")
    void delete_softDeletesAndSaves() {
        Fixture fixture = new Fixture();

        fixture.service.delete(OrderId.of(ORDER_ID));

        assertThat(fixture.orderRepository.saved).hasSize(1);
        assertThat(fixture.orderRepository.saved.getFirst().isDeleted()).isTrue();
    }

    private static final class Fixture {

        private final StubOrderRepository orderRepository = new StubOrderRepository();
        private final OrderTransitionService service = new OrderTransitionService(orderRepository);

        private Fixture() {
            orderRepository.stored = orderWithStatus(OrderStatus.PENDING);
        }

        private static Order orderWithStatus(OrderStatus status) {
            return Order.reconstitute(
                ORDER_ID,
                MemberId.of(MEMBER_ID),
                ShopId.of(1L),
                "ORD-20260731000000-ABCDEF123456",
                OrderMethod.DELIVERY,
                status,
                "홍길동",
                "01012345678",
                "hong@example.com",
                20000, 0, 0, 0, 0, 0, 20000, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0, 0,
                false,
                null,
                null
            );
        }
    }

    private static final class StubOrderRepository implements OrderRepository {

        private Order stored;
        private final List<Order> saved = new ArrayList<>();

        @Override
        public Optional<Order> findById(OrderId orderId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public Order save(Order order) {
            saved.add(order);
            return order;
        }
    }
}
