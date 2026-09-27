package com.tastyhouse.infrastructure.order.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.order.port.out.write.OrderDeliveryDestinationSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderScheduleSnapshot;
import com.tastyhouse.application.order.port.out.write.OrderState;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    @DisplayName("OrderState → 엔티티 → OrderState 왕복 시 배달지 7필드·예약·금액이 뒤바뀌지 않는다")
    void orderStateRoundTrip() {
        OrderState state = state(
            new OrderDeliveryDestinationSnapshot(
                31L,
                "101동 202호",
                1234,
                new BigDecimal("37.123456"),
                new BigDecimal("127.654321"),
                "서울시 강남구 역삼동 1-2",
                "서울시 강남구 테헤란로 3"
            ),
            new OrderScheduleSnapshot(LocalDateTime.of(2026, 5, 1, 12, 0), LocalDateTime.of(2026, 5, 1, 12, 30))
        );

        OrderJpaEntity entity = OrderMapper.toEntity(state);
        OrderDeliveryDestinationEmbeddable embedded = entity.getDeliveryDestination();

        assertThat(embedded.adminDongId()).isEqualTo(31L);
        assertThat(embedded.detailAddress()).isEqualTo("101동 202호");
        assertThat(embedded.distanceMeters()).isEqualTo(1234);
        assertThat(embedded.latitude()).isEqualTo(new BigDecimal("37.123456"));
        assertThat(embedded.longitude()).isEqualTo(new BigDecimal("127.654321"));
        assertThat(embedded.lotAddress()).isEqualTo("서울시 강남구 역삼동 1-2");
        assertThat(embedded.roadAddress()).isEqualTo("서울시 강남구 테헤란로 3");
        assertThat(OrderMapper.toState(entity)).usingRecursiveComparison().isEqualTo(state);
    }

    @Test
    @DisplayName("applyChanges는 상태·금액·배달지·예약을 옮기고 결과가 State와 같다")
    void applyChangesCopiesWritableFields() {
        OrderJpaEntity entity = OrderMapper.toEntity(state(null, null));
        OrderState changed = state(
            new OrderDeliveryDestinationSnapshot(
                41L, "상세", 55, new BigDecimal("35.1"), new BigDecimal("129.2"), "지번", "도로명"),
            new OrderScheduleSnapshot(LocalDateTime.of(2026, 6, 1, 9, 0), LocalDateTime.of(2026, 6, 1, 9, 30))
        );

        OrderMapper.applyChanges(entity, changed);

        assertThat(OrderMapper.toState(entity)).usingRecursiveComparison().isEqualTo(changed);
    }

    @Test
    @DisplayName("배달지·예약이 null이면 엔티티 왕복 후에도 null이다")
    void nullEmbeddedStaysNull() {
        OrderState state = state(null, null);

        OrderState restored = OrderMapper.toState(OrderMapper.toEntity(state));

        assertThat(restored.deliveryDestination()).isNull();
        assertThat(restored.schedule()).isNull();
    }

    private static OrderState state(OrderDeliveryDestinationSnapshot destination, OrderScheduleSnapshot schedule) {
        return new OrderState(
            null,
            12L,
            13L,
            "ORD20260501000001",
            "DELIVERY",
            "PREPARING",
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
            destination,
            schedule,
            14L,
            2000,
            195,
            false,
            null,
            null
        );
    }
}
