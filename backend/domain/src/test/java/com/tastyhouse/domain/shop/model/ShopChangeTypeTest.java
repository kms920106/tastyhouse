package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShopChangeTypeTest {
    @Test
    @DisplayName("모든 중분류는 대분류와 한글 라벨을 갖는다")
    void everyChangeTypeHasCategoryAndDescription() {
        assertThat(ShopChangeType.values()).allSatisfy(changeType -> {
            assertThat(changeType.getCategory()).isNotNull();
            assertThat(changeType.getDescription()).isNotBlank();
        });
    }

    @Test
    @DisplayName("모든 대분류는 한글 라벨을 갖고, 중분류를 하나 이상 보유한다")
    void everyCategoryHasDescriptionAndAtLeastOneChangeType() {
        assertThat(ShopChangeCategory.values()).allSatisfy(category -> {
            assertThat(category.getDescription()).isNotBlank();
            assertThat(ShopChangeType.values())
                .anyMatch(changeType -> changeType.getCategory() == category);
        });
    }

    @Test
    @DisplayName("from은 상수명을 해당 상수로 승격한다")
    void from_promotesConstantName() {
        assertThat(ShopChangeType.from("DELIVERY_TIP_SCHEDULE")).isEqualTo(ShopChangeType.DELIVERY_TIP_SCHEDULE);
        assertThat(ShopChangeCategory.from("DELIVERY")).isEqualTo(ShopChangeCategory.DELIVERY);
    }

    @Test
    @DisplayName("미해당 중분류 문자열은 BusinessException(SHOP_CHANGE_TYPE_UNKNOWN)이 된다")
    void from_unknownChangeType_throwsBusinessException() {
        assertThatThrownBy(() -> ShopChangeType.from("NOT_A_CHANGE_TYPE"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CHANGE_TYPE_UNKNOWN);
    }

    @Test
    @DisplayName("미해당 대분류 문자열은 BusinessException(SHOP_CHANGE_CATEGORY_UNKNOWN)이 된다")
    void from_unknownCategory_throwsBusinessException() {
        assertThatThrownBy(() -> ShopChangeCategory.from("NOT_A_CATEGORY"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_CHANGE_CATEGORY_UNKNOWN);
    }
}
