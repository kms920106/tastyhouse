package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopPhotoCategoryImageTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientPhotoCategoryImage() {
        ShopPhotoCategoryImage image = ShopPhotoCategoryImage.of(1L, 10L, 1, true);

        assertThat(image.getId()).isNull();
        assertThat(image.getShopPhotoCategoryId()).isEqualTo(1L);
        assertThat(image.getImageFileId()).isEqualTo(10L);
        assertThat(image.getSort()).isEqualTo(1);
        assertThat(image.isVisible()).isTrue();
    }

    @Test
    @DisplayName("update는 이미지·정렬·노출 여부를 변경한다")
    void update_changesFields() {
        ShopPhotoCategoryImage image = ShopPhotoCategoryImage.of(1L, 10L, 1, true);

        image.update(20L, 2, false);

        assertThat(image.getImageFileId()).isEqualTo(20L);
        assertThat(image.getSort()).isEqualTo(2);
        assertThat(image.isVisible()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopPhotoCategoryImage image = ShopPhotoCategoryImage.reconstitute(1L, 2L, 10L, 1, true);

        assertThat(image.getId()).isEqualTo(1L);
        assertThat(image.getShopPhotoCategoryId()).isEqualTo(2L);
    }
}
