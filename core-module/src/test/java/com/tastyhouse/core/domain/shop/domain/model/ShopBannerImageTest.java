package com.tastyhouse.core.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopBannerImageTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBannerImage() {
        ShopBannerImage bannerImage = ShopBannerImage.of(1L, 10L, 1);

        assertThat(bannerImage.getId()).isNull();
        assertThat(bannerImage.getShopId()).isEqualTo(1L);
        assertThat(bannerImage.getImageFileId()).isEqualTo(10L);
        assertThat(bannerImage.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBannerImage bannerImage = ShopBannerImage.reconstitute(1L, 2L, 10L, 1);

        assertThat(bannerImage.getId()).isEqualTo(1L);
        assertThat(bannerImage.getShopId()).isEqualTo(2L);
    }
}
