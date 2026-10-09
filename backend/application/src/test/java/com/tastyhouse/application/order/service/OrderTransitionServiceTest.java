package com.tastyhouse.application.order.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.order.port.out.write.OrderLoadPort;
import com.tastyhouse.application.order.port.out.write.OrderSavePort;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        fixture.orderPersistence.stored = null;

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

        assertThat(fixture.orderPersistence.saved).hasSize(1);
        assertThat(fixture.orderPersistence.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("이미 로드된 주문의 상태 전이도 저장한다(결제 경로용 오버로드)")
    void changeStatus_withLoadedOrder_saves() {
        Fixture fixture = new Fixture();
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        fixture.service.changeStatus(order, OrderStatus.CANCELLED);

        assertThat(fixture.orderPersistence.saved).hasSize(1);
        assertThat(fixture.orderPersistence.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("전이 테이블이 막는 상태 변경은 저장 없이 BusinessException으로 실패한다")
    void changeStatus_invalidTransition_throwsAndDoesNotSave() {
        Fixture fixture = new Fixture();

        assertThatThrownBy(() -> fixture.service.changeStatus(OrderId.of(ORDER_ID), OrderStatus.COMPLETED))
            .isInstanceOf(BusinessException.class);

        assertThat(fixture.orderPersistence.saved).isEmpty();
    }

    @Test
    @DisplayName("이미 취소된 주문의 결제 확정은 저장 없이 ORDER_ALREADY_CANCELLED로 실패한다")
    void confirm_onCancelledOrder_throwsAndDoesNotSave() {
        Fixture fixture = new Fixture();
        fixture.orderPersistence.stored = Fixture.orderWithStatus(OrderStatus.CANCELLED);
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        assertThatThrownBy(() -> fixture.service.confirm(order))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(DomainErrorCode.ORDER_ALREADY_CANCELLED);

        assertThat(fixture.orderPersistence.saved).isEmpty();
    }

    @Test
    @DisplayName("조리 시작된 주문의 결제 취소는 저장 없이 ORDER_ALREADY_PREPARING으로 실패한다")
    void cancel_onPreparingOrder_throwsAndDoesNotSave() {
        Fixture fixture = new Fixture();
        fixture.orderPersistence.stored = Fixture.orderWithStatus(OrderStatus.PREPARING);
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        assertThatThrownBy(() -> fixture.service.cancel(order))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(DomainErrorCode.ORDER_ALREADY_PREPARING);

        assertThat(fixture.orderPersistence.saved).isEmpty();
    }

    @Test
    @DisplayName("결제 승인 확정은 CONFIRMED로 전이하고 저장한다")
    void confirm_transitionsAndSaves() {
        Fixture fixture = new Fixture();
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        fixture.service.confirm(order);

        assertThat(fixture.orderPersistence.saved).hasSize(1);
        assertThat(fixture.orderPersistence.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("결제 취소는 CANCELLED로 전이하고 저장한다")
    void cancel_transitionsAndSaves() {
        Fixture fixture = new Fixture();
        Order order = fixture.service.load(OrderId.of(ORDER_ID));

        fixture.service.cancel(order);

        assertThat(fixture.orderPersistence.saved).hasSize(1);
        assertThat(fixture.orderPersistence.saved.getFirst().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("삭제는 soft delete 후 저장한다")
    void delete_softDeletesAndSaves() {
        Fixture fixture = new Fixture();

        fixture.service.delete(OrderId.of(ORDER_ID));

        assertThat(fixture.orderPersistence.saved).hasSize(1);
        assertThat(fixture.orderPersistence.saved.getFirst().isDeleted()).isTrue();
    }

    private static final class Fixture {

        private final StubOrderPersistence orderPersistence = new StubOrderPersistence();
        private final OrderTransitionService service = new OrderTransitionService(orderPersistence, orderPersistence);

        private Fixture() {
            orderPersistence.stored = orderWithStatus(OrderStatus.PENDING);
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
                20000, 0, 0, 0, 0, 0, 0, 20000, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0, 0,
                false,
                null,
                null
            );
        }
    }

    private static final class StubOrderPersistence implements OrderLoadPort, OrderSavePort {

        private Order stored;
        private final List<Order> saved = new ArrayList<>();

        @Override
        public Optional<Order> findByIdIncludingDeleted(OrderId orderId) {
            return Optional.ofNullable(stored);
        }

        @Override
        public Order save(Order order) {
            saved.add(order);
            return order;
        }
    }
}
