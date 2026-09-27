package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopOriginInfoStateMapperTest {

    @Test
    @DisplayName("ShopOriginInfo → ShopOriginInfoState → ShopOriginInfo 왕복 시 모든 필드가 보존된다")
    void shopOriginInfoRoundTrip() {
        ShopOriginInfo original = ShopOriginInfo.reconstitute(
            154L,
            ShopId.of(155L),
            OriginSourceType.DIRECT,
            "v57",
            "v58",
            LocalDateTime.of(2026, 1, 4, 10, 59),
            LocalDateTime.of(2026, 1, 5, 10, 0)
        );

        ShopOriginInfo restored = ShopOriginInfoStateMapper.toDomain(ShopOriginInfoStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
