package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopCeoAssignmentHistoryStateMapperTest {

    @Test
    @DisplayName("ShopCeoAssignmentHistory → ShopCeoAssignmentHistoryState → ShopCeoAssignmentHistory 왕복 시 모든 필드가 보존된다")
    void shopCeoAssignmentHistoryRoundTrip() {
        ShopCeoAssignmentHistory original = ShopCeoAssignmentHistory.reconstitute(
            107L,
            ShopId.of(108L),
            CeoId.of(109L),
            ShopCeoAssignmentActionType.GRANT,
            111L,
            LocalDateTime.of(2026, 1, 13, 10, 12)
        );

        ShopCeoAssignmentHistory restored = ShopCeoAssignmentHistoryStateMapper.toDomain(ShopCeoAssignmentHistoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
