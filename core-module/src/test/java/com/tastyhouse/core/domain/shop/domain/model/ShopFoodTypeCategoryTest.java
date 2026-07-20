package com.tastyhouse.core.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopFoodTypeCategoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientFoodTypeCategory() {
        ShopFoodTypeCategory category = ShopFoodTypeCategory.of(FoodType.KOREAN, "한식", 1L, 2L, 1, true);

        assertThat(category.getId()).isNull();
        assertThat(category.getFoodType()).isEqualTo(FoodType.KOREAN);
        assertThat(category.getDisplayName()).isEqualTo("한식");
        assertThat(category.getActiveImageFileId()).isEqualTo(1L);
        assertThat(category.getInactiveImageFileId()).isEqualTo(2L);
        assertThat(category.getSort()).isEqualTo(1);
        assertThat(category.isVisible()).isTrue();
    }

    @Test
    @DisplayName("update는 표시명·이미지·정렬·노출 여부를 변경한다")
    void update_changesFields() {
        ShopFoodTypeCategory category = ShopFoodTypeCategory.of(FoodType.KOREAN, "한식", 1L, 2L, 1, true);

        category.update("새 이름", 10L, 20L, 2, false);

        assertThat(category.getDisplayName()).isEqualTo("새 이름");
        assertThat(category.getActiveImageFileId()).isEqualTo(10L);
        assertThat(category.getInactiveImageFileId()).isEqualTo(20L);
        assertThat(category.getSort()).isEqualTo(2);
        assertThat(category.isVisible()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopFoodTypeCategory category = ShopFoodTypeCategory.reconstitute(1L, FoodType.KOREAN, "한식", 1L, 2L, 1, true);

        assertThat(category.getId()).isEqualTo(1L);
    }
}
