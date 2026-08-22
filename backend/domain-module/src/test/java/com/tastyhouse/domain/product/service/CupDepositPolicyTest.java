package com.tastyhouse.domain.product.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 일회용컵 보증금 정책의 순수 단위 테스트.
 *
 * <p>요율이 한 곳에만 있어야 "화면에 보이는 금액과 결제 금액이 다른" 사고가 구조적으로 불가능해진다.
 */
class CupDepositPolicyTest {

    private final CupDepositPolicy policy = new CupDepositPolicy();

    @Test
    @DisplayName("보증금은 컵 개수 × 300원이다")
    void depositAmountOf_multipliesByRate() {
        assertThat(policy.depositAmountOf(1)).isEqualTo(300);
        assertThat(policy.depositAmountOf(2)).isEqualTo(600);
        assertThat(policy.depositAmountOf(10)).isEqualTo(3000);
    }

    @Test
    @DisplayName("컵 개수가 없거나 0이면 보증금은 0원이다 — 일반 옵션이 이 경로로 들어와도 금액에 영향이 없다")
    void depositAmountOf_nullOrZero_isZero() {
        assertThat(policy.depositAmountOf(null)).isZero();
        assertThat(policy.depositAmountOf(0)).isZero();
    }

    @Test
    @DisplayName("상한(10개)을 넘는 컵 개수는 거부한다")
    void depositAmountOf_exceedingMax_rejected() {
        assertThatThrownBy(() -> policy.depositAmountOf(11))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_CUP_COUNT_INVALID);
    }

    @Test
    @DisplayName("컵 개수 검증은 1~10만 통과한다")
    void validateCupCount_range() {
        assertThatCode(() -> policy.validateCupCount(1)).doesNotThrowAnyException();
        assertThatCode(() -> policy.validateCupCount(10)).doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.validateCupCount(0))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> policy.validateCupCount(11))
            .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> policy.validateCupCount(null))
            .isInstanceOf(BusinessException.class);
    }
}
