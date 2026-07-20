package com.tastyhouse.core.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopPhotoCategoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientPhotoCategory() {
        ShopPhotoCategory photoCategory = ShopPhotoCategory.of(1L, "가게 외관");

        assertThat(photoCategory.getId()).isNull();
        assertThat(photoCategory.getShopId()).isEqualTo(1L);
        assertThat(photoCategory.getName()).isEqualTo("가게 외관");
    }

    @Test
    @DisplayName("update는 카테고리명을 변경한다")
    void update_changesName() {
        ShopPhotoCategory photoCategory = ShopPhotoCategory.of(1L, "가게 외관");

        photoCategory.update("메뉴");

        assertThat(photoCategory.getName()).isEqualTo("메뉴");
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopPhotoCategory photoCategory = ShopPhotoCategory.reconstitute(1L, 2L, "가게 외관");

        assertThat(photoCategory.getId()).isEqualTo(1L);
        assertThat(photoCategory.getShopId()).isEqualTo(2L);
    }
}
