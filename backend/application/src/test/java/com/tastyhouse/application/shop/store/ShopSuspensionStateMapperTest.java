package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopSuspension;
import com.tastyhouse.domain.shop.model.SuspensionReason;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopSuspensionStateMapperTest {

    @Test
    @DisplayName("ShopSuspension → ShopSuspensionState → ShopSuspension 왕복 시 모든 필드가 보존된다")
    void shopSuspensionRoundTrip() {
        ShopSuspension original = ShopSuspension.reconstitute(
            176L,
            ShopId.of(177L),
            SuspensionReason.EARLY_CLOSE,
            OrderMethod.TAKEOUT,
            LocalDateTime.of(2026, 1, 25, 10, 20),
            LocalDateTime.of(2026, 1, 26, 10, 21),
            LocalDateTime.of(2026, 1, 27, 10, 22),
            LocalDateTime.of(2026, 1, 28, 10, 23),
            LocalDateTime.of(2026, 1, 1, 10, 24)
        );

        ShopSuspension restored = ShopSuspensionStateMapper.toDomain(ShopSuspensionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
