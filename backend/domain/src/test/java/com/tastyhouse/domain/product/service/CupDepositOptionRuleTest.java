package com.tastyhouse.domain.product.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 일회용컵 보증금 옵션·옵션그룹 설정 규칙의 순수 단위 테스트.
 *
 * <p>보증금은 결제 금액에 직접 들어가고 비과세 항목으로 분리 저장되므로, "어떤 옵션이 보증금인가"의
 * 판정이 흔들리면 금액이 조용히 틀어진다.
 */
class CupDepositOptionRuleTest {

    private final CupDepositPolicy policy = new CupDepositPolicy();

    @Test
    @DisplayName("★ 보증금 옵션그룹은 필수 선택으로 설정할 수 없다 — 개인컵 손님이 주문 불가가 된다")
    void depositGroup_required_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateDepositGroupSelectRange(
            ProductOptionGroupType.CUP_DEPOSIT, true, false, 0, 1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_CANNOT_BE_REQUIRED);
    }

    @Test
    @DisplayName("★ 보증금 옵션그룹의 선택 개수는 0~1로 고정이다 — 다르면 무시하지 않고 거부한다")
    void depositGroup_nonFixedSelectRange_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateDepositGroupSelectRange(
            ProductOptionGroupType.CUP_DEPOSIT, false, false, 1, 1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_SELECT_FIXED);

        assertThatThrownBy(() -> CupDepositOptionRule.validateDepositGroupSelectRange(
            ProductOptionGroupType.CUP_DEPOSIT, false, true, 0, 1))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_DEPOSIT_SELECT_FIXED);
    }

    @Test
    @DisplayName("고정 제약을 지킨 보증금 그룹은 통과한다")
    void depositGroup_fixedSelectRange_passes() {
        assertThatCode(() -> CupDepositOptionRule.validateDepositGroupSelectRange(
            ProductOptionGroupType.CUP_DEPOSIT, false, false, 0, 1))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("일반 옵션그룹에는 이 제약을 적용하지 않는다")
    void normalGroup_notConstrained() {
        assertThatCode(() -> CupDepositOptionRule.validateDepositGroupSelectRange(
            ProductOptionGroupType.NORMAL, true, true, 1, 5))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("보증금 옵션에는 컵 개수가 필수다")
    void depositOption_missingCupCount_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateOptionValues(
            depositGroup(), 0, null, null, policy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_CUP_COUNT_REQUIRED);
    }

    @Test
    @DisplayName("★ 보증금 옵션에는 추가 금액을 설정할 수 없다 — 추가금과 섞이면 비과세 분리가 무너진다")
    void depositOption_withAdditionalPrice_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateOptionValues(
            depositGroup(), 500, 1, null, policy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_DEPOSIT_ADDITIONAL_PRICE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("컵 개수 범위(1~10)를 벗어나면 거부한다")
    void depositOption_cupCountOutOfRange_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateOptionValues(
            depositGroup(), 0, 11, null, policy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_CUP_COUNT_INVALID);
    }

    @Test
    @DisplayName("정상적인 보증금 옵션은 통과한다")
    void depositOption_valid_passes() {
        assertThatCode(() -> CupDepositOptionRule.validateOptionValues(
            depositGroup(), 0, 1, null, policy))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ 개인컵 옵션은 컵을 주지 않으므로 컵 개수가 없어야 한다")
    void personalCupOption_withCupCount_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateOptionValues(
            depositGroup(), 0, 1, 300, policy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_CUP_COUNT_NOT_ALLOWED);
    }

    @Test
    @DisplayName("개인컵 옵션(컵 개수 없음 + 할인 있음)은 통과한다")
    void personalCupOption_valid_passes() {
        assertThatCode(() -> CupDepositOptionRule.validateOptionValues(
            depositGroup(), 0, null, 300, policy))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("일반 옵션에는 컵 개수를 설정할 수 없다")
    void normalOption_withCupCount_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateOptionValues(
            normalGroup(), 500, 1, null, policy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_CUP_COUNT_NOT_ALLOWED);
    }

    @Test
    @DisplayName("★ 개인컵 옵션은 보증금 옵션그룹 안에서만 만들 수 있다")
    void personalCupOption_inNormalGroup_rejected() {
        assertThatThrownBy(() -> CupDepositOptionRule.validateOptionValues(
            normalGroup(), 0, null, 300, policy))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_PERSONAL_CUP_NOT_IN_DEPOSIT_GROUP);
    }

    @Test
    @DisplayName("일반 옵션은 추가 금액만 있으면 통과한다 — 기존 동작이 그대로 유지된다")
    void normalOption_valid_passes() {
        assertThatCode(() -> CupDepositOptionRule.validateOptionValues(
            normalGroup(), 500, null, null, policy))
            .doesNotThrowAnyException();
    }

    private static ProductOptionGroup depositGroup() {
        return ProductOptionGroup.reconstitute(
            10L, ProductId.of(1L), "일회용컵 보증금", null, false, false, 0, 1, 0, true,
            ProductOptionGroupType.CUP_DEPOSIT
        );
    }

    private static ProductOptionGroup normalGroup() {
        return ProductOptionGroup.reconstitute(
            11L, ProductId.of(1L), "일반그룹", null, false, false, 0, 3, 0, true,
            ProductOptionGroupType.NORMAL
        );
    }
}
