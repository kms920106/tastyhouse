package com.tastyhouse.application.shop.store;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.ShopRiderGuide;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopRiderGuideStateMapperTest {

    @Test
    @DisplayName("ShopRiderGuide → ShopRiderGuideState → ShopRiderGuide 왕복 시 모든 필드가 보존된다")
    void shopRiderGuideRoundTrip() {
        ShopRiderGuide original = ShopRiderGuide.reconstitute(
            147L,
            ShopId.of(148L),
            "v49",
            "v50",
            "v51",
            "v52",
            new BigDecimal("53.125"),
            new BigDecimal("54.125"),
            LocalDateTime.of(2026, 1, 28, 10, 55),
            LocalDateTime.of(2026, 1, 1, 10, 56)
        );

        ShopRiderGuide restored = ShopRiderGuideStateMapper.toDomain(ShopRiderGuideStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("ShopRiderGuideHistory → ShopRiderGuideHistoryState → ShopRiderGuideHistory 왕복 시 모든 필드가 보존된다")
    void shopRiderGuideHistoryRoundTrip() {
        ShopRiderGuideHistory original = ShopRiderGuideHistory.reconstitute(
            157L,
            ShopId.of(158L),
            RiderGuideActorType.ADMIN,
            160L,
            RiderGuideActionType.REVISION_REQUEST,
            "v62",
            "v63",
            "v64",
            LocalDateTime.of(2026, 1, 10, 10, 5)
        );

        ShopRiderGuideHistory restored = ShopRiderGuideHistoryStateMapper.toDomain(ShopRiderGuideHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
