package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

class ShopBannerImageTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientBannerImage() {
        ShopBannerImage bannerImage = ShopBannerImage.of(ShopId.of(1L), UploadedFileId.of(10L), 1);

        assertThat(bannerImage.getId()).isNull();
        assertThat(bannerImage.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(bannerImage.getImageFileId()).isEqualTo(UploadedFileId.of(10L));
        assertThat(bannerImage.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopBannerImage bannerImage = ShopBannerImage.reconstitute(1L, ShopId.of(2L), UploadedFileId.of(10L), 1);

        assertThat(bannerImage.getId()).isEqualTo(1L);
        assertThat(bannerImage.getShopId()).isEqualTo(ShopId.of(2L));
    }
}
