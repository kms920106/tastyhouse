package com.tastyhouse.domain.order.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderDeliveryDestination;
import com.tastyhouse.domain.order.vo.OrderSchedule;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
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
    private static final ShopId SHOP_ID = ShopId.of(10L);

    private Order newOrder() {
        return Order.of(
            MEMBER_ID,
            SHOP_ID,
            "ORD-001",
            OrderMethod.TABLE,
            null,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            null, null, null, null, null,
            null,
            0,
            null,
            null,
            null,
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
            SHOP_ID,
            "ORD-001",
            OrderMethod.TABLE,
            status,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            10000, 0, 0, 0, 0,
            0,
            0,
            10000,
            OrderDeliveryDestination.none(),
            OrderSchedule.none(),
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

        order.updateAmounts(10000, 1000, 500, 300, 1800, 0, 0, 8200, OrderDeliveryDestination.none(), OrderSchedule.none(), MemberCouponId.of(99L), 300);

        assertThat(order.getTotalProductAmount()).isEqualTo(10000);
        assertThat(order.getProductDiscountAmount()).isEqualTo(1000);
        assertThat(order.getCouponDiscountAmount()).isEqualTo(500);
        assertThat(order.getPointDiscountAmount()).isEqualTo(300);
        assertThat(order.getTotalDiscountAmount()).isEqualTo(1800);
        assertThat(order.getFinalAmount()).isEqualTo(8200);
        assertThat(order.getMemberCouponId()).isEqualTo(MemberCouponId.of(99L));
        assertThat(order.getUsedPoint()).isEqualTo(300);
    }

    @Test
    @DisplayName("updateAmounts는 총 할인이 항목 합과 다르면 거부한다")
    void updateAmounts_rejectsInconsistentTotalDiscount() {
        Order order = newOrder();

        // 1000 + 500 + 300 = 1800 인데 총 할인을 1700으로 보냄
        assertThatThrownBy(() -> order.updateAmounts(10000, 1000, 500, 300, 1700, 0, 0, 8300, OrderDeliveryDestination.none(), OrderSchedule.none(), MemberCouponId.of(99L), 300))
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
        assertThatThrownBy(() -> order.updateAmounts(10000, 1000, 500, 300, 1800, 0, 0, 9000, OrderDeliveryDestination.none(), OrderSchedule.none(), MemberCouponId.of(99L), 300))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
    }

    @Test
    @DisplayName("updateAmounts는 음수 금액을 거부한다")
    void updateAmounts_rejectsNegativeAmounts() {
        Order order = newOrder();

        // 상품 금액 음수 (합산 정합 자체는 성립: -100 - 0 = -100)
        assertThatThrownBy(() -> order.updateAmounts(-100, 0, 0, 0, 0, 0, 0, -100, null, null, null, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);

        // 할인 항목 음수 (합산 정합 성립: -500 + 0 + 0 = -500, 10000 - (-500) = 10500)
        assertThatThrownBy(() -> order.updateAmounts(10000, -500, 0, 0, -500, 0, 0, 10500, null, null, null, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);

        // 사용 포인트 음수
        assertThatThrownBy(() -> order.updateAmounts(10000, 0, 0, 0, 0, 0, 0, 10000, null, null, null, -1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
    }

    @Test
    @DisplayName("updateAmounts는 할인이 상품 금액을 초과해 최종 금액이 음수가 되면 거부한다")
    void updateAmounts_rejectsNegativeFinalAmount() {
        Order order = newOrder();

        // 10000원 주문에 15000원 정액 쿠폰 → finalAmount -5000 (합산 정합 자체는 성립)
        assertThatThrownBy(() -> order.updateAmounts(10000, 0, 15000, 0, 15000, 0, 0, -5000, OrderDeliveryDestination.none(), OrderSchedule.none(), MemberCouponId.of(99L), 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
    }

    @Test
    @DisplayName("updateAmounts는 null 금액을 0으로 정규화해 검증하고 저장한다")
    void updateAmounts_normalizesNullToZero() {
        Order order = newOrder();

        order.updateAmounts(null, null, null, null, null, null, 0, null, null, null, null, null);

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
        order.updateAmounts(10000, null, null, null, null, null, 0, 10000, null, null, null, null);

        assertThat(order.getTotalDiscountAmount()).isEqualTo(0);
        assertThat(order.getFinalAmount()).isEqualTo(10000);

        // 같은 부분 null 입력이지만 최종 금액이 정합하지 않으면 거부한다
        assertThatThrownBy(() -> order.updateAmounts(10000, null, null, null, null, null, 0, 9000, null, null, null, null))
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
    @DisplayName("validateOwnership은 다른 회원이면 BusinessException을 던진다")
    void validateOwnership_throwsWhenNotOwner() {
        Order order = newOrder();
        MemberId otherMemberId = MemberId.of(2L);

        assertThatThrownBy(() -> order.validateOwnership(otherMemberId))
            .isInstanceOf(BusinessException.class);
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
            SHOP_ID,
            "ORD-001",
            OrderMethod.TABLE,
            OrderStatus.CONFIRMED,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            10000, 1000, 500, 300, 1800,
            0,
            0,
            8200,
            OrderDeliveryDestination.none(),
            OrderSchedule.none(),
            MemberCouponId.of(99L),
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

    /**
     * {@code of()} 금액 정합 불변식 — {@code updateAmounts}와 같은 검증({@code validateAmountConsistency})을
     * 공유하므로, 여기서는 생성 경로에서도 그 검증이 실제로 걸리는지를 본다.
     */
    private Order orderWithAmounts(
        Integer totalProductAmount,
        Integer productDiscountAmount,
        Integer couponDiscountAmount,
        Integer pointDiscountAmount,
        Integer totalDiscountAmount,
        Integer finalAmount,
        Integer usedPoint
    ) {
        return Order.of(
            MEMBER_ID,
            SHOP_ID,
            "ORD-001",
            OrderMethod.TABLE,
            null,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            totalProductAmount,
            productDiscountAmount,
            couponDiscountAmount,
            pointDiscountAmount,
            totalDiscountAmount,
            0,
            0,
            finalAmount,
            OrderDeliveryDestination.none(),
            OrderSchedule.none(),
            null,
            usedPoint,
            0
        );
    }

    @Test
    @DisplayName("of는 금액 전부 생략(모두 0) 시 통과한다 — 기존 주문 접수 플로 무회귀")
    void of_allZeroAmounts_passes() {
        Order order = orderWithAmounts(null, null, null, null, null, null, null);

        assertThat(order.getTotalProductAmount()).isZero();
        assertThat(order.getFinalAmount()).isZero();
    }

    @Test
    @DisplayName("of는 정합이 맞는 금액 조합을 통과시킨다(경계: 전액 할인으로 결제 금액 0)")
    void of_consistentAmounts_passes() {
        Order order = orderWithAmounts(10000, 1000, 500, 300, 1800, 8200, 300);
        assertThat(order.getFinalAmount()).isEqualTo(8200);

        Order fullyDiscounted = orderWithAmounts(10000, 10000, 0, 0, 10000, 0, 0);
        assertThat(fullyDiscounted.getFinalAmount()).isZero();
    }

    @Test
    @DisplayName("of는 음수 금액을 ORDER_AMOUNT_NEGATIVE로 거부한다")
    void of_negativeAmount_throws() {
        assertThatThrownBy(() -> orderWithAmounts(-1, 0, 0, 0, 0, -1, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
    }

    @Test
    @DisplayName("of는 총 할인이 항목 합과 다르면 ORDER_AMOUNT_NOT_CONSISTENT로 거부한다")
    void of_discountSumMismatch_throws() {
        assertThatThrownBy(() -> orderWithAmounts(10000, 1000, 500, 300, 9999, 1, 300))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
    }

    @Test
    @DisplayName("of는 결제 금액이 상품금액-총할인과 다르면 ORDER_AMOUNT_NOT_CONSISTENT로 거부한다")
    void of_finalAmountMismatch_throws() {
        assertThatThrownBy(() -> orderWithAmounts(10000, 1000, 500, 300, 1800, 9999, 300))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
    }

    @Test
    @DisplayName("reconstitute는 금액 정합 검증을 하지 않는다(불변식 위반 레거시 행도 로드 가능)")
    void reconstitute_bypassesAmountValidation() {
        Order order = Order.reconstitute(
            1L,
            MEMBER_ID,
            SHOP_ID,
            "ORD-001",
            OrderMethod.TABLE,
            OrderStatus.CONFIRMED,
            "홍길동",
            "010-1234-5678",
            "hong@test.com",
            -1, 0, 0, 0, 9999,
            0,
            0,
            -12345,
            OrderDeliveryDestination.none(),
            OrderSchedule.none(),
            null,
            0,
            0,
            false,
            null,
            null
        );

        assertThat(order.getFinalAmount()).isEqualTo(-12345);
        assertThat(order.getTotalDiscountAmount()).isEqualTo(9999);
    }

    /**
     * 배달팁 도입으로 확장된 금액 정합 계약 —
     * {@code finalAmount == totalProductAmount - totalDiscountAmount + deliveryTipAmount}.
     */
    @Nested
    @DisplayName("배달팁 금액 정합")
    class DeliveryTipAmount {

        @Test
        @DisplayName("최종 금액이 상품금액 − 할인 + 배달팁이면 통과한다")
        void of_finalAmountIncludingDeliveryTip_passes() {
            Order order = orderWithDeliveryTip(1000, 500, 300, 1800, 3000, 11200);

            assertThat(order.getDeliveryTipAmount()).isEqualTo(3000);
            assertThat(order.getFinalAmount()).isEqualTo(11200);
        }

        @Test
        @DisplayName("배달팁을 빼먹은 최종 금액은 ORDER_AMOUNT_NOT_CONSISTENT로 거부한다")
        void of_finalAmountMissingDeliveryTip_throws() {
            // 10000 - 1800 + 3000 = 11200 인데 배달팁을 빠뜨린 8200을 보냄
            assertThatThrownBy(() -> orderWithDeliveryTip(1000, 500, 300, 1800, 3000, 8200))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
        }

        @Test
        @DisplayName("배달팁이 음수면 ORDER_AMOUNT_NEGATIVE로 거부한다")
        void of_negativeDeliveryTip_throws() {
            // 합산 정합 자체는 성립: 10000 - 0 + (-500) = 9500
            assertThatThrownBy(() -> orderWithDeliveryTip(0, 0, 0, 0, -500, 9500))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
        }

        @Test
        @DisplayName("updateAmounts도 배달팁을 포함한 정합을 강제한다")
        void updateAmounts_enforcesDeliveryTipConsistency() {
            Order order = newOrder();

            order.updateAmounts(10000, 1000, 500, 300, 1800, 3000, 0, 11200, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 300);

            assertThat(order.getDeliveryTipAmount()).isEqualTo(3000);
            assertThat(order.getFinalAmount()).isEqualTo(11200);

            assertThatThrownBy(() -> order.updateAmounts(
                10000, 1000, 500, 300, 1800, 3000, 0, 8200, OrderDeliveryDestination.none(), OrderSchedule.none(), null, 300
            ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
        }

        @Test
        @DisplayName("배달팁이 0이면 기존 식(상품 − 할인)과 동일하게 성립한다 — 하위호환 회귀 방어")
        void of_zeroDeliveryTip_matchesLegacyFormula() {
            Order order = orderWithDeliveryTip(1000, 500, 300, 1800, 0, 8200);

            assertThat(order.getDeliveryTipAmount()).isZero();
            assertThat(order.getFinalAmount()).isEqualTo(8200);
        }

        @Test
        @DisplayName("reconstitute는 배달팁 정합을 검증하지 않는다 — 배달팁 0원인 기존 행도 그대로 로드된다")
        void reconstitute_passesLegacyZeroDeliveryTipRow() {
            Order legacy = Order.reconstitute(
                1L,
                MEMBER_ID,
                SHOP_ID,
                "ORD-LEGACY",
                OrderMethod.DELIVERY,
                OrderStatus.COMPLETED,
                "홍길동",
                "010-1234-5678",
                "hong@test.com",
                10000, 1000, 500, 300, 1800,
                0,
                0,
                8200,
                OrderDeliveryDestination.none(),
                OrderSchedule.none(),
                null,
                300,
                0,
                false,
                null,
                null
            );

            assertThat(legacy.getDeliveryTipAmount()).isZero();
            assertThat(legacy.getFinalAmount()).isEqualTo(8200);
            assertThat(legacy.getDeliveryDestination().isPresent()).isFalse();
        }

        private Order orderWithDeliveryTip(
            Integer productDiscountAmount,
            Integer couponDiscountAmount,
            Integer pointDiscountAmount,
            Integer totalDiscountAmount,
            Integer deliveryTipAmount,
            Integer finalAmount
        ) {
            return Order.of(
                MEMBER_ID,
                SHOP_ID,
                "ORD-001",
                OrderMethod.DELIVERY,
                null,
                "홍길동",
                "010-1234-5678",
                "hong@test.com",
                10000,
                productDiscountAmount,
                couponDiscountAmount,
                pointDiscountAmount,
                totalDiscountAmount,
                deliveryTipAmount,
                0,
                finalAmount,
                OrderDeliveryDestination.none(),
                OrderSchedule.none(),
                null,
                0,
                0
            );
        }
    }

    @Nested
    @DisplayName("일회용컵 보증금 금액 정합")
    class CupDepositAmountConsistency {

        @Test
        @DisplayName("★ finalAmount는 상품 − 할인 + 배달팁 + 보증금이다")
        void updateAmounts_includesCupDepositInFinalAmount() {
            Order order = newOrder();

            order.updateAmounts(10000, 1000, 0, 0, 1000, 3000, 600, 12600,
                OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0);

            assertThat(order.getCupDepositAmount()).isEqualTo(600);
            assertThat(order.getFinalAmount()).isEqualTo(12600);
        }

        @Test
        @DisplayName("보증금을 빠뜨린 finalAmount는 거부한다")
        void updateAmounts_finalAmountMissingDeposit_rejected() {
            Order order = newOrder();

            assertThatThrownBy(() -> order.updateAmounts(10000, 1000, 0, 0, 1000, 3000, 600, 12000,
                OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_AMOUNT_NOT_CONSISTENT);
        }

        @Test
        @DisplayName("★ 보증금이 0이면 기존 케이스가 전부 그대로 성립한다 — 하위호환 회귀 방어")
        void updateAmounts_zeroDeposit_behavesAsBefore() {
            Order order = newOrder();

            order.updateAmounts(10000, 1000, 500, 300, 1800, 3000, 0, 11200,
                OrderDeliveryDestination.none(), OrderSchedule.none(), null, 300);

            assertThat(order.getCupDepositAmount()).isZero();
            assertThat(order.getFinalAmount()).isEqualTo(11200);
        }

        @Test
        @DisplayName("음수 보증금은 거부한다")
        void updateAmounts_negativeDeposit_rejected() {
            Order order = newOrder();

            assertThatThrownBy(() -> order.updateAmounts(10000, 0, 0, 0, 0, 0, -300, 9700,
                OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_AMOUNT_NEGATIVE);
        }

        @Test
        @DisplayName("★ 보증금은 totalProductAmount에 섞이지 않는다 — 최소주문금액·쿠폰·포인트 기준액 보호")
        void cupDeposit_isNotPartOfTotalProductAmount() {
            Order order = newOrder();

            order.updateAmounts(10000, 0, 0, 0, 0, 0, 900, 10900,
                OrderDeliveryDestination.none(), OrderSchedule.none(), null, 0);

            // 상품 금액은 보증금과 무관하게 유지되어야 한다 — 이 값이 최소주문금액·쿠폰 기준액이다.
            assertThat(order.getTotalProductAmount()).isEqualTo(10000);
            assertThat(order.getCupDepositAmount()).isEqualTo(900);
        }
    }
}
