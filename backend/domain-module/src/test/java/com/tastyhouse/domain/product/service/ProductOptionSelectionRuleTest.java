package com.tastyhouse.domain.product.service;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 옵션 잔여 개수·0원 옵션 규칙의 순수 단위 테스트.
 *
 * <p><b>이 규칙이 한 곳에 있어야 하는 이유가 곧 이 테스트의 존재 이유다</b> — 과거에는 일괄 숨김과
 * 개별 삭제가 서로 다른 하한을 써서, 일괄로는 막히는 상태를 개별 삭제로는 만들 수 있었다.
 */
class ProductOptionSelectionRuleTest {

    @Test
    @DisplayName("★ 잔여 하한은 max(minSelect, maxSelect, 1)이다")
    void minRemaining_isMaxOfBounds() {
        assertThat(ProductOptionSelectionRule.minRemaining(null, null)).isEqualTo(1);
        assertThat(ProductOptionSelectionRule.minRemaining(0, 0)).isEqualTo(1);
        assertThat(ProductOptionSelectionRule.minRemaining(2, null)).isEqualTo(2);
        assertThat(ProductOptionSelectionRule.minRemaining(null, 3)).isEqualTo(3);
        assertThat(ProductOptionSelectionRule.minRemaining(2, 5)).isEqualTo(5);
    }

    @Test
    @DisplayName("★ minSelect가 0·null이어도 마지막 옵션은 남긴다 — 옵션 0개인 그룹은 주문 불가를 만든다")
    void validateRemaining_lastOption_rejected() {
        ProductOptionGroup group = group(null, null);
        ProductOption only = option(1L, 0, true);

        assertThatThrownBy(() ->
            ProductOptionSelectionRule.validateRemainingAfterBlocking(group, only, List.of(only)))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_MIN_SELECT_VIOLATION);
    }

    @Test
    @DisplayName("★ maxSelect가 하한을 끌어올리면 전용 사유 코드로 거부한다")
    void validateRemaining_maxSelectBound_usesMaxSelectCode() {
        ProductOptionGroup group = group(null, 3);
        ProductOption target = option(1L, 0, true);
        List<ProductOption> options = List.of(target, option(2L, 0, true), option(3L, 0, true));

        assertThatThrownBy(() ->
            ProductOptionSelectionRule.validateRemainingAfterBlocking(group, target, options))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_MAX_SELECT_VIOLATION);
    }

    @Test
    @DisplayName("하한을 채우고도 남으면 통과한다")
    void validateRemaining_enoughRemaining_passes() {
        ProductOptionGroup group = group(1, null);
        ProductOption target = option(1L, 0, true);
        List<ProductOption> options = List.of(target, option(2L, 0, true));

        assertThatCode(() ->
            ProductOptionSelectionRule.validateRemainingAfterBlocking(group, target, options))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 선택 불가인 옵션을 감추는 것은 개수를 줄이지 않으므로 통과한다(멱등)")
    void validateRemaining_alreadyBlockedTarget_passes() {
        ProductOptionGroup group = group(1, null);
        ProductOption hidden = option(1L, 0, false);

        assertThatCode(() ->
            ProductOptionSelectionRule.validateRemainingAfterBlocking(group, hidden, List.of(hidden)))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ 필수 그룹은 0원 옵션을 1개 이상 포함해야 한다 — 순차공개 가격책정 금지")
    void validateZeroPriceOption_requiredGroupWithoutZeroPrice_rejected() {
        ProductOptionGroup required = requiredGroup();

        assertThatThrownBy(() -> ProductOptionSelectionRule.validateZeroPriceOption(
            required, List.of(option(1L, 500, true), option(2L, 1000, true))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_REQUIRES_ZERO_PRICE_OPTION);
    }

    @Test
    @DisplayName("0원 옵션이 남아 있으면 통과한다")
    void validateZeroPriceOption_withZeroPrice_passes() {
        assertThatCode(() -> ProductOptionSelectionRule.validateZeroPriceOption(
            requiredGroup(), List.of(option(1L, 0, true), option(2L, 1000, true))))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ 숨은 0원 옵션은 그 역할을 대신하지 못한다 — 손님이 고를 수 없기 때문이다")
    void validateZeroPriceOption_hiddenZeroPrice_rejected() {
        assertThatThrownBy(() -> ProductOptionSelectionRule.validateZeroPriceOption(
            requiredGroup(), List.of(option(1L, 0, false), option(2L, 1000, true))))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.PRODUCT_OPTION_GROUP_REQUIRES_ZERO_PRICE_OPTION);
    }

    @Test
    @DisplayName("필수가 아닌 그룹에는 적용하지 않는다")
    void validateZeroPriceOption_optionalGroup_passes() {
        assertThatCode(() -> ProductOptionSelectionRule.validateZeroPriceOption(
            group(0, null), List.of(option(1L, 500, true))))
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("★ 옵션이 0건인 그룹은 통과시킨다 — 그룹은 옵션보다 먼저 만들어진다")
    void validateZeroPriceOption_emptyGroup_passes() {
        assertThatCode(() -> ProductOptionSelectionRule.validateZeroPriceOption(requiredGroup(), List.of()))
            .doesNotThrowAnyException();
    }

    private static ProductOptionGroup group(Integer minSelect, Integer maxSelect) {
        return ProductOptionGroup.reconstitute(
            10L, ProductId.of(1L), "그룹", null, false, false, minSelect, maxSelect, 0, true,
            ProductOptionGroupType.NORMAL
        );
    }

    private static ProductOptionGroup requiredGroup() {
        return ProductOptionGroup.reconstitute(
            10L, ProductId.of(1L), "필수그룹", null, true, false, 1, 1, 0, true,
            ProductOptionGroupType.NORMAL
        );
    }

    private static ProductOption option(Long id, Integer additionalPrice, boolean visible) {
        return ProductOption.reconstitute(
            id, ProductOptionGroupId.of(10L), "옵션" + id, additionalPrice, 0, false, null, visible, null, null
        );
    }
}
