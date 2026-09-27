package com.tastyhouse.application.shop.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopNoticeImage;

import static org.assertj.core.api.Assertions.assertThat;

class ShopNoticeImageStateMapperTest {

    @Test
    @DisplayName("ShopNoticeImage → ShopNoticeImageState → ShopNoticeImage 왕복 시 모든 필드가 보존된다")
    void shopNoticeImageRoundTrip() {
        ShopNoticeImage original = ShopNoticeImage.reconstitute(
            108L,
            109L,
            UploadedFileId.of(110L),
            11
        );

        ShopNoticeImage restored = ShopNoticeImageStateMapper.toDomain(ShopNoticeImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
