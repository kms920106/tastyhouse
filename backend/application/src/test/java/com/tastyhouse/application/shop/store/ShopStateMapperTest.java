package com.tastyhouse.application.shop.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shop.port.out.write.ShopState;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.StationId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopStateMapperTest {

    @Test
    @DisplayName("Shop → ShopState → Shop 왕복 시 모든 필드가 보존된다")
    void shopRoundTrip() {
        Shop original = Shop.reconstitute(
            127L,
            CeoId.of(128L),
            StationId.of(129L),
            "v30",
            new BigDecimal("31.125"),
            new BigDecimal("32.125"),
            33.5,
            "v34",
            "v35",
            "v36",
            UploadedFileId.of(137L),
            UploadedFileId.of(138L),
            true,
            false,
            true,
            42,
            false,
            true,
            false,
            LocalDateTime.of(2026, 1, 19, 10, 46),
            LocalDateTime.of(2026, 1, 20, 10, 47)
        );

        Shop restored = ShopStateMapper.toDomain(ShopStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("nullable VO가 전부 null인 Shop도 State로 왕복되고 State의 FK는 null이다")
    void shopRoundTripWithNullableVosNull() {
        Shop original = Shop.reconstitute(
            null,
            null,
            StationId.of(1L),
            "가게",
            BigDecimal.ONE,
            BigDecimal.ONE,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            0,
            false,
            false,
            false,
            null,
            null
        );

        ShopState state = ShopStateMapper.toState(original);
        Shop restored = ShopStateMapper.toDomain(state);

        assertThat(state.ceoId()).isNull();
        assertThat(state.thumbnailImageFileId()).isNull();
        assertThat(state.trademarkImageFileId()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
