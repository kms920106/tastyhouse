package com.tastyhouse.application.shop.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopConvenienceInfoStateMapperTest {

    @Test
    @DisplayName("ShopConvenienceInfo → ShopConvenienceInfoState → ShopConvenienceInfo 왕복 시 모든 필드가 보존된다")
    void shopConvenienceInfoRoundTrip() {
        ShopConvenienceInfo original = ShopConvenienceInfo.reconstitute(
            137L,
            ShopId.of(138L),
            true,
            false,
            true,
            false,
            "v43",
            new BigDecimal("44.125"),
            new BigDecimal("45.125"),
            LocalDateTime.of(2026, 1, 19, 10, 46),
            LocalDateTime.of(2026, 1, 20, 10, 47)
        );

        ShopConvenienceInfo restored = ShopConvenienceInfoStateMapper.toDomain(ShopConvenienceInfoStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
