package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopAmenityCategoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientAmenityCategory() {
        ShopAmenityCategory category = ShopAmenityCategory.of(Amenity.PARKING, "와이파이", 1L, 2L, 1, true);

        assertThat(category.getId()).isNull();
        assertThat(category.getAmenity()).isEqualTo(Amenity.PARKING);
        assertThat(category.getDisplayName()).isEqualTo("와이파이");
        assertThat(category.getActiveImageFileId()).isEqualTo(1L);
        assertThat(category.getInactiveImageFileId()).isEqualTo(2L);
        assertThat(category.getSort()).isEqualTo(1);
        assertThat(category.isVisible()).isTrue();
    }

    @Test
    @DisplayName("update는 표시명·이미지·정렬·노출 여부를 변경한다")
    void update_changesFields() {
        ShopAmenityCategory category = ShopAmenityCategory.of(Amenity.PARKING, "와이파이", 1L, 2L, 1, true);

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
        ShopAmenityCategory category = ShopAmenityCategory.reconstitute(1L, Amenity.PARKING, "와이파이", 1L, 2L, 1, true);

        assertThat(category.getId()).isEqualTo(1L);
    }
}
