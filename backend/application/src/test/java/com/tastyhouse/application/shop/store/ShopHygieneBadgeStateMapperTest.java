package com.tastyhouse.application.shop.store;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopHygieneBadgeStateMapperTest {

    @Test
    @DisplayName("ShopHygieneBadge → ShopHygieneBadgeState → ShopHygieneBadge 왕복 시 모든 필드가 보존된다")
    void shopHygieneBadgeRoundTrip() {
        ShopHygieneBadge original = ShopHygieneBadge.reconstitute(
            148L,
            ShopId.of(149L),
            HygieneBadgeType.CESCO_WHITE,
            LocalDate.of(2026, 2, 24),
            "v52",
            LocalDateTime.of(2026, 1, 26, 10, 53)
        );

        ShopHygieneBadge restored = ShopHygieneBadgeStateMapper.toDomain(ShopHygieneBadgeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
