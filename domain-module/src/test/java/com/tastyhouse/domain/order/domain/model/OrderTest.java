package com.tastyhouse.domain.order.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.order.domain.vo.OrderId;
import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.exception.AccessDeniedException;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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

    private Order orderWithStatus(OrderStatus status) {
        return Order.reconstitute(
            1L,
            MEMBER_ID,
            10L,
            "ORD-001",
            OrderMethod.TABLE,
            status,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            10000, 0, 0, 0, 0, 10000,
            null,
            0,
            0,
            false,
            null,
            null
        );
    }

    @Test
    @DisplayName("confirm은 PENDING -> CONFIRMED 전이한다")
    void confirm_fromPending() {
        Order order = newOrder();

        order.confirm();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("cancel은 PENDING과 CONFIRMED에서 CANCELLED로 전이한다")
    void cancel_fromPendingAndConfirmed() {
        Order pending = orderWithStatus(OrderStatus.PENDING);
        pending.cancel();
        assertThat(pending.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        Order confirmed = orderWithStatus(OrderStatus.CONFIRMED);
        confirmed.cancel();
        assertThat(confirmed.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @ParameterizedTest(name = "{0} -> {1} 전이는 허용된다")
    @CsvSource({
        "PENDING, CONFIRMED",
        "PENDING, CANCELLED",
        "CONFIRMED, PREPARING",
        "CONFIRMED, CANCELLED",
        "PREPARING, COMPLETED"
    })
    @DisplayName("전이 테이블이 허용하는 전이는 전부 성공한다")
    void changeStatus_allowsEveryTransitionInTable(OrderStatus from, OrderStatus to) {
        Order order = orderWithStatus(from);

        order.changeStatus(to);

        assertThat(order.getOrderStatus()).isEqualTo(to);
    }

    @ParameterizedTest(name = "{0} -> {1} 전이는 거부된다")
    @CsvSource({
        // 종결 상태에서의 이탈
        "CANCELLED, CONFIRMED",
        "CANCELLED, PENDING",
        "COMPLETED, CANCELLED",
        // 단계 건너뛰기 · 역행
        "PENDING, PREPARING",
        "PENDING, COMPLETED",
        "CONFIRMED, COMPLETED",
        "CONFIRMED, PENDING",
        "PREPARING, CANCELLED",
        // 같은 상태로의 재전이(멱등 호출)도 허용하지 않는다
        "PENDING, PENDING",
        "CONFIRMED, CONFIRMED"
    })
    @DisplayName("전이 테이블에 없는 전이는 BusinessException으로 거부한다")
    void changeStatus_rejectsTransitionsOutsideTable(OrderStatus from, OrderStatus to) {
        Order order = orderWithStatus(from);

        assertThatThrownBy(() -> order.changeStatus(to))
            .isInstanceOf(BusinessException.class);

        assertThat(order.getOrderStatus()).isEqualTo(from);
    }

    @Test
    @DisplayName("CANCELLED 주문의 confirm은 ORDER_ALREADY_CANCELLED로 거부한다")
    void confirm_onCancelledOrder_throws() {
        Order order = orderWithStatus(OrderStatus.CANCELLED);

        assertThatThrownBy(order::confirm)
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_CANCELLED);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("취소 불가 상태는 사유별로 구분된 에러 코드를 던진다")
    void cancel_rejectedWithStatusSpecificErrorCode() {
        assertThatThrownBy(() -> orderWithStatus(OrderStatus.CANCELLED).cancel())
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_CANCELLED);

        assertThatThrownBy(() -> orderWithStatus(OrderStatus.COMPLETED).cancel())
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_COMPLETED);

        assertThatThrownBy(() -> orderWithStatus(OrderStatus.PREPARING).cancel())
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_ALREADY_PREPARING);
    }

    @Test
    @DisplayName("reconstitute는 전이 가드를 태우지 않는다(DB에 어떤 상태가 있어도 로드 가능)")
    void reconstitute_bypassesTransitionGuard() {
        for (OrderStatus status : OrderStatus.values()) {
            assertThat(orderWithStatus(status).getOrderStatus()).isEqualTo(status);
        }
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
    @DisplayName("updateAmounts는 총 할인이 항목 합과 다르면 거부한다")
    void updateAmounts_rejectsInconsistentTotalDiscount() {
        Order order = newOrder();

        // 1000 + 500 + 300 = 1800 인데 총 할인을 1700으로 보냄
        assertThatThrownBy(() -> order.updateAmounts(10000, 1000, 500, 300, 1700, 8300, 99L, 300))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);

        assertThat(order.getTotalProductAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("updateAmounts는 최종 결제 금액이 상품금액-총할인과 다르면 거부한다")
    void updateAmounts_rejectsInconsistentFinalAmount() {
        Order order = newOrder();

        // 10000 - 1800 = 8200 인데 최종 금액을 9000으로 보냄
        assertThatThrownBy(() -> order.updateAmounts(10000, 1000, 500, 300, 1800, 9000, 99L, 300))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
    }

    @Test
    @DisplayName("updateAmounts는 음수 금액을 거부한다")
    void updateAmounts_rejectsNegativeAmounts() {
        Order order = newOrder();

        // 상품 금액 음수 (합산 정합 자체는 성립: -100 - 0 = -100)
        assertThatThrownBy(() -> order.updateAmounts(-100, 0, 0, 0, 0, -100, null, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);

        // 할인 항목 음수 (합산 정합 성립: -500 + 0 + 0 = -500, 10000 - (-500) = 10500)
        assertThatThrownBy(() -> order.updateAmounts(10000, -500, 0, 0, -500, 10500, null, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);

        // 사용 포인트 음수
        assertThatThrownBy(() -> order.updateAmounts(10000, 0, 0, 0, 0, 10000, null, -1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
    }

    @Test
    @DisplayName("updateAmounts는 할인이 상품 금액을 초과해 최종 금액이 음수가 되면 거부한다")
    void updateAmounts_rejectsNegativeFinalAmount() {
        Order order = newOrder();

        // 10000원 주문에 15000원 정액 쿠폰 → finalAmount -5000 (합산 정합 자체는 성립)
        assertThatThrownBy(() -> order.updateAmounts(10000, 0, 15000, 0, 15000, -5000, 99L, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
    }

    @Test
    @DisplayName("updateAmounts는 null 금액을 0으로 정규화해 검증하고 저장한다")
    void updateAmounts_normalizesNullToZero() {
        Order order = newOrder();

        order.updateAmounts(null, null, null, null, null, null, null, null);

        // 검증만 0으로 보고 저장은 raw null을 넣으면 불변식을 위반한 상태가 저장된다 — 저장값도 0이어야 한다
        assertThat(order.getTotalProductAmount()).isEqualTo(0);
        assertThat(order.getProductDiscountAmount()).isEqualTo(0);
        assertThat(order.getCouponDiscountAmount()).isEqualTo(0);
        assertThat(order.getPointDiscountAmount()).isEqualTo(0);
        assertThat(order.getTotalDiscountAmount()).isEqualTo(0);
        assertThat(order.getFinalAmount()).isEqualTo(0);
        assertThat(order.getUsedPoint()).isEqualTo(0);
    }

    @Test
    @DisplayName("updateAmounts는 부분 null 입력도 0으로 정규화해 정합을 판정한다")
    void updateAmounts_normalizesPartialNull() {
        Order order = newOrder();

        // 총 할인·항목 할인을 null로 보내면 0으로 정규화되어 10000 - 0 = 10000과 정합해야 통과
        order.updateAmounts(10000, null, null, null, null, 10000, null, null);

        assertThat(order.getTotalDiscountAmount()).isEqualTo(0);
        assertThat(order.getFinalAmount()).isEqualTo(10000);

        // 같은 부분 null 입력이지만 최종 금액이 정합하지 않으면 거부한다
        assertThatThrownBy(() -> order.updateAmounts(10000, null, null, null, null, 9000, null, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
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
