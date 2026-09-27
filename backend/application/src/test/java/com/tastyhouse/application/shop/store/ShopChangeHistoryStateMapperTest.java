package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActorType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeHistory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopChangeHistoryStateMapperTest {

    @Test
    @DisplayName("ShopChangeHistory → ShopChangeHistoryState → ShopChangeHistory 왕복 시 모든 필드가 보존된다")
    void shopChangeHistoryRoundTrip() {
        ShopChangeHistory original = ShopChangeHistory.reconstitute(
            113L,
            ShopId.of(114L),
            ShopChangeCategory.OPERATION,
            ShopChangeType.DELIVERY_AREA_ADJUSTMENT,
            ShopChangeActionType.DELETE,
            ShopChangeActorType.CEO,
            119L,
            "v20",
            "v21",
            LocalDateTime.of(2026, 1, 23, 10, 22)
        );

        ShopChangeHistory restored = ShopChangeHistoryStateMapper.toDomain(ShopChangeHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
