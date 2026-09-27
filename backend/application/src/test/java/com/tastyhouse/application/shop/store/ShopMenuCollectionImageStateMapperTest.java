package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopMenuCollectionImageStateMapperTest {

    @Test
    @DisplayName("ShopMenuCollectionImage → ShopMenuCollectionImageState → ShopMenuCollectionImage 왕복 시 모든 필드가 보존된다")
    void shopMenuCollectionImageRoundTrip() {
        ShopMenuCollectionImage original = ShopMenuCollectionImage.reconstitute(
            139L,
            ShopId.of(140L),
            UploadedFileId.of(141L),
            42,
            ApprovalStatus.CANCELED,
            "v44",
            LocalDateTime.of(2026, 1, 18, 10, 45),
            LocalDateTime.of(2026, 1, 19, 10, 46)
        );

        ShopMenuCollectionImage restored = ShopMenuCollectionImageStateMapper.toDomain(ShopMenuCollectionImageStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
