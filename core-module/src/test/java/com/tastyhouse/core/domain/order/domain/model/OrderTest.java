package com.tastyhouse.core.domain.order.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.order.domain.vo.OrderId;
import com.tastyhouse.core.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.core.exception.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class OrderTest {

    private static final MemberId MEMBER_ID = MemberId.of(1L);

    private Order newOrder() {
        return Order.of(
            MEMBER_ID,
            10L,
            "ORD-001",
            OrderMethod.TABLE,
            null,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            null, null, null, null, null, null,
            null,
            null,
            null
        );
    }

    @Test
    @DisplayName("of로 생성하면 미영속 상태이고 orderStatus는 PENDING, 금액류는 0으로 기본값이 채워진다")
    void of_createsTransientOrderWithDefaults() {
        Order order = newOrder();

        assertThat(order.getId()).isNull();
        assertThat(order.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalProductAmount()).isEqualTo(0);
        assertThat(order.getUsedPoint()).isEqualTo(0);
        assertThat(order.getEarnedPoint()).isEqualTo(0);
        assertThat(order.isDeleted()).isFalse();
        assertThat(order.getCreatedAt()).isNull();
        assertThat(order.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("confirm/cancel/changeStatus는 orderStatus를 변경한다")
    void statusTransitions_changeOrderStatus() {
        Order order = newOrder();

        order.confirm();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);

        order.cancel();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        order.changeStatus(OrderStatus.PENDING);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        Order order = newOrder();

        order.delete();

        assertThat(order.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("updateAmounts는 금액 관련 필드를 모두 갱신한다")
    void updateAmounts_changesAmountFields() {
        Order order = newOrder();

        order.updateAmounts(10000, 1000, 500, 300, 1800, 8200, 99L, 300);

        assertThat(order.getTotalProductAmount()).isEqualTo(10000);
        assertThat(order.getProductDiscountAmount()).isEqualTo(1000);
        assertThat(order.getCouponDiscountAmount()).isEqualTo(500);
        assertThat(order.getPointDiscountAmount()).isEqualTo(300);
        assertThat(order.getTotalDiscountAmount()).isEqualTo(1800);
        assertThat(order.getFinalAmount()).isEqualTo(8200);
        assertThat(order.getMemberCouponId()).isEqualTo(99L);
        assertThat(order.getUsedPoint()).isEqualTo(300);
    }

    @Test
    @DisplayName("updateEarnedPoint는 적립 포인트만 갱신한다")
    void updateEarnedPoint_changesEarnedPointOnly() {
        Order order = newOrder();

        order.updateEarnedPoint(500);

        assertThat(order.getEarnedPoint()).isEqualTo(500);
    }

    @Test
    @DisplayName("validateOwnership은 다른 회원이면 AccessDeniedException을 던진다")
    void validateOwnership_throwsWhenNotOwner() {
        Order order = newOrder();
        MemberId otherMemberId = MemberId.of(2L);

        assertThatThrownBy(() -> order.validateOwnership(otherMemberId))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("validateOwnership은 동일 회원이면 예외를 던지지 않는다")
    void validateOwnership_passesWhenOwner() {
        Order order = newOrder();

        order.validateOwnership(MEMBER_ID);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Order order = Order.reconstitute(
            1L,
            MEMBER_ID,
            10L,
            "ORD-001",
            OrderMethod.TABLE,
            OrderStatus.CONFIRMED,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            10000, 1000, 500, 300, 1800, 8200,
            99L,
            300,
            500,
            false,
            createdAt,
            updatedAt
        );

        assertThat(order.getId()).isEqualTo(1L);
        assertThat(order.getOrderId()).isEqualTo(OrderId.of(1L));
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getCreatedAt()).isEqualTo(createdAt);
        assertThat(order.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
