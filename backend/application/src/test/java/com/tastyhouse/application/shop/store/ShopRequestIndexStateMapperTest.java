package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopRequestIndex;
import com.tastyhouse.domain.shop.model.ShopRequestStatus;
import com.tastyhouse.domain.shop.model.ShopRequestType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopRequestIndexStateMapperTest {

    @Test
    @DisplayName("ShopRequestIndex → ShopRequestIndexState → ShopRequestIndex 왕복 시 모든 필드가 보존된다")
    void shopRequestIndexRoundTrip() {
        ShopRequestIndex original = ShopRequestIndex.reconstitute(
            119L,
            ShopId.of(120L),
            ShopRequestType.THUMBNAIL_CHANGE,
            122L,
            "v23",
            ShopRequestStatus.APPROVED,
            "v25",
            UploadedFileId.of(126L),
            127L,
            LocalDateTime.of(2026, 1, 1, 10, 28),
            LocalDateTime.of(2026, 1, 2, 10, 29),
            LocalDateTime.of(2026, 1, 3, 10, 30)
        );

        ShopRequestIndex restored = ShopRequestIndexStateMapper.toDomain(ShopRequestIndexStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
