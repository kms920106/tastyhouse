package com.tastyhouse.application.shop.store;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopTemporaryClosure;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopTemporaryClosureStateMapperTest {

    @Test
    @DisplayName("ShopTemporaryClosure → ShopTemporaryClosureState → ShopTemporaryClosure 왕복 시 모든 필드가 보존된다")
    void shopTemporaryClosureRoundTrip() {
        ShopTemporaryClosure original = ShopTemporaryClosure.reconstitute(
            185L,
            ShopId.of(186L),
            LocalDate.of(2026, 2, 4),
            LocalDate.of(2026, 2, 5),
            LocalDateTime.of(2026, 1, 6, 10, 29)
        );

        ShopTemporaryClosure restored = ShopTemporaryClosureStateMapper.toDomain(ShopTemporaryClosureStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
