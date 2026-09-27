package com.tastyhouse.application.order.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.order.port.out.write.OrderDeliveryDestinationSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderState;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStateMapperTest {

    @Test
    @DisplayName("Order → OrderState → Order 왕복 시 금액·배달지·예약 필드가 뒤바뀌지 않고 보존된다")
    void orderRoundTrip() {
        Order original = order(
            new OrderDeliveryDestination(
                31L,
                "101동 202호",
                1234,
                new BigDecimal("37.123456"),
                new BigDecimal("127.654321"),
                "서울시 강남구 역삼동 1-2",
                "서울시 강남구 테헤란로 3"
            ),
            OrderSchedule.of(LocalDateTime.of(2026, 5, 1, 12, 0), LocalDateTime.of(2026, 5, 1, 12, 30))
        );

        Order restored = OrderStateMapper.toDomain(OrderStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("배달지 7필드는 이름이 같은 Snapshot 컴포넌트로 옮겨진다")
    void deliveryDestinationFieldsMapByName() {
        Order original = order(
            new OrderDeliveryDestination(
                31L,
                "101동 202호",
                1234,
                new BigDecimal("37.123456"),
                new BigDecimal("127.654321"),
                "서울시 강남구 역삼동 1-2",
                "서울시 강남구 테헤란로 3"
            ),
            OrderSchedule.none()
        );

        OrderState state = OrderStateMapper.toState(original);
        OrderDeliveryDestinationSnapshot snapshot = state.deliveryDestination();

        assertThat(snapshot.adminDongId()).isEqualTo(31L);
        assertThat(snapshot.detailAddress()).isEqualTo("101동 202호");
        assertThat(snapshot.distanceMeters()).isEqualTo(1234);
        assertThat(snapshot.latitude()).isEqualTo(new BigDecimal("37.123456"));
        assertThat(snapshot.longitude()).isEqualTo(new BigDecimal("127.654321"));
        assertThat(snapshot.lotAddress()).isEqualTo("서울시 강남구 역삼동 1-2");
        assertThat(snapshot.roadAddress()).isEqualTo("서울시 강남구 테헤란로 3");
        assertThat(state.totalProductAmount()).isEqualTo(20000);
        assertThat(state.deliveryTipAmount()).isEqualTo(3000);
        assertThat(state.finalAmount()).isEqualTo(19500);
    }

    @Test
    @DisplayName("배달지·예약이 null이면 왕복 후에도 null이다")
    void nullEmbeddedValuesStayNull() {
        Order original = order(null, null);

        Order restored = OrderStateMapper.toDomain(OrderStateMapper.toState(original));

        assertThat(restored.getDeliveryDestination()).isNull();
        assertThat(restored.getSchedule()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static Order order(OrderDeliveryDestination deliveryDestination, OrderSchedule schedule) {
        return Order.reconstitute(
            11L,
            MemberId.of(12L),
            ShopId.of(13L),
            "ORD20260501000001",
            OrderMethod.DELIVERY,
            OrderStatus.PREPARING,
            "홍길동",
            "01012345678",
            "hong@example.com",
            20000,
            1000,
            1500,
            2000,
            4500,
            3000,
            1000,
            19500,
            deliveryDestination,
            schedule,
            MemberCouponId.of(14L),
            2000,
            195,
            false,
            LocalDateTime.of(2026, 5, 1, 11, 0),
            LocalDateTime.of(2026, 5, 1, 11, 5)
        );
    }
}
