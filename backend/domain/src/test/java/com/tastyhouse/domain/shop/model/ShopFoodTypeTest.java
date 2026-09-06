package com.tastyhouse.domain.shop.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.shop.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;

class ShopFoodTypeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientShopFoodType() {
        ShopFoodType foodType = ShopFoodType.of(ShopId.of(1L), ShopFoodTypeCategoryId.of(2L));

        assertThat(foodType.getId()).isNull();
        assertThat(foodType.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(foodType.getShopFoodTypeCategoryId()).isEqualTo(ShopFoodTypeCategoryId.of(2L));
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopFoodType foodType = ShopFoodType.reconstitute(1L, ShopId.of(2L), ShopFoodTypeCategoryId.of(3L));

        assertThat(foodType.getId()).isEqualTo(1L);
        assertThat(foodType.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(foodType.getShopFoodTypeCategoryId()).isEqualTo(ShopFoodTypeCategoryId.of(3L));
    }
}
