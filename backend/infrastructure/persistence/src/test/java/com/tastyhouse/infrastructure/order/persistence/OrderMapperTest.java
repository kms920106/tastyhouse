package com.tastyhouse.infrastructure.order.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    @Test
    @DisplayName("Order → 엔티티 변환 시 금액·배달지 7필드·예약 컬럼이 뒤바뀌지 않는다")
    void orderToEntity() {
        Order original = order(deliveryDestination(), schedule());

        OrderJpaEntity entity = OrderMapper.toEntity(original);
        OrderDeliveryDestinationEmbeddable embedded = entity.getDeliveryDestination();

        assertThat(entity.getMemberId()).isEqualTo(12L);
        assertThat(entity.getShopId()).isEqualTo(13L);
        assertThat(entity.getOrderNumber()).isEqualTo("ORD20260501000001");
        assertThat(entity.getOrderMethod()).isEqualTo("DELIVERY");
        assertThat(entity.getOrderStatus()).isEqualTo("PREPARING");
        assertThat(entity.getOrdererName()).isEqualTo("홍길동");
        assertThat(entity.getOrdererPhone()).isEqualTo("01012345678");
        assertThat(entity.getOrdererEmail()).isEqualTo("hong@example.com");
        assertThat(entity.getTotalProductAmount()).isEqualTo(20000);
        assertThat(entity.getProductDiscountAmount()).isEqualTo(1000);
        assertThat(entity.getCouponDiscountAmount()).isEqualTo(1500);
        assertThat(entity.getPointDiscountAmount()).isEqualTo(2000);
        assertThat(entity.getTotalDiscountAmount()).isEqualTo(4500);
        assertThat(entity.getDeliveryTipAmount()).isEqualTo(3000);
        assertThat(entity.getCupDepositAmount()).isEqualTo(1000);
        assertThat(entity.getFinalAmount()).isEqualTo(19500);
        assertThat(embedded.adminDongId()).isEqualTo(31L);
        assertThat(embedded.detailAddress()).isEqualTo("101동 202호");
        assertThat(embedded.distanceMeters()).isEqualTo(1234);
        assertThat(embedded.latitude()).isEqualTo(new BigDecimal("37.123456"));
        assertThat(embedded.longitude()).isEqualTo(new BigDecimal("127.654321"));
        assertThat(embedded.lotAddress()).isEqualTo("서울시 강남구 역삼동 1-2");
        assertThat(embedded.roadAddress()).isEqualTo("서울시 강남구 테헤란로 3");
        assertThat(entity.getSchedule().scheduledAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 0));
        assertThat(entity.getSchedule().scheduledSlotEndAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 12, 30));
        assertThat(entity.getMemberCouponId()).isEqualTo(14L);
        assertThat(entity.getUsedPoint()).isEqualTo(2000);
        assertThat(entity.getEarnedPoint()).isEqualTo(195);
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 → Order 변환 시 id·생성·수정 시각과 금액·배달지·예약 필드가 보존된다")
    void orderToDomain() {
        Order original = order(deliveryDestination(), schedule());
        OrderJpaEntity entity = OrderMapper.toEntity(original);
        setAuditFields(entity);

        Order restored = OrderMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("예약이 없는 OrderSchedule은 두 컬럼이 모두 null인 Embeddable로 옮겨진다")
    void emptyScheduleMapsToNullColumns() {
        OrderJpaEntity entity = OrderMapper.toEntity(order(deliveryDestination(), OrderSchedule.none()));

        assertThat(entity.getSchedule().scheduledAt()).isNull();
        assertThat(entity.getSchedule().scheduledSlotEndAt()).isNull();
        assertThat(entity.getTotalProductAmount()).isEqualTo(20000);
        assertThat(entity.getDeliveryTipAmount()).isEqualTo(3000);
        assertThat(entity.getFinalAmount()).isEqualTo(19500);
    }

    @Test
    @DisplayName("applyChanges는 상태·금액·배달지·예약을 옮기고 결과가 도메인과 같다")
    void applyChangesCopiesWritableFields() {
        OrderJpaEntity entity = OrderMapper.toEntity(order(null, null));
        setAuditFields(entity);
        Order changed = order(
            new OrderDeliveryDestination(
                41L, "상세", 55, new BigDecimal("35.1"), new BigDecimal("129.2"), "지번", "도로명"),
            OrderSchedule.of(LocalDateTime.of(2026, 6, 1, 9, 0), LocalDateTime.of(2026, 6, 1, 9, 30))
        );

        OrderMapper.applyChanges(entity, changed);

        assertThat(OrderMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(changed);
    }

    @Test
    @DisplayName("배달지·예약이 null이면 엔티티의 Embeddable도 null이다")
    void nullEmbeddedValuesToEntity() {
        OrderJpaEntity entity = OrderMapper.toEntity(order(null, null));

        assertThat(entity.getDeliveryDestination()).isNull();
        assertThat(entity.getSchedule()).isNull();
    }

    @Test
    @DisplayName("Embeddable이 null인 엔티티는 배달지·예약이 null인 Order로 변환된다")
    void nullEmbeddedValuesToDomain() {
        Order original = order(null, null);
        OrderJpaEntity entity = OrderMapper.toEntity(original);
        setAuditFields(entity);

        Order restored = OrderMapper.toDomain(entity);

        assertThat(restored.getDeliveryDestination()).isNull();
        assertThat(restored.getSchedule()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static void setAuditFields(OrderJpaEntity entity) {
        ReflectionTestUtils.setField(entity, "id", 11L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 5, 1, 11, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 5, 1, 11, 5));
    }

    private static OrderDeliveryDestination deliveryDestination() {
        return new OrderDeliveryDestination(
            31L,
            "101동 202호",
            1234,
            new BigDecimal("37.123456"),
            new BigDecimal("127.654321"),
            "서울시 강남구 역삼동 1-2",
            "서울시 강남구 테헤란로 3"
        );
    }

    private static OrderSchedule schedule() {
        return OrderSchedule.of(LocalDateTime.of(2026, 5, 1, 12, 0), LocalDateTime.of(2026, 5, 1, 12, 30));
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
