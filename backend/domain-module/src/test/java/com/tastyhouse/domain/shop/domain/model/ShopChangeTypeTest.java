package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 가게 변경이력 분류 카탈로그 단위 테스트.
 *
 * <p>중분류는 자기 대분류를 보유하고, 이 대응 관계가 그대로 필터 카탈로그 API로 나간다. 상수를 추가할 때
 * 대분류·라벨을 빠뜨리면 화면 드롭다운에 이름 없는 항목이 뜨므로 여기서 전수 검증한다.
 */
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
